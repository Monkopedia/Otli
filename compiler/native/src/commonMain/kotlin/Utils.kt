@file:OptIn(ExperimentalForeignApi::class)

package com.monkopedia.otli.clang


import clang.CXCursor
import clang.CXCursorKind
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.Serializable
import platform.posix.fflush
import platform.posix.fprintf

object Utils {
    val STDERR = platform.posix.fdopen(2, "w")

    fun printerrln(message: String) {
        fprintf(STDERR, message + "\n")
        fflush(STDERR)
    }

    @Serializable
    data class CursorTreeInfo(
        val spelling: String?,
        val type: String?,
        val usr: String?,
        val visibility: String?,
        val availability: String?,
        val kind: CXCursorKind,
        val file: String?,
//        val prettyPrint: String,
        val children: List<CursorTreeInfo>
    ) {
        constructor(cursor: CValue<CXCursor>) : this(
            cursor.spelling.toKString() ?: "UKN",
            cursor.type.spelling.toKString() ?: "UKN",
            cursor.usr.toKString() ?: "NOUSR",
            cursor.accessSpecifier.toString(),
            cursor.availability.toString(),
            cursor.kind,
            cursor.extend.getStartLocation().file?.getPath(),
//            cursor.prettyPrinted.toKString() ?: "NOINFO",
            cursor.mapChildren { CursorTreeInfo(it) },
        )
    }
}
