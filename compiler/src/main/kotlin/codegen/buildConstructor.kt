package com.monkopedia.otli.codegen

import com.monkopedia.otli.builders.CodeBuilder
import com.monkopedia.otli.builders.InlineArrayDefinition
import com.monkopedia.otli.builders.Symbol
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.types.classFqName

fun CodegenVisitor.buildConstructor(expression: IrConstructorCall, data: CodeBuilder): Symbol {
    val type = expression.type
    when (type.classFqName?.asString()) {
        "kotlin.ranges.IntRange" -> return InlineArrayDefinition(
            expression.arguments[0]!!.accept(this, data),
            expression.arguments[1]!!.accept(this, data)
        )
    }
    error("Unsupported constructor ${type.classFqName}")
}
