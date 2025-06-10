package com.monkopedia.otli

import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import java.io.File
import kotlin.collections.plus
import kotlin.text.isEmpty
import kotlin.text.split
import org.jetbrains.kotlin.analyzer.CompilationErrorException
import org.jetbrains.kotlin.backend.common.CompilationException
import org.jetbrains.kotlin.cli.common.CLICompiler
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.CommonCompilerPerformanceManager
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.ExitCode.COMPILATION_ERROR
import org.jetbrains.kotlin.cli.common.ExitCode.INTERNAL_ERROR
import org.jetbrains.kotlin.cli.common.ExitCode.OK
import org.jetbrains.kotlin.cli.common.config.addKotlinSourceRoot
import org.jetbrains.kotlin.cli.common.incrementalCompilationIsEnabledForJs
import org.jetbrains.kotlin.cli.common.messages.AnalyzerWithCompilerReport
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.ERROR
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.INFO
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.LOGGING
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.WARNING
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.common.messages.MessageUtil
import org.jetbrains.kotlin.cli.js.klib.TopDownAnalyzerFacadeForJSIR
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.DuplicatedUniqueNameStrategy
import org.jetbrains.kotlin.config.KlibConfigurationKeys
import org.jetbrains.kotlin.config.Services
import org.jetbrains.kotlin.config.getModuleNameForSource
import org.jetbrains.kotlin.diagnostics.DiagnosticReporterFactory
import org.jetbrains.kotlin.incremental.components.ExpectActualTracker
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.IrDeclarationReference
import org.jetbrains.kotlin.ir.linkage.partial.setupPartialLinkageConfig
import org.jetbrains.kotlin.ir.visitors.IrVisitor
import org.jetbrains.kotlin.js.analyzer.JsAnalysisResult
import org.jetbrains.kotlin.konan.file.ZipFileSystemAccessor
import org.jetbrains.kotlin.konan.file.ZipFileSystemCacheableAccessor
import org.jetbrains.kotlin.library.metadata.KlibMetadataVersion
import org.jetbrains.kotlin.metadata.deserialization.BinaryVersion
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.utils.KotlinPaths
import org.jetbrains.kotlin.utils.join

private class DisposableZipFileSystemAccessor private constructor(
    private val zipAccessor: ZipFileSystemCacheableAccessor
) : Disposable,
    ZipFileSystemAccessor by zipAccessor {
    constructor(cacheLimit: Int) : this(ZipFileSystemCacheableAccessor(cacheLimit))

    override fun dispose() {
        zipAccessor.reset()
    }
}

class OtliCompiler : CLICompiler<OtliCompilerArguments>() {
    class OtliCompilerPerformanceManager : CommonCompilerPerformanceManager("Otli Compiler")

    override val defaultPerformanceManager: CommonCompilerPerformanceManager =
        OtliCompilerPerformanceManager()

    override fun createArguments(): OtliCompilerArguments = OtliCompilerArguments()

    private class OtliIrTransformer(val module: ModulesStructure) {

        fun compileAndTransformIrNew(): IrModuleInfo = loadIr(module, IrFactoryImplForOtli())
    }

    private class ExitCodeException(val code: ExitCode) : Exception("Return: $code")

    override fun doExecute(
        arguments: OtliCompilerArguments,
        configuration: CompilerConfiguration,
        rootDisposable: Disposable,
        paths: KotlinPaths?
    ): ExitCode {
        try {
            compileToIr(arguments, configuration, rootDisposable, paths)
            return OK
        } catch (t: ExitCodeException) {
            return t.code
        }
    }

