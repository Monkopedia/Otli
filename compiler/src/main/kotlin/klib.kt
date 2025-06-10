package com.monkopedia.otli

import com.intellij.openapi.project.Project
import com.monkopedia.otli.serialization.KlibMetadataIncrementalSerializer
import com.monkopedia.otli.serialization.OtliIrFileMetadata
import com.monkopedia.otli.serialization.OtliIrModuleSerializer
import java.io.File
import java.util.Properties
import kotlin.to
import org.jetbrains.kotlin.analyzer.AbstractAnalyzerWithCompilerReport
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.analyzer.CompilationErrorException
import org.jetbrains.kotlin.backend.common.CommonKLibResolver
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContextImpl
import org.jetbrains.kotlin.backend.common.linkage.issues.checkNoUnboundSymbols
import org.jetbrains.kotlin.backend.common.linkage.partial.createPartialLinkageSupportForLinker
import org.jetbrains.kotlin.backend.common.overrides.FakeOverrideChecker
import org.jetbrains.kotlin.backend.common.serialization.CompatibilityMode
import org.jetbrains.kotlin.backend.common.serialization.DescriptorByIdSignatureFinderImpl
import org.jetbrains.kotlin.backend.common.serialization.DeserializationStrategy
import org.jetbrains.kotlin.backend.common.serialization.IrSerializationSettings
import org.jetbrains.kotlin.backend.common.serialization.KotlinFileSerializedData
import org.jetbrains.kotlin.backend.common.serialization.metadata.DynamicTypeDeserializer
import org.jetbrains.kotlin.backend.common.serialization.metadata.KlibSingleFileMetadataSerializer
import org.jetbrains.kotlin.backend.common.serialization.serializeModuleIntoKlib
import org.jetbrains.kotlin.backend.common.serialization.signature.IdSignatureDescriptor
import org.jetbrains.kotlin.backend.common.toLogger
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.DuplicatedUniqueNameStrategy
import org.jetbrains.kotlin.config.KlibConfigurationKeys
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.config.messageCollector
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.descriptors.IrDescriptorBasedFunctionFactory
import org.jetbrains.kotlin.ir.linkage.IrDeserializer
import org.jetbrains.kotlin.ir.linkage.partial.partialLinkageConfig
import org.jetbrains.kotlin.ir.util.DeclarationStubGenerator
import org.jetbrains.kotlin.ir.util.ExternalDependenciesGenerator
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.js.analyze.AbstractTopDownAnalyzerFacadeForWeb
import org.jetbrains.kotlin.js.analyzer.JsAnalysisResult
import org.jetbrains.kotlin.konan.properties.propertyList
import org.jetbrains.kotlin.library.KLIB_PROPERTY_CONTAINS_ERROR_CODE
import org.jetbrains.kotlin.library.KLIB_PROPERTY_DEPENDS
import org.jetbrains.kotlin.library.KotlinAbiVersion
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.library.KotlinLibraryVersioning
import org.jetbrains.kotlin.library.SerializedIrFile
import org.jetbrains.kotlin.library.impl.BuiltInsPlatform
import org.jetbrains.kotlin.library.impl.buildKotlinLibrary
import org.jetbrains.kotlin.library.metadata.KlibMetadataFactories
import org.jetbrains.kotlin.library.metadata.KlibMetadataVersion
import org.jetbrains.kotlin.library.uniqueName
import org.jetbrains.kotlin.library.unresolvedDependencies
import org.jetbrains.kotlin.progress.ProgressIndicatorAndCompilationCanceledStatus
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi2ir.Psi2IrConfiguration
import org.jetbrains.kotlin.psi2ir.Psi2IrTranslator
import org.jetbrains.kotlin.psi2ir.descriptors.IrBuiltInsOverDescriptors
import org.jetbrains.kotlin.psi2ir.generators.DeclarationStubGeneratorImpl
import org.jetbrains.kotlin.psi2ir.generators.GeneratorContext
import org.jetbrains.kotlin.psi2ir.generators.TypeTranslatorImpl
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.storage.LockBasedStorageManager
import org.jetbrains.kotlin.storage.StorageManager
import org.jetbrains.kotlin.utils.DFS
import org.jetbrains.kotlin.utils.memoryOptimizedFilter

// Considering library built-ins if it has no dependencies.
// All non-built-ins libraries must have built-ins as a dependency.
val KotlinLibrary.isBuiltIns: Boolean
    get() =
        manifestProperties
            .propertyList(KLIB_PROPERTY_DEPENDS, escapeInQuotes = true)
            .isEmpty()

