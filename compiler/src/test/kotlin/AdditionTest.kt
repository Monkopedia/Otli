package com.monkopedia.otli

import com.monkopedia.otli.builders.CCodeBuilder
import com.monkopedia.otli.codegen.CodegenVisitor
import java.io.File
import kotlin.test.Test

class AdditionTest {

    @Test
    fun `addition test`() {
        println("Exec: ${File(".").absolutePath}")
        val ir = OtliCompiler().compileCode("""
            val x = 2
            val y = 3
            val z = x + y
        """.trimIndent())
        ir?.accept(MyVisitor(), Unit)
        val builder = CCodeBuilder()
        ir?.accept(CodegenVisitor(), builder)
        println(builder.toString())
    }
}
