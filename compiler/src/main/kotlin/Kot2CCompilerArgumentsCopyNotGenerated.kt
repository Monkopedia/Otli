package com.monkopedia.kot

import org.jetbrains.kotlin.cli.common.arguments.copyCommonKlibBasedCompilerArguments

fun copyKot2CCompilerArguments(
    from: Kot2CCompilerArguments,
    to: Kot2CCompilerArguments
): Kot2CCompilerArguments {
    copyCommonKlibBasedCompilerArguments(from, to)

    to.friendModules = from.friendModules
    to.includes = from.includes
    to.irModuleName = from.irModuleName
    to.libraries = from.libraries
    to.moduleName = from.moduleName
    to.outputDir = from.outputDir

    return to
}