val KotlinLibrary.otliOutputName: String?
    get() = manifestProperties.getProperty(KLIB_PROPERTY_OTLI_OUTPUT_NAME)

data class IrModuleInfo(
    val module: IrModuleFragment,
    val allDependencies: List<IrModuleFragment>,
    val builtins: IrBuiltIns,
    val symbolTable: SymbolTable,
    val deserializer: OtliIrLinker,
    val moduleFragmentToUniqueName: Map<IrModuleFragment, String>
)

fun sortDependencies(
    moduleDependencies: Map<KotlinLibrary, List<KotlinLibrary>>
): Collection<KotlinLibrary> = DFS
    .topologicalOrder(moduleDependencies.keys) { m ->
        moduleDependencies.getValue(m)
    }.reversed()

fun deserializeDependencies(
    sortedDependencies: Collection<KotlinLibrary>,
    irLinker: OtliIrLinker,
    mainModuleLib: KotlinLibrary?,
    filesToLoad: Set<String>?,
    mapping: (KotlinLibrary) -> ModuleDescriptor
): Map<IrModuleFragment, KotlinLibrary> = sortedDependencies.associateBy { klib ->
    val descriptor = mapping(klib)
    when {
        mainModuleLib == null ->
            irLinker.deserializeIrModuleHeader(
                descriptor,
                klib,
                { DeserializationStrategy.EXPLICITLY_EXPORTED }
            )

        filesToLoad != null && klib == mainModuleLib ->
            irLinker.deserializeDirtyFiles(
                descriptor,
                klib,
                filesToLoad
            )

        filesToLoad != null && klib != mainModuleLib ->
            irLinker.deserializeHeadersWithInlineBodies(
                descriptor,
                klib
            )

        klib == mainModuleLib ->
            irLinker.deserializeIrModuleHeader(
                descriptor,
                klib,
                { DeserializationStrategy.ALL }
            )

        else ->
            irLinker.deserializeIrModuleHeader(
                descriptor,
                klib,
                { DeserializationStrategy.EXPLICITLY_EXPORTED }
            )
    }
}

