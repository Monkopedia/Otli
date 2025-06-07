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
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.isPropertyAccessor
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStringConcatenation
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.getPackageFragment

@OptIn(UnsafeDuringIrConstructionAPI::class)
fun <T : LangFactory> CodegenVisitor<T>.buildCall(
    expression: IrCall,
    data: CodeBuilder<T>
): Symbol {
    val owner = expression.symbol.owner
    return buildCall(
        owner.name.asString(),
        expression.arguments,
        data,
        owner.returnType,
        owner.getPackageFragment().packageFqName.asString(),
        owner.isOperator,
        owner.isPropertyAccessor,
        expression.extensionReceiver ?: expression.dispatchReceiver,
        owner.correspondingPropertySymbol?.owner
    )
}

fun <T : LangFactory> CodegenVisitor<T>.buildCall(
    name: String,
    arguments: List<IrExpression?>,
    data: CodeBuilder<T>,
    returnType: IrType? = null,
    pkg: String = "",
    isOperator: Boolean = false,
    isPropertyAccessor: Boolean = false,
    receiver: IrExpression? = null,
    correspondingProperty: IrProperty? = null
): Symbol {
    if (isOperator) {
        return when (name) {
            "plus" -> operatorSymbol(arguments, data, "+")
            "minus" -> operatorSymbol(arguments, data, "-")
            "times" -> operatorSymbol(arguments, data, "*")
            "div" -> operatorSymbol(arguments, data, "/")

            else -> error("Unsupported operator: $name")
        }
    }
    if (pkg.startsWith("kotlin")) {
        return when (name) {
            "toByte",
            "toShort",
            "toInt",
            "toLong",
            "toUByte",
            "toUShort",
            "toUInt",
            "toULong" -> RawCast(
                ResolvedType(returnType ?: error("stdlib method missing return")).toString(),
                receiver?.accept(this, data)
                    ?: error("Missing receiver")
            )

            "println" -> Call(
                "printf",
                *convertArgs(
                    arguments.singleOrNull() as? IrStringConcatenation
                        ?: error("Wrong argument"),
                    data
                )
            )

            else -> error("Unhandled stdlib method $pkg.$name")
        }
    }
    if (isPropertyAccessor) {
        val symbol = correspondingProperty?.let { declarationLookup[it] }
            ?: error("Cannot find declaration for $correspondingProperty")
        return Reference(symbol)
    }
    return Call(
        name,
        *arguments.map { it?.accept(this, data) ?: error("Missing argument") }
            .toTypedArray()
    )
}

private fun <T : LangFactory> CodegenVisitor<T>.operatorSymbol(
    arguments: List<IrExpression?>,
    data: CodeBuilder<T>,
    operand: String
): Symbol {
    arguments.takeIf { it.size == 2 }?.takeIf {
        val type1 = ResolvedType(it[0]?.type ?: return@takeIf false)
        val type2 = ResolvedType(it[1]?.type ?: return@takeIf false)
        type1.isNative && type2.isNative
    } ?: error("Can't handle $arguments")
    val symbol1 = arguments[0]?.accept(this, data)
        ?: error("Lost child during resolution")
    val symbol2 = arguments[1]?.accept(this, data)
        ?: error("Lost child during resolution")
    return Parens(symbol1.op(operand, symbol2))
}
