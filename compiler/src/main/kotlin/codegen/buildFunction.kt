package com.monkopedia.otli.codegen

import com.monkopedia.otli.builders.BlockSymbol
import com.monkopedia.otli.builders.CodeBuilder
import com.monkopedia.otli.builders.LangFactory
import com.monkopedia.otli.builders.ResolvedType
import com.monkopedia.otli.builders.Symbol
import com.monkopedia.otli.builders.function
import com.monkopedia.otli.builders.functionDeclaration
import com.monkopedia.otli.builders.type
import com.monkopedia.otli.type.WrappedType
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI


@OptIn(UnsafeDuringIrConstructionAPI::class)
fun <T : LangFactory> CodegenVisitor<T>.buildFunction(
    expression: IrFunction,
    data: CodeBuilder<T>
): Symbol {
    if (expression.extensionReceiverParameter != null) {
        error("Extension receivers are not yet supported.")
    }
    if (expression.dispatchReceiverParameter != null) {
        error("Dispatch receivers are not yet supported.")
    }

    return data.function {
        this.name = expression.symbol.owner.name.asString()
        this.retType = data.type(ResolvedType(expression.returnType))
        expression.parameters.map { arg ->
            define(arg.name.asString(), ResolvedType(arg.type)).also {
                declarationLookup[arg] = it
            }
        }
        body {
            val symbol = expression.body?.accept(this@buildFunction, data)
                ?: error("No body on function declaration")
            if (symbol is BlockSymbol<*>) {
                symbol.symbols.forEach { addSymbol(it) }
            } else {
                addSymbol(symbol)
            }
        }
    }
}