fun loadIr(
    depsDescriptors: ModulesStructure,
    irFactory: IrFactory,
    filesToLoad: Set<String>? = null
): IrModuleInfo {
    val project = depsDescriptors.project
    val mainModule = depsDescriptors.mainModule
    val configuration = depsDescriptors.compilerConfiguration
    val allDependencies = depsDescriptors.allDependencies
    val messageLogger = configuration.messageCollector
    val partialLinkageEnabled = configuration.partialLinkageConfig.isEnabled

    val signaturer = IdSignatureDescriptor(OtliManglerDesc)
    val symbolTable = SymbolTable(signaturer, irFactory)

    when (mainModule) {
        is MainModule.SourceFiles -> {
            assert(filesToLoad == null)
            val psi2IrContext = preparePsi2Ir(depsDescriptors, symbolTable, partialLinkageEnabled)
            val friendModules =
                mapOf(
                    psi2IrContext.moduleDescriptor.name.asString() to
                        depsDescriptors.friendDependencies.map { it.uniqueName }
                )

            return getIrModuleInfoForSourceFiles(
                psi2IrContext,
                project,
                configuration,
                mainModule.files,
                sortDependencies(depsDescriptors.moduleDependencies),
                friendModules,
                symbolTable,
                messageLogger
            ) { depsDescriptors.getModuleDescriptor(it) }
        }

        is MainModule.Klib -> {
            val mainPath = File(mainModule.libPath).canonicalPath
            val mainModuleLib =
                allDependencies.find { it.libraryFile.canonicalPath == mainPath }
                    ?: error("No module with ${mainModule.libPath} found")
            val moduleDescriptor = depsDescriptors.getModuleDescriptor(mainModuleLib)
            val sortedDependencies = sortDependencies(depsDescriptors.moduleDependencies)
            val friendModules =
                mapOf(
                    mainModuleLib.uniqueName to
                        depsDescriptors.friendDependencies.map { it.uniqueName }
                )

            return getIrModuleInfoForKlib(
                moduleDescriptor,
                sortedDependencies,
                friendModules,
                filesToLoad,
                configuration,
                symbolTable,
                messageLogger
            ) { depsDescriptors.getModuleDescriptor(it) }
        }
    }
}

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun getIrModuleInfoForKlib(
    moduleDescriptor: ModuleDescriptor,
    sortedDependencies: Collection<KotlinLibrary>,
    friendModules: Map<String, List<String>>,
    filesToLoad: Set<String>?,
    configuration: CompilerConfiguration,
    symbolTable: SymbolTable,
    messageCollector: MessageCollector,
    mapping: (KotlinLibrary) -> ModuleDescriptor
): IrModuleInfo {
    val mainModuleLib = sortedDependencies.last()
    val typeTranslator =
        TypeTranslatorImpl(symbolTable, configuration.languageVersionSettings, moduleDescriptor)
    val irBuiltIns =
        IrBuiltInsOverDescriptors(moduleDescriptor.builtIns, typeTranslator, symbolTable)

    val irLinker =
        OtliIrLinker(
            currentModule = null,
            messageCollector = messageCollector,
            builtIns = irBuiltIns,
            symbolTable = symbolTable,
            partialLinkageSupport =
            createPartialLinkageSupportForLinker(
                partialLinkageConfig = configuration.partialLinkageConfig,
                builtIns = irBuiltIns,
                messageCollector = messageCollector
            ),
            icData = null,
            friendModules = friendModules
        )

    val deserializedModuleFragmentsToLib =
        deserializeDependencies(sortedDependencies, irLinker, mainModuleLib, filesToLoad, mapping)
    val deserializedModuleFragments = deserializedModuleFragmentsToLib.keys.toList()
    irBuiltIns.functionFactory =
        IrDescriptorBasedFunctionFactory(
            irBuiltIns,
            symbolTable,
            typeTranslator,
            null,
            true
        )

    val moduleFragment = deserializedModuleFragments.last()

    irLinker.init(null)
    ExternalDependenciesGenerator(
        symbolTable,
        listOf(irLinker)
    ).generateUnboundSymbolsAsDependencies()
    irLinker.postProcess(inOrAfterLinkageStep = true)

    return IrModuleInfo(
        moduleFragment,
        deserializedModuleFragments,
        irBuiltIns,
        symbolTable,
        irLinker,
        deserializedModuleFragmentsToLib.getUniqueNameForEachFragment()
    )
}

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun getIrModuleInfoForSourceFiles(
    psi2IrContext: GeneratorContext,
    project: Project,
    configuration: CompilerConfiguration,
    files: List<KtFile>,
    allSortedDependencies: Collection<KotlinLibrary>,
    friendModules: Map<String, List<String>>,
    symbolTable: SymbolTable,
    messageCollector: MessageCollector,
    mapping: (KotlinLibrary) -> ModuleDescriptor
): IrModuleInfo {
    val irBuiltIns = psi2IrContext.irBuiltIns
    val stubGenerator = DeclarationStubGeneratorImpl(
        psi2IrContext.moduleDescriptor,
        symbolTable,
        psi2IrContext.irBuiltIns,
        DescriptorByIdSignatureFinderImpl(psi2IrContext.moduleDescriptor, OtliManglerDesc)
    )
    val irLinker =
        OtliIrLinker(
            currentModule = psi2IrContext.moduleDescriptor,
            messageCollector = messageCollector,
            builtIns = irBuiltIns,
            symbolTable = symbolTable,
            partialLinkageSupport =
            createPartialLinkageSupportForLinker(
                partialLinkageConfig = configuration.partialLinkageConfig,
                builtIns = irBuiltIns,
                messageCollector = messageCollector
            ),
            icData = null,
            friendModules = friendModules,
            stubGenerator = stubGenerator
        )
    val deserializedModuleFragmentsToLib =
        deserializeDependencies(allSortedDependencies, irLinker, null, null, mapping)
    val deserializedModuleFragments = deserializedModuleFragmentsToLib.keys.toList()
    (irBuiltIns as IrBuiltInsOverDescriptors).functionFactory =
        IrDescriptorBasedFunctionFactory(
            irBuiltIns,
            symbolTable,
            psi2IrContext.typeTranslator,
            null,
            true
        )

    val (moduleFragment, _) =
        psi2IrContext.generateModuleFragmentWithPlugins(
            project,
            files,
            irLinker,
            messageCollector,
            stubGenerator
        )

    if (configuration.getBoolean(OtliConfigurationKeys.FAKE_OVERRIDE_VALIDATOR)) {
        val fakeOverrideChecker = FakeOverrideChecker(OtliManglerIr, OtliManglerDesc)
        irLinker.modules.forEach { fakeOverrideChecker.check(it) }
    }

    return IrModuleInfo(
        moduleFragment,
        deserializedModuleFragments,
        irBuiltIns,
        symbolTable,
        irLinker,
        deserializedModuleFragmentsToLib.getUniqueNameForEachFragment()
    )
}

