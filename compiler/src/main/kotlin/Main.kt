package com.monkopedia.otli

import com.monkopedia.otli.clang.getClangService
import java.io.File
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    val compiler = OtliCompiler()
    println("Hello there")
    val testFile = File("test.kt")
    runBlocking {
        val service = getClangService()
        println("Got clang service: ${service.hello()}")
    }
//    testFile.writeText(
//        """
//        val x = 2
//        val y = 3
//        val z = x + y
//        """.trimIndent()
//    )
//    compiler.exec(
//        System.err,
//        "-kotlin-home",
//        "/usr/share/kotlin",
//        "-ir-output-dir",
//        "ir",
//        "-output-klib",
//        "-ir-output-name",
//        "test",
//        testFile.absolutePath,
//        "-libraries=otli-stdlib/build/otli-stdlib.klib"
//    )
//    compiler.exec(System.err, "-help")
    println("Donezo")
}
