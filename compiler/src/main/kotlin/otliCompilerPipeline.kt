package com.monkopedia.otli

import java.nio.file.Paths
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.fileBelongsToModuleForPsi
import org.jetbrains.kotlin.cli.common.fir.FirDiagnosticsCompilerResultsReporter
import org.jetbrains.kotlin.cli.common.isCommonSourceForPsi
import org.jetbrains.kotlin.cli.common.messages.AnalyzerWithCompilerReport
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.common.prepareJsSessions
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.diagnostics.impl.BaseDiagnosticsCollector
import org.jetbrains.kotlin.fir.BinaryModuleData
import org.jetbrains.kotlin.fir.DependencyListForCliModule
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.pipeline.ModuleCompilerAnalyzedOutput
import org.jetbrains.kotlin.fir.pipeline.buildResolveAndCheckFirFromKtFiles
import org.jetbrains.kotlin.fir.pipeline.runPlatformCheckers
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.konan.NativePlatforms
import org.jetbrains.kotlin.psi.KtFile

inline fun <F> compileModuleToAnalyzedFir(
    moduleStructure: ModulesStructure,
    files: List<F>,
    libraries: List<String>,
    friendLibraries: List<String>,
    lookupTracker: LookupTracker?,
    noinline isCommonSource: (F) -> Boolean,
    noinline fileBelongsToModule: (F, String) -> Boolean,
    buildResolveAndCheckFir: (FirSession, List<F>) -> ModuleCompilerAnalyzedOutput
): List<ModuleCompilerAnalyzedOutput> {
    // FIR
    val extensionRegistrars = FirExtensionRegistrar.getInstances(moduleStructure.project)

    val mainModuleName =
        moduleStructure.compilerConfiguration.get(CommonConfigurationKeys.MODULE_NAME)!!
    val escapedMainModuleName = Name.special("<$mainModuleName>")
    val platform = NativePlatforms.unspecifiedNativePlatform
    val binaryModuleData = BinaryModuleData.initialize(escapedMainModuleName, platform)
    val dependencyList =
        DependencyListForCliModule.build(binaryModuleData) {
            dependencies(libraries.map { Paths.get(it).toAbsolutePath() })
            friendDependencies(friendLibraries.map { Paths.get(it).toAbsolutePath() })
            // TODO: !!! dependencies module data?
        }

    val resolvedLibraries = moduleStructure.allDependencies

    val sessionsWithSources =
        prepareJsSessions(
            files,
            moduleStructure.compilerConfiguration,
            escapedMainModuleName,
            resolvedLibraries,
            dependencyList,
            extensionRegistrars,
            isCommonSource = isCommonSource,
            fileBelongsToModule = fileBelongsToModule,
            lookupTracker,
            icData = null
        )

    val outputs =
        sessionsWithSources.map {
            buildResolveAndCheckFir(it.session, it.files)
        }

    return outputs
}

internal fun reportCollectedDiagnostics(
    compilerConfiguration: CompilerConfiguration,
    diagnosticsReporter: BaseDiagnosticsCollector,
    messageCollector: MessageCollector
) {
    val renderName =
        compilerConfiguration.getBoolean(CLIConfigurationKeys.RENDER_DIAGNOSTIC_INTERNAL_NAME)
    FirDiagnosticsCompilerResultsReporter.reportToMessageCollector(
        diagnosticsReporter,
        messageCollector,
        renderName
    )
}

open class AnalyzedFirOutput {
    protected open fun checkSyntaxErrors(messageCollector: MessageCollector) = false

    fun reportCompilationErrors(
        moduleStructure: ModulesStructure,
        diagnosticsReporter: BaseDiagnosticsCollector,
        messageCollector: MessageCollector
    ): Boolean {
        if (checkSyntaxErrors(messageCollector) || diagnosticsReporter.hasErrors) {
            reportCollectedDiagnostics(
                moduleStructure.compilerConfiguration,
                diagnosticsReporter,
                messageCollector
            )
            return true
        }

        return false
    }
}

class AnalyzedFirWithPsiOutput(private val compiledFiles: List<KtFile>) : AnalyzedFirOutput() {
    override fun checkSyntaxErrors(messageCollector: MessageCollector): Boolean =
        compiledFiles.fold(false) { errorsFound, file ->
            AnalyzerWithCompilerReport
                .reportSyntaxErrors(
                    file,
                    messageCollector
                ).isHasErrors or errorsFound
        }
}

fun compileModuleToAnalyzedFirWithPsi(
    moduleStructure: ModulesStructure,
    ktFiles: List<KtFile>,
    libraries: List<String>,
    friendLibraries: List<String>,
    diagnosticsReporter: BaseDiagnosticsCollector,
    lookupTracker: LookupTracker?
): AnalyzedFirWithPsiOutput {
    val output =
        compileModuleToAnalyzedFir(
            moduleStructure,
            ktFiles,
            libraries,
            friendLibraries,
            lookupTracker,
            isCommonSource = isCommonSourceForPsi,
            fileBelongsToModule = fileBelongsToModuleForPsi,
            buildResolveAndCheckFir = { session, files ->
                buildResolveAndCheckFirFromKtFiles(session, files, diagnosticsReporter)
            }
        )
    output.runPlatformCheckers(diagnosticsReporter)
    return AnalyzedFirWithPsiOutput(ktFiles)
}