private fun preparePsi2Ir(
    depsDescriptors: ModulesStructure,
    symbolTable: SymbolTable,
    partialLinkageEnabled: Boolean
): GeneratorContext {
    val analysisResult = depsDescriptors.frontEndResult
    val psi2Ir =
        Psi2IrTranslator(
            depsDescriptors.compilerConfiguration.languageVersionSettings,
            Psi2IrConfiguration(ignoreErrors = false, partialLinkageEnabled),
            depsDescriptors.compilerConfiguration::checkNoUnboundSymbols
        )
    return psi2Ir.createGeneratorContext(
        analysisResult.moduleDescriptor,
        analysisResult.bindingContext,
        symbolTable
    )
}

fun GeneratorContext.generateModuleFragmentWithPlugins(
    project: Project,
    files: List<KtFile>,
    irLinker: IrDeserializer,
    messageCollector: MessageCollector,
    stubGenerator: DeclarationStubGenerator? = null
): Pair<IrModuleFragment, IrPluginContext> {
    val psi2Ir =
        Psi2IrTranslator(
            languageVersionSettings,
            configuration,
            messageCollector::checkNoUnboundSymbols
        )
    val extensions = IrGenerationExtension.getInstances(project)

    // plugin context should be instantiated before postprocessing steps
    val pluginContext =
        IrPluginContextImpl(
            moduleDescriptor,
            bindingContext,
            languageVersionSettings,
            symbolTable,
            typeTranslator,
            irBuiltIns,
            linker = irLinker,
            messageCollector
        )
    if (extensions.isNotEmpty()) {
        for (extension in extensions) {
            psi2Ir.addPostprocessingStep { module ->
                val old = stubGenerator?.unboundSymbolGeneration
                try {
                    stubGenerator?.unboundSymbolGeneration = true
                    extension.generate(module, pluginContext)
                } finally {
                    stubGenerator?.unboundSymbolGeneration = old!!
                }
            }
        }
    }

    return psi2Ir.generateModuleFragment(
        this,
        files,
        listOf(stubGenerator ?: irLinker)
    ) to pluginContext
}

private fun createBuiltIns(storageManager: StorageManager) =
    object : KotlinBuiltIns(storageManager) {}

val CFactories = KlibMetadataFactories(::createBuiltIns, DynamicTypeDeserializer)

sealed class MainModule {
    class SourceFiles(val files: List<KtFile>) : MainModule()

    class Klib(val libPath: String) : MainModule()
}

