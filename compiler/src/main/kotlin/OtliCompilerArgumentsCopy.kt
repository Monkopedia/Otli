package com.monkopedia.otli

import org.jetbrains.kotlin.cli.common.arguments.copyCommonKlibBasedCompilerArguments

fun copyOtliCompilerArguments(
    from: OtliCompilerArguments,
    to: OtliCompilerArguments
): OtliCompilerArguments {
    copyCommonKlibBasedCompilerArguments(from, to)

    to.friendModules = from.friendModules
    to.includes = from.includes
    to.irModuleName = from.irModuleName
    to.libraries = from.libraries
    to.moduleName = from.moduleName
    to.outputDir = from.outputDir
    to.outputKlib = from.outputKlib

    return to
}