    internal fun compileToIr(
        arguments: OtliCompilerArguments,
        configuration: CompilerConfiguration,
        rootDisposable: Disposable,
        paths: KotlinPaths?
    ): IrModuleFragment {
        val messageCollector =
            configuration.getNotNull(CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY)

        val pluginLoadResult = loadPlugins(paths, arguments, configuration)
        if (pluginLoadResult != OK) throw ExitCodeException(pluginLoadResult)

        if (arguments.script) {
            messageCollector.report(ERROR, "K/Otli does not support Kotlin script (*.kts) files")
            throw ExitCodeException(COMPILATION_ERROR)
        }

        if (arguments.freeArgs.isEmpty() && !(incrementalCompilationIsEnabledForJs(arguments))) {
            if (arguments.version) {
                throw ExitCodeException(OK)
            }
            if (arguments.includes.isNullOrEmpty()) {
                messageCollector.report(
                    ERROR,
                    "Specify at least one source file or directory",
                    null
                )
                throw ExitCodeException(COMPILATION_ERROR)
            }
        }

        val libraries: List<String> =
            configureLibraries(arguments.libraries) + listOfNotNull(arguments.includes)
        val friendLibraries: List<String> = configureLibraries(arguments.friendModules)

        val commonSourcesArray = arguments.commonSources
        val commonSources = commonSourcesArray?.toSet() ?: emptySet()
        val hmppCliModuleStructure =
            configuration.get(CommonConfigurationKeys.HMPP_MODULE_STRUCTURE)
        for (arg in arguments.freeArgs) {
            configuration.addKotlinSourceRoot(
                arg,
                commonSources.contains(arg),
                hmppCliModuleStructure?.getModuleNameForSource(arg)
            )
        }

        arguments.relativePathBases?.let {
            configuration.put(KlibConfigurationKeys.KLIB_RELATIVE_PATH_BASES, it.toList())
        }

        configuration.put(
            KlibConfigurationKeys.KLIB_NORMALIZE_ABSOLUTE_PATH,
            arguments.normalizeAbsolutePath
        )
        configuration.put(
            KlibConfigurationKeys.PRODUCE_KLIB_SIGNATURES_CLASH_CHECKS,
            arguments.enableSignatureClashChecks
        )

        configuration.put(KlibConfigurationKeys.NO_DOUBLE_INLINING, arguments.noDoubleInlining)
        configuration.put(
            KlibConfigurationKeys.DUPLICATED_UNIQUE_NAME_STRATEGY,
            DuplicatedUniqueNameStrategy.parseOrDefault(
                arguments.duplicatedUniqueNameStrategy,
                default = DuplicatedUniqueNameStrategy.DENY
            )
        )

        // ----

        val environmentForOtli = KotlinCoreEnvironment.createForProduction(
            rootDisposable,
            configuration,
            EnvironmentConfigFiles.METADATA_CONFIG_FILES
        )
        val projectOtli = environmentForOtli.project
        val configurationOtli = environmentForOtli.configuration
        val sourcesFiles = environmentForOtli.getSourceFiles()

        configurationOtli.put(
            CLIConfigurationKeys.ALLOW_KOTLIN_PACKAGE,
            arguments.allowKotlinPackage
        )
        configurationOtli.put(
            CLIConfigurationKeys.RENDER_DIAGNOSTIC_INTERNAL_NAME,
            arguments.renderInternalDiagnosticNames
        )

        val zipAccessor = DisposableZipFileSystemAccessor(64)
        Disposer.register(rootDisposable, zipAccessor)
        configurationOtli.put(OtliConfigurationKeys.ZIP_FILE_SYSTEM_ACCESSOR, zipAccessor)

        val outputDirPath = arguments.outputDir
        val outputName = arguments.moduleName
        if (outputDirPath == null) {
            messageCollector.report(ERROR, "IR: Specify output dir via -ir-output-dir", null)
            throw ExitCodeException(COMPILATION_ERROR)
        }

        if (outputName == null) {
            messageCollector.report(ERROR, "IR: Specify output name via -ir-output-name", null)
            throw ExitCodeException(COMPILATION_ERROR)
        }

        if (messageCollector.hasErrors()) {
            throw ExitCodeException(COMPILATION_ERROR)
        }

        if (sourcesFiles.isEmpty() &&
            (!incrementalCompilationIsEnabledForJs(arguments)) &&
            arguments.includes.isNullOrEmpty()
        ) {
            messageCollector.report(ERROR, "No source files", null)
            throw ExitCodeException(COMPILATION_ERROR)
        }

        if (arguments.verbose) {
            reportCompiledSourcesList(messageCollector, sourcesFiles)
        }

        val moduleName = arguments.irModuleName ?: outputName
        configurationOtli.put(CommonConfigurationKeys.MODULE_NAME, moduleName)

        // Run analysis if main module is sources
        var sourceModule: ModulesStructure? = null
        val includes = arguments.includes
        val outputDir = File(outputDirPath)
        if (includes == null) {
            val outputKlibPath =
                if (arguments.outputKlib) {
                    outputDir.resolve("$outputName.klib")
                        .normalize().absolutePath
                } else {
                    outputDirPath
                }
            sourceModule =
                produceSourceModule(
                    environmentForOtli,
                    libraries,
                    friendLibraries,
                    arguments,
                    outputKlibPath
                )
        }

        val module = if (includes != null) {
            if (sourcesFiles.isNotEmpty()) {
                messageCollector.report(
                    ERROR,
                    "Source files are not supported when -Xinclude is present"
                )
            }
            val includesPath = File(includes).canonicalPath
            val mainLibPath =
                libraries.find { File(it).canonicalPath == includesPath }
                    ?: error("No library with name $includes ($includesPath) found")
            val kLib = MainModule.Klib(mainLibPath)
            ModulesStructure(
                projectOtli,
                kLib,
                configurationOtli,
                libraries,
                friendLibraries
            )
        } else {
            sourceModule!!
        }

        val start = System.currentTimeMillis()

        try {
            val otliIrTransformer =
                OtliIrTransformer(module)
            val outputs = otliIrTransformer.compileAndTransformIrNew()

            messageCollector.report(
                INFO,
                "Executable production duration: ${System.currentTimeMillis() - start}ms"
            )
            if (arguments.outputKlib) {
                val diagnosticsReporter =
                    DiagnosticReporterFactory.createPendingReporter(messageCollector)
                if (!File(outputDirPath).exists()) {
                    File(outputDirPath).mkdirs()
                }
                generateKLib(
                    sourceModule ?: error(""),
                    outputDirPath.plus("/$moduleName.klib"),
                    nopack = false,
                    jsOutputName = null,
                    icData = emptyList(),
                    moduleFragment = outputs.module,
                    irBuiltIns = outputs.builtins,
                    diagnosticReporter = diagnosticsReporter
                )
            }

            return outputs.module
        } catch (e: CompilationException) {
            messageCollector.report(
                ERROR,
                e.stackTraceToString(),
                CompilerMessageLocation.create(
                    path = e.path,
                    line = e.line,
                    column = e.column,
                    lineContent = e.content
                )
            )
            throw ExitCodeException(INTERNAL_ERROR)
        }
    }