class ModulesStructure(
    val project: Project,
    val mainModule: MainModule,
    val compilerConfiguration: CompilerConfiguration,
    dependencies: Collection<String>,
    friendDependenciesPaths: Collection<String>
) {
    val allDependenciesResolution =
        CommonKLibResolver.resolveWithoutDependencies(
            dependencies,
            compilerConfiguration.messageCollector.toLogger(),
            compilerConfiguration.get(OtliConfigurationKeys.ZIP_FILE_SYSTEM_ACCESSOR),
            duplicatedUniqueNameStrategy =
            compilerConfiguration.get(
                KlibConfigurationKeys.DUPLICATED_UNIQUE_NAME_STRATEGY,
                DuplicatedUniqueNameStrategy.DENY
            )
        )

    val allDependencies: List<KotlinLibrary>
        get() = allDependenciesResolution.libraries

    val friendDependencies =
        allDependencies.run {
            val friendAbsolutePaths = friendDependenciesPaths.map { File(it).canonicalPath }
            memoryOptimizedFilter {
                it.libraryFile.absolutePath in friendAbsolutePaths
            }
        }

    val moduleDependencies: Map<KotlinLibrary, List<KotlinLibrary>> by lazy {
        val transitives = allDependenciesResolution.resolveWithDependencies().getFullResolvedList()
        transitives
            .associate { klib ->
                klib.library to klib.resolvedDependencies.map { d -> d.library }
            }.toMap()
    }

    private val builtInsDep = allDependencies.find { it.isBuiltIns }

    class FrontEndResult(val otliAnalysisResult: AnalysisResult) {
        val moduleDescriptor: ModuleDescriptor
            get() = otliAnalysisResult.moduleDescriptor

        val bindingContext: BindingContext
            get() = otliAnalysisResult.bindingContext
    }

    lateinit var frontEndResult: FrontEndResult

    private val languageVersionSettings: LanguageVersionSettings =
        compilerConfiguration.languageVersionSettings

    private val storageManager: LockBasedStorageManager =
        LockBasedStorageManager("ModulesStructure")
    private var runtimeModule: ModuleDescriptorImpl? = null

    private val _descriptors: MutableMap<KotlinLibrary, ModuleDescriptorImpl> = mutableMapOf()

    init {
        val descriptors = allDependencies.map { getModuleDescriptorImpl(it) }
        val friendDescriptors = friendDependencies.mapTo(mutableSetOf(), ::getModuleDescriptorImpl)
        descriptors.forEach { descriptor ->
            descriptor.setDependencies(descriptors, friendDescriptors)
        }
    }

    // TODO: these are roughly equivalent to KlibResolvedModuleDescriptorsFactoryImpl. Refactor me.
    val descriptors: Map<KotlinLibrary, ModuleDescriptor>
        get() = _descriptors

    private fun getModuleDescriptorImpl(current: KotlinLibrary): ModuleDescriptorImpl {
        if (current in _descriptors) {
            return _descriptors.getValue(current)
        }

        val isBuiltIns = current.unresolvedDependencies.isEmpty()

        val lookupTracker =
            compilerConfiguration[CommonConfigurationKeys.LOOKUP_TRACKER]
                ?: LookupTracker.DO_NOTHING
        val md =
            CFactories.DefaultDeserializedDescriptorFactory.createDescriptorOptionalBuiltIns(
                current,
                languageVersionSettings,
                storageManager,
                runtimeModule?.builtIns,
                // TODO: This is a speed optimization used by Native. Don't bother for now.
                packageAccessHandler = null,
                lookupTracker = LookupTracker.DO_NOTHING
            )
        if (isBuiltIns) runtimeModule = md

        _descriptors[current] = md

        return md
    }

    fun runAnalysis(
        analyzer: AbstractAnalyzerWithCompilerReport,
        analyzerFacade: AbstractTopDownAnalyzerFacadeForWeb
    ) {
        require(mainModule is MainModule.SourceFiles)
        val files = mainModule.files

        analyzer.analyzeAndReport(files) {
            analyzerFacade.analyzeFiles(
                files,
                project,
                compilerConfiguration,
                descriptors.values.toList(),
                friendDependencies.map { getModuleDescriptor(it) },
                analyzer.targetEnvironment,
                thisIsBuiltInsModule = builtInModuleDescriptor == null,
                customBuiltInsModule = builtInModuleDescriptor
            )
        }

        ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()

        val analysisResult = analyzer.analysisResult

        if (analyzer.hasErrors() || analysisResult !is JsAnalysisResult) {
            throw CompilationErrorException()
        }

        analyzerFacade.checkForErrors(files, analysisResult.bindingContext)

        frontEndResult = FrontEndResult(analysisResult)
    }

    fun getModuleDescriptor(current: KotlinLibrary): ModuleDescriptor =
        getModuleDescriptorImpl(current)

    val builtInModuleDescriptor =
        if (builtInsDep != null) {
            getModuleDescriptor(builtInsDep)
        } else {
            null // null in case compiling builtInModule itself
        }
}

const val KLIB_PROPERTY_OTLI_OUTPUT_NAME = "otliOutputName"

private fun Map<IrModuleFragment, KotlinLibrary>.getUniqueNameForEachFragment(): Map<
    IrModuleFragment,
    String
    > =
    this.entries
        .mapNotNull { (moduleFragment, klib) ->
            klib.otliOutputName?.let { moduleFragment to it }
        }.toMap()

fun generateKLib(
    depsDescriptors: ModulesStructure,
    outputKlibPath: String,
    nopack: Boolean,
    abiVersion: KotlinAbiVersion = KotlinAbiVersion.CURRENT,
    jsOutputName: String?,
    icData: List<KotlinFileSerializedData>,
    moduleFragment: IrModuleFragment,
    irBuiltIns: IrBuiltIns,
    diagnosticReporter: DiagnosticReporter,
    builtInsPlatform: BuiltInsPlatform = BuiltInsPlatform.JS
) {
    val configuration = depsDescriptors.compilerConfiguration
    val allDependencies = depsDescriptors.allDependencies

    serializeModuleIntoKlib(
        configuration[CommonConfigurationKeys.MODULE_NAME]!!,
        configuration,
        diagnosticReporter,
        KlibMetadataIncrementalSerializer(depsDescriptors, moduleFragment),
        outputKlibPath,
        allDependencies,
        moduleFragment,
        irBuiltIns,
        icData,
        nopack,
        perFile = false,
        false,
        abiVersion,
        jsOutputName,
        builtInsPlatform
    )
}

