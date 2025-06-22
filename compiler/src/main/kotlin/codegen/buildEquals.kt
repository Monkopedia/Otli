@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package com.monkopedia.otli.codegen

import com.monkopedia.otli.builders.Call
import com.monkopedia.otli.builders.CodeBuilder
import com.monkopedia.otli.builders.ResolvedType
import com.monkopedia.otli.builders.Symbol
import com.monkopedia.otli.builders.op
import com.monkopedia.otli.type.canConvert
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.functions

fun CodegenVisitor.buildEquals(
    data: CodeBuilder,
    first: IrExpression,
    second: IrExpression
): Symbol {
    val firstIrType = first.type
    val secondIrType = second.type
    val firstSym = first.accept(this, data)
    val secondSym = second.accept(this, data)
    return buildEquals(firstIrType, secondIrType, firstSym, secondSym)
}

fun buildEquals(
    firstIrType: IrType,
    secondIrType: IrType,
    firstSym: Symbol,
    secondSym: Symbol
): Symbol {
    val firstType = ResolvedType(firstIrType)
    val secondType = ResolvedType(secondIrType)
    if (firstType != secondType) {
        if (!firstType.isNative || !firstType.canConvert(secondType)) {
            error("Comparing $firstType and $secondType is not supported in otli")
        }
    }
    if (firstType.isNative) {
        return firstSym.op("==", secondSym)
    }
    val eqMethod = firstIrType.getClass()?.functions?.find {
        it.name.asString() == "equals" && it.parameters.size == 1 &&
            it.parameters.single().type == firstIrType
    } ?: error("No equals found for $firstType")
    return Call(methodName(eqMethod), firstSym, secondSym)
}
