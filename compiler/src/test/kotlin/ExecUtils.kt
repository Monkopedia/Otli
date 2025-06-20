package com.monkopedia.otli

import com.intellij.openapi.util.Disposer
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText
import org.jetbrains.kotlin.cli.common.CLICompiler
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.computeKotlinPaths
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.common.setupCommonArguments
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.Services
import org.jetbrains.kotlin.config.messageCollector
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

fun OtliCompiler.compileCode(code: String, file: Path? = null): IrModuleFragment? {
    val arguments = OtliCompilerArguments()
    val file = file ?: createTempFile(suffix = ".kt")
    file.writeText(code)
    arguments.kotlinHome = "/usr/share/kotlin"
    arguments.outputDir = "ir"
    arguments.moduleName = "test_module"
    arguments.libraries = "../otli-stdlib/build/otli-stdlib.klib${File.pathSeparator}" +
        "../otli-test/build/otli-test.klib"
    arguments.freeArgs = listOf(file.absolutePathString())

    val messageCollector = PrintingMessageCollector(
        System.out,
        MessageRenderer.PLAIN_RELATIVE_PATHS,
        arguments.verbose
    )
    val configuration = CompilerConfiguration()

    configuration.put(CLIConfigurationKeys.ORIGINAL_MESSAGE_COLLECTOR_KEY, messageCollector)
    configuration.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, messageCollector)

    configuration.setupCommonArguments(arguments, this::createMetadataVersion)
    setupPlatformSpecificArgumentsAndServices(configuration, arguments, Services.EMPTY)
    val paths = computeKotlinPaths(messageCollector, arguments)
    if (messageCollector.hasErrors()) {
        throw Exception("Collector found errors")
    }

    val rootDisposable =
        Disposer.newDisposable("Disposable for ${CLICompiler::class.simpleName}.compileCode")

    try {
        return compileToIr(arguments, configuration, rootDisposable, paths)
    } catch (t: Throwable) {
        t.printStackTrace()
        return null
    }
}
