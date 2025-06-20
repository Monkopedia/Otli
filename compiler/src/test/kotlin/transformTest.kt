package com.monkopedia.otli

import com.monkopedia.otli.builders.CCodeBuilder
import com.monkopedia.otli.codegen.CodegenVisitor
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createTempFile
import kotlin.test.assertEquals

fun transformTest(
    otliCode: String,
    expected: String,
    file: Path? = null,
    verification: (Map<String, String>) -> Unit = {
        val generated = it.filter { it.key.endsWith(".c") }.values.first().let {
            if (it.startsWith("#include")) it.split("\n").drop(1).joinToString("\n") else it
        }
        assertEquals(expected, generated)
    }
) {
    val ir = OtliCompiler().compileCode(otliCode, file = file)
    val builder = CCodeBuilder()
    ir?.accept(CodegenVisitor(), builder)?.let { builder.addSymbol(it) }
    verification(builder.files())
}
