@file:OptIn(ExperimentalForeignApi::class)

package com.monkopedia.otli.clang

import clang.CXIndex
import clang.CXTranslationUnit
import clang.clang_defaultDiagnosticDisplayOptions
import clang.clang_disposeString
import clang.clang_formatDiagnostic
import clang.clang_getCString
import clang.clang_getDiagnostic
import clang.clang_getNumDiagnostics
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.DeferScope
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.set
import kotlinx.cinterop.toKString
import kotlinx.cinterop.toKStringFromUtf8
import kotlinx.serialization.json.Json
import platform.posix.EOF
import platform.posix.close
import platform.posix.getenv
import platform.posix.read
import platform.posix.system
import platform.posix.write

fun generateIncludes(compiler: String) = memScoped {
    val emptyFile = File("/tmp/clang_includes.c")
    emptyFile.writeText("")
    val process =
        Process {
            system("$compiler -E -x c++ -v ${emptyFile.path}")
        }
    process.start()
    defer {
        process.kill()
    }
    val buffer =
        alloc<ByteVar> {
            EOF
        }
    write(process.stdIn(), buffer.ptr, 1.convert())
    close(process.stdIn())
    process.wait()
    val readBuffer = allocArray<ByteVar>(256)
    var fullString = StringBuilder()
    var amount = read(process.stdOut(), readBuffer, 255.convert())
    while (amount > 0) {
        readBuffer[amount.toInt()] = 0.toByte()
        fullString.append(readBuffer.toKStringFromUtf8())
        amount = read(process.stdOut(), readBuffer, 255.convert())
    }
    val lines = fullString.split("\n")
    val start = lines.indexOf("#include <...> search starts here:")
    val end = lines.indexOf("End of search list.")
    if (start < 0 || end < 0) {
        throw IllegalStateException("Can't find includes for:\n$fullString")
    }
    return@memScoped (
        lines.subList(start + 1, end).toList().map { it.trim() } + "."
        ).toTypedArray()
}

fun find(s: String): String? {
    val paths = getenv("PATH")?.toKStringFromUtf8().orEmpty().split(":")
    for (path in paths) {
        val parent = File(path)
        val file = File(parent, s)
        if (file.exists()) {
            return file.path
        }
    }
    return error("Can't find $s in $paths")
}

fun DeferScope.parseHeader(
    index: CXIndex,
    file: List<String>,
    includePaths: Array<String>,
    args: Array<String> =
        arrayOf("-xc++", "--std=c++17") +
            includePaths
                .map { "-I$it" }
                .toTypedArray(),
    debug: Boolean = false
) {
//    val builder = ResolverBuilderImpl()
//    val tu =
        file.forEach { parseHeader(index, it, includePaths, args, debug) }
//            .reduceRight { tu1, tu2 ->
//                tu1.also {
//                    it.addAllChildren(
//                        tu2.children.map {
//                            it.also { it.parent = tu1 }
//                        }
//                    )
//                }
//            }
//    Log.i("Reduced ${tu.children.size}")
//    return ParsedResolver(tu)
}

fun DeferScope.parseHeader(
    index: CXIndex,
    file: String,
    includePaths: Array<String>,
    args: Array<String> =
        arrayOf("-xc++", "--std=c++17") +
            includePaths
                .map { "-I$it" }
                .toTypedArray(),
    debug: Boolean = false
): CXTranslationUnit {
    val tu = index.parseTranslationUnit(file, args, null) ?: error("Failed to parse $file")
    tu.printDiagnostics()?.let {
        throw RuntimeException("Parse failure: $it")
    }
    defer {
        tu.dispose()
    }
    val cursor = tu.cursor
    if (debug) {
        File(
            File("/tmp"),
            "cursor_${File(file).name}.json"
        ).writeText(Json.encodeToString(Utils.CursorTreeInfo(cursor)))
    }
//    val element = WrappedElement.mapAll(tu.cursor, resolverBuilder)
//    return element as? WrappedTU ?: error("$element is not a WrappedTU, ${tu.cursor.kind}")
    return tu
}

fun CXTranslationUnit.printDiagnostics(): String? {
    val nbDiag = clang_getNumDiagnostics(this)
    var foundError = false
    val errorString =
        buildString {
            append("There are $nbDiag diagnostics:")
            append('\n')

            for (currentDiag in 0 until nbDiag.toInt()) {
                val diagnotic = clang_getDiagnostic(this@printDiagnostics, currentDiag.toUInt())
                val errorString =
                    clang_formatDiagnostic(diagnotic, clang_defaultDiagnosticDisplayOptions())
                val str = clang_getCString(errorString)?.toKString()
                clang_disposeString(errorString)
                if (str?.contains("error:") == true) {
                    foundError = true
                }
                append("$str")
                append('\n')
            }
        }
    return if (foundError) {
        errorString
    } else {
        null
    }
}
