package com.monkopedia.otli.codegen

import com.monkopedia.otli.builders.Call
import com.monkopedia.otli.builders.CodeBuilder
import com.monkopedia.otli.builders.LangFactory
import com.monkopedia.otli.builders.Parens
import com.monkopedia.otli.builders.RawCast
import com.monkopedia.otli.builders.Reference
import com.monkopedia.otli.builders.ResolvedType
import com.monkopedia.otli.builders.Symbol
import com.monkopedia.otli.builders.op
import org.jetbrains.kotlin.ir.declarations.isPropertyAccessor
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.getPackageFragment

@OptIn(UnsafeDuringIrConstructionAPI::class)
fun <T : LangFactory> CodegenVisitor<T>.buildCall(
    expression: IrCall,
    data: CodeBuilder<T>
): Symbol {
    val owner = expression.symbol.owner
    if (owner.isOperator) {
        when (owner.name.asString()) {
            "plus" -> return operatorSymbol(expression, data, "+")
            "minus" -> return operatorSymbol(expression, data, "-")
            "times" -> return operatorSymbol(expression, data, "*")
            "div" -> return operatorSymbol(expression, data, "/")

            else -> {
                error("Unsupported operator: ${owner.name.asString()}")
            }
        }
    }
    val pkg = owner.parent.getPackageFragment()?.packageFqName?.asString()
    if (pkg?.startsWith("kotlin") == true) {
        when (owner.name.asString()) {
            "toByte",
            "toShort",
            "toInt",
            "toLong",
            "toUByte",
            "toUShort",
            "toUInt",
            "toULong" -> return RawCast(
                ResolvedType(owner.returnType).toString(),
                (expression.extensionReceiver ?: expression.dispatchReceiver)?.accept(this, data)
                    ?: error("Missing receiver")
            )

            else -> error("Unhandled stdlib method $pkg.${owner.name.asString()}")
        }
    }
    if (owner.isPropertyAccessor) {
        val symbol = owner.correspondingPropertySymbol
            ?.owner?.let { declarationLookup[it] }
            ?: error("Cannot find declaration for ${owner.correspondingPropertySymbol}")
        return Reference(symbol)
    }
    return Call(
        owner.name.asString(),
        *expression.arguments.map { it?.accept(this, data) ?: error("Missing argument") }
            .toTypedArray()
    )
}

private fun <T : LangFactory> CodegenVisitor<T>.operatorSymbol(
    expression: IrCall,
    data: CodeBuilder<T>,
    operand: String
): Symbol {
    expression.arguments.takeIf { it.size == 2 }?.takeIf {
        val type1 = ResolvedType(it[0]?.type ?: return@takeIf false)
        val type2 = ResolvedType(it[1]?.type ?: return@takeIf false)
        type1.isNative && type2.isNative
    } ?: error("Can't handle ${expression.arguments}")
    val symbol1 = expression.arguments[0]?.accept(this, data)
        ?: error("Lost child during resolution")
    val symbol2 = expression.arguments[1]?.accept(this, data)
        ?: error("Lost child during resolution")
    return Parens(symbol1.op(operand, symbol2))
}
