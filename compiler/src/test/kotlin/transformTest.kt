package com.monkopedia.otli

import com.monkopedia.otli.builders.CCodeBuilder
import com.monkopedia.otli.codegen.CodegenVisitor
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createTempFile
import kotlin.test.assertEquals
import org.jetbrains.kotlin.ir.util.DumpIrTreeVisitor

fun transformTest(
    otliCode: String,
    expected: String,
    file: Path? = null,
    verification: (Map<String, String>) -> Unit = {
        val generated = it.filter { it.key.endsWith(".c") }.entries.first().let { (key, text) ->
            if (file == null) {
                text.split("\n").filter {
                    !it.startsWith("#include \"${key.replace(".c", ".h")}")
                }.joinToString("\n")
            } else {
                text
            }
        }
        assertEquals(expected, generated)
    }
) {
    val ir = OtliCompiler().compileCode(otliCode, file = file)
    println(
        buildString {
            ir?.accept(DumpIrTreeVisitor(this), "")
        }
    )
    val builder = CCodeBuilder()
    ir?.accept(CodegenVisitor(), builder)?.let { builder.addSymbol(it) }
    verification(builder.files())
}
