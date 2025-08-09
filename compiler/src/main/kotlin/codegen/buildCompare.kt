package com.monkopedia.otli.codegen

import com.monkopedia.otli.builders.CodeBuilder
import com.monkopedia.otli.builders.ResolvedType
import com.monkopedia.otli.builders.Symbol
import com.monkopedia.otli.builders.op
import com.monkopedia.otli.type.canConvert
import org.jetbrains.kotlin.ir.expressions.IrExpression

fun CodegenVisitor.buildLess(arguments: List<IrExpression?>, data: CodeBuilder): Symbol {
    val (firstSym, secondSym) = acceptCompare(arguments, "less", data)
    return firstSym.op("<", secondSym)
}

fun CodegenVisitor.buildGreater(arguments: List<IrExpression?>, data: CodeBuilder): Symbol {
    val (firstSym, secondSym) = acceptCompare(arguments, "greater", data)
    return firstSym.op(">", secondSym)
}
fun CodegenVisitor.buildLessOrEqual(arguments: List<IrExpression?>, data: CodeBuilder): Symbol {
    val (firstSym, secondSym) = acceptCompare(arguments, "lessOrEqual", data)
    return firstSym.op("<=", secondSym)
}

fun CodegenVisitor.buildGreaterOrEqual(arguments: List<IrExpression?>, data: CodeBuilder): Symbol {
    val (firstSym, secondSym) = acceptCompare(arguments, "greaterOrEqual", data)
    return firstSym.op(">=", secondSym)
}

private fun CodegenVisitor.acceptCompare(
    arguments: List<IrExpression?>,
    method: String,
    data: CodeBuilder
): Pair<Symbol, Symbol> {
    val firstArg = arguments.getOrNull(0) ?: error("Missing first argument for $method")
    val secondArg = arguments.getOrNull(1) ?: error("Missing second argument for $method")
    val firstType = ResolvedType(firstArg.type)
    val secondType = ResolvedType(secondArg.type)
    if (!firstType.isNative && !secondType.isNative) {
        error("Non-native comparisons not supported yet")
    }
    if (firstType != secondType && !firstType.canConvert(secondType)) {
        error("Comparing $firstType and $secondType is not supported in otli")
    }
    val firstSym = firstArg.accept(this, data)
    val secondSym = secondArg.accept(this, data)
    return Pair(firstSym, secondSym)
}
