package com.monkopedia.otli.codegen

import com.monkopedia.otli.builders.CodeBuilder
import com.monkopedia.otli.builders.InlineArrayDefinition
import com.monkopedia.otli.builders.Symbol
import com.monkopedia.otli.builders.op
import com.monkopedia.otli.builders.reference
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.properties

fun CodegenVisitor.buildConstructor(expression: IrConstructorCall, data: CodeBuilder): Symbol {
    val type = expression.type
    val arguments = expression.arguments
    return buildConstructor(type, expression.symbol.owner.parameters, arguments, data)
}

fun CodegenVisitor.buildConstructor(
    type: IrType,
    parameters: List<IrValueParameter>,
    arguments: List<IrExpression?>,
    data: CodeBuilder
): Symbol {
    when (type.classFqName?.asString()) {
        "kotlin.ranges.IntRange" -> return InlineArrayDefinition(
            arguments[0]!!.accept(this, data),
            arguments[1]!!.accept(this, data)
        )
    }
    val cls = type.classOrNull?.owner
    if (cls?.isData == true) {
        return buildDataConstructor(cls, parameters, arguments, data)
    }
    error("Unsupported constructor ${type.classFqName}")
}

fun CodegenVisitor.buildDataConstructor(
    cls: IrClass,
    parameters: List<IrValueParameter>,
    arguments: List<IrExpression?>,
    data: CodeBuilder
): Symbol = InlineArrayDefinition(
    *parameters.zip(arguments).map { (parameter, argument) ->
        val property = cls.properties.find { it.name == parameter.name }
            ?: error("Can't find property ${parameter.name}")
        declarationLookup[property!!.backingField!!]!!.reference.op(
            ":",
            argument?.accept(this, data)
                ?: parameter.defaultValue?.accept(this, data)
                ?: error("Missing argument in ${parameter.type} ${parameter.name} $argument")
        )
    }.toTypedArray()
)