    private fun produceSourceModule(
        environmentForOtli: KotlinCoreEnvironment,
        libraries: List<String>,
        friendLibraries: List<String>,
        arguments: OtliCompilerArguments,
        outputKlibPath: String
    ): ModulesStructure {
        val configuration = environmentForOtli.configuration
        val messageCollector =
            configuration.getNotNull(CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY)
        val diagnosticsReporter = DiagnosticReporterFactory.createPendingReporter(messageCollector)

        val mainModule = MainModule.SourceFiles(environmentForOtli.getSourceFiles())
        val moduleStructure =
            ModulesStructure(
                environmentForOtli.project,
                mainModule,
                configuration,
                libraries,
                friendLibraries
            )
        do {
            val analyzerFacade = TopDownAnalyzerFacadeForJSIR
            moduleStructure.runAnalysis(
                AnalyzerWithCompilerReport(environmentForOtli.configuration),
                analyzerFacade = analyzerFacade
            )
            val result = moduleStructure.frontEndResult.otliAnalysisResult
            if (result is JsAnalysisResult.RetryWithAdditionalRoots) {
                environmentForOtli.addKotlinSourceRoots(result.additionalKotlinRoots)
            }
        } while (result is JsAnalysisResult.RetryWithAdditionalRoots)

        val lookupTracker =
            configuration.get(CommonConfigurationKeys.LOOKUP_TRACKER) ?: LookupTracker.DO_NOTHING

        val analyzedOutput =
            compileModuleToAnalyzedFirWithPsi(
                moduleStructure = moduleStructure,
                ktFiles = environmentForOtli.getSourceFiles(),
                libraries = libraries,
                friendLibraries = friendLibraries,
                diagnosticsReporter = diagnosticsReporter,
                lookupTracker = lookupTracker
            )

        if (analyzedOutput.reportCompilationErrors(
                moduleStructure,
                diagnosticsReporter,
                messageCollector
            )
        ) {
            throw CompilationErrorException()
        }

        return moduleStructure
    }

    public override fun setupPlatformSpecificArgumentsAndServices(
        configuration: CompilerConfiguration,
        arguments: OtliCompilerArguments,
        services: Services
    ) {
        val messageCollector =
            configuration.getNotNull(CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY)

        configuration.putIfNotNull(
            CommonConfigurationKeys.LOOKUP_TRACKER,
            services[LookupTracker::class.java]
        )
        configuration.putIfNotNull(
            CommonConfigurationKeys.EXPECT_ACTUAL_TRACKER,
            services[ExpectActualTracker::class.java]
        )

        configuration.setupPartialLinkageConfig(
            mode = arguments.partialLinkageMode,
            logLevel = arguments.partialLinkageLogLevel,
            // no PL when producing KLIB
            compilerModeAllowsUsingPartialLinkage = arguments.includes != null,
            onWarning = { messageCollector.report(WARNING, it) },
            onError = { messageCollector.report(ERROR, it) }
        )
    }

    override fun executableScriptFileName(): String = "otlic"

    public override fun createMetadataVersion(versionArray: IntArray): BinaryVersion =
        KlibMetadataVersion(*versionArray)

    override fun MutableList<String>.addPlatformOptions(arguments: OtliCompilerArguments) {}

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            doMain(OtliCompiler(), args)
        }

        private fun reportCompiledSourcesList(
            messageCollector: MessageCollector,
            sourceFiles: List<KtFile>
        ) {
            val fileNames =
                sourceFiles.map { file ->
                    val virtualFile = file.virtualFile
                    if (virtualFile != null) {
                        MessageUtil.virtualFileToPath(virtualFile)
                    } else {
                        file.name + " (no virtual file)"
                    }
                }
            messageCollector.report(
                LOGGING,
                "Compiling source files: " + join(fileNames, ", "),
                null
            )
        }

        private fun configureLibraries(libraryString: String?): List<String> =
            libraryString?.splitByPathSeparator() ?: emptyList()

        private fun String.splitByPathSeparator(): List<String> = this
            .split(File.pathSeparator.toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()
            .filterNot { it.isEmpty() }
    }
}

class MyVisitor : IrVisitor<Unit, Unit>() {
    var depth = 0
    override fun visitElement(element: IrElement, data: Unit) {
        println(
            "${
                "    ".repeat(
                    depth
                )
            }Element: ${element::class} ${(element as? IrDeclarationReference)?.symbol}"
        )
        depth++
        element.acceptChildren(this, data)
        depth--
    }
}
