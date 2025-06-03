package com.monkopedia.otli

import java.io.File

fun main(args: Array<String>) {
    val compiler = OtliCompiler()
    println("Hello there")
    val testFile = File("test.kt")
    testFile.writeText(
        """
        val x = 2
        val y = 3
        val z = x + y
        """.trimIndent()
    )
    compiler.exec(
        System.err,
        "-kotlin-home",
        "/usr/share/kotlin",
        "-ir-output-dir",
        "ir",
        "-ir-output-name",
        "test",
        testFile.absolutePath,
        "-libraries=compiler/klibs/kotlin-stdlib-js-2.1.21.klib"
    )
//    compiler.exec(System.err, "-help")
    println("Donezo")
}
