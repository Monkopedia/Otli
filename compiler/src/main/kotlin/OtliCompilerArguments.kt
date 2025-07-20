package com.monkopedia.otli

import com.intellij.util.xmlb.annotations.Transient
import kotlin.collections.set
import org.jetbrains.kotlin.cli.common.arguments.Argument
import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArgumentsConfigurator
import org.jetbrains.kotlin.cli.common.arguments.CommonKlibBasedCompilerArguments
import org.jetbrains.kotlin.cli.common.arguments.CommonKlibBasedCompilerArgumentsConfigurator
import org.jetbrains.kotlin.cli.common.arguments.DefaultValue
import org.jetbrains.kotlin.cli.common.arguments.Freezable
import org.jetbrains.kotlin.cli.common.arguments.GradleInputTypes
import org.jetbrains.kotlin.cli.common.arguments.GradleOption
import org.jetbrains.kotlin.cli.common.arguments.K2JSCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.AnalysisFlag
import org.jetbrains.kotlin.config.AnalysisFlags.allowFullyQualifiedNameInKClass
import org.jetbrains.kotlin.config.ApiVersion
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.config.LanguageVersionSettings

class OtliCompilerArguments : CommonKlibBasedCompilerArguments() {
    companion object {
        @JvmStatic
        private val serialVersionUID = 0L
    }

    @Argument(
        value = "-ir-output-dir",
        valueDescription = "<directory>",
        description = "Destination for generated files."
    )
    var outputDir: String? = null
        set(value) {
            checkFrozen()
            field = if (value.isNullOrEmpty()) null else value
        }

    @GradleOption(
        value = DefaultValue.STRING_NULL_DEFAULT,
        gradleInputType = GradleInputTypes.INPUT,
        shouldGenerateDeprecatedKotlinOptions = true
    )
    @Argument(value = "-ir-output-name", description = "Base name of generated files.")
    var moduleName: String? = null
        set(value) {
            checkFrozen()
            field = if (value.isNullOrEmpty()) null else value
        }

    @Argument(
        value = "-libraries",
        valueDescription = "<path>",
        description = "Paths to Kotlin libraries with .klib files, " +
            "separated by the system path separator."
    )
    var libraries: String? = null
        set(value) {
            checkFrozen()
            field = if (value.isNullOrEmpty()) null else value
        }

    @Argument(
        value = "-interop",
        valueDescription = "<path>",
        description = "Path to an interop definition, " +
            "which will be output as a klib."
    )
    var interop: String? = null
        set(value) {
            checkFrozen()
            field = if (value.isNullOrEmpty()) null else value
        }

    // Advanced options

    @Argument(
        value = "-Xir-module-name",
        valueDescription = "<name>",
        description = "Specify the name of the compilation module for the IR backend."
    )
    var irModuleName: String? = null
        set(value) {
            checkFrozen()
            field = if (value.isNullOrEmpty()) null else value
        }

    @Argument(
        value = "-Xinclude",
        valueDescription = "<path>",
        description = "Path to an intermediate library that should be processed " +
            "in the same manner as source files."
    )
    var includes: String? = null
        set(value) {
            checkFrozen()
            field = if (value.isNullOrEmpty()) null else value
        }

    @Argument(
        value = "-Xfriend-modules",
        valueDescription = "<path>",
        description = "Paths to friend modules."
    )
    var friendModules: String? = null
        set(value) {
            checkFrozen()
            field = if (value.isNullOrEmpty()) null else value
        }

    @Argument(
        value = "-output-klib",
        description = "Write out a klib file."
    )
    var outputKlib = false
        set(value) {
            checkFrozen()
            field = value
        }

    @get:Transient
    @field:kotlin.jvm.Transient
    override val configurator: CommonCompilerArgumentsConfigurator = OtliCompilerArgumentsConfigurator()

    override fun copyOf(): Freezable = copyOtliCompilerArguments(this, OtliCompilerArguments())
}

class OtliCompilerArgumentsConfigurator : CommonKlibBasedCompilerArgumentsConfigurator() {
    override fun configureAnalysisFlags(
        arguments: CommonCompilerArguments,
        collector: MessageCollector,
        languageVersion: LanguageVersion
    ): MutableMap<AnalysisFlag<*>, Any> =
        super.configureAnalysisFlags(arguments, collector, languageVersion).also {
            it[allowFullyQualifiedNameInKClass] = false
        }

    override fun configureExtraLanguageFeatures(
        arguments: CommonCompilerArguments,
        map: HashMap<LanguageFeature, LanguageFeature.State>
    ) {
        super.configureExtraLanguageFeatures(arguments, map)
    }

}