fun serializeModuleIntoKlib(
    moduleName: String,
    configuration: CompilerConfiguration,
    diagnosticReporter: DiagnosticReporter,
    metadataSerializer: KlibSingleFileMetadataSerializer<*>,
    klibPath: String,
    dependencies: List<KotlinLibrary>,
    moduleFragment: IrModuleFragment,
    irBuiltIns: IrBuiltIns,
    cleanFiles: List<KotlinFileSerializedData>,
    nopack: Boolean,
    perFile: Boolean,
    containsErrorCode: Boolean = false,
    abiVersion: KotlinAbiVersion,
    otliOutputName: String?,
    builtInsPlatform: BuiltInsPlatform = BuiltInsPlatform.COMMON
) {
//    val incrementalResultsConsumer = configuration.get(JSConfigurationKeys.INCREMENTAL_RESULTS_CONSUMER)
//    val empty = ByteArray(0)
    val serializerOutput = serializeModuleIntoKlib(
        moduleName = moduleFragment.name.asString(),
        irModuleFragment = moduleFragment,
        irBuiltins = irBuiltIns,
        configuration = configuration,
        diagnosticReporter = diagnosticReporter,
        compatibilityMode = CompatibilityMode(abiVersion),
        cleanFiles = cleanFiles,
        dependencies = dependencies,
        createModuleSerializer = {
                irDiagnosticReporter,
                irBuiltins,
                compatibilityMode,
                normalizeAbsolutePaths,
                sourceBaseDirs,
                languageVersionSettings,
                shouldCheckSignaturesOnUniqueness
            ->
            OtliIrModuleSerializer(
                settings = IrSerializationSettings(
                    languageVersionSettings = languageVersionSettings,
                    compatibilityMode = compatibilityMode,
                    normalizeAbsolutePaths = normalizeAbsolutePaths,
                    sourceBaseDirs = sourceBaseDirs,
                    shouldCheckSignaturesOnUniqueness = shouldCheckSignaturesOnUniqueness
                ),
                irDiagnosticReporter,
                irBuiltins
            ) { OtliIrFileMetadata(emptyList()) }
        },
        metadataSerializer = metadataSerializer,
        processCompiledFileData = { ioFile, compiledFile ->
//            incrementalResultsConsumer?.run {
//                processPackagePart(ioFile, compiledFile.metadata, empty, empty)
//                with(compiledFile.irData!!) {
//                    processIrFile(
//                        ioFile,
//                        fileData,
//                        types,
//                        signatures,
//                        strings,
//                        declarations,
//                        bodies,
//                        fqName.toByteArray(),
//                        fileMetadata,
//                        debugInfo,
//                    )
//                }
//            }
        },
        processKlibHeader = {
//            incrementalResultsConsumer?.processHeader(it)
        }
    )

    val fullSerializedIr = serializerOutput.serializedIr
        ?: error("Metadata-only KLIBs are not supported in Kotlin/Otli")

    val versions = KotlinLibraryVersioning(
        abiVersion = abiVersion,
        compilerVersion = KotlinCompilerVersion.VERSION,
        metadataVersion = KlibMetadataVersion.INSTANCE.toString()
    )

    val properties = Properties().also { p ->
        if (otliOutputName != null) {
            p.setProperty(KLIB_PROPERTY_OTLI_OUTPUT_NAME, otliOutputName)
        }
        if (containsErrorCode) {
            p.setProperty(KLIB_PROPERTY_CONTAINS_ERROR_CODE, "true")
        }
    }

    buildKotlinLibrary(
        linkDependencies = serializerOutput.neededLibraries,
        ir = fullSerializedIr,
        metadata = serializerOutput.serializedMetadata ?: error("expected serialized metadata"),
        manifestProperties = properties,
        moduleName = moduleName,
        nopack = nopack,
        perFile = perFile,
        output = klibPath,
        versions = versions,
        builtInsPlatform = builtInsPlatform
    )
}

internal val SerializedIrFile.fileMetadata: ByteArray
    get() = backendSpecificMetadata
        ?: error("Expect file caches to have backendSpecificMetadata, but '$path' doesn't")
