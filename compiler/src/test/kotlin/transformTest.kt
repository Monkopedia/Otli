package com.monkopedia.otli

import com.monkopedia.otli.builders.CCodeBuilder
import com.monkopedia.otli.codegen.CodegenVisitor
import kotlin.test.assertEquals

fun transformTest(otliCode: String, expected: String) {
    val ir = OtliCompiler().compileCode(otliCode)
    val builder = CCodeBuilder()
    ir?.accept(CodegenVisitor(), builder)?.let { builder.addSymbol(it) }
    val generated = builder.files().values.first()
    assertEquals(expected, generated)
}
