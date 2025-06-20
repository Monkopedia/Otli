@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package com.monkopedia.otli.codegen

import com.monkopedia.otli.builders.BlockSymbol
import com.monkopedia.otli.builders.CodeBuilder
import com.monkopedia.otli.builders.FunctionBuilder
import com.monkopedia.otli.builders.Included
import com.monkopedia.otli.builders.ResolvedType
import com.monkopedia.otli.builders.Symbol
import com.monkopedia.otli.builders.function
import com.monkopedia.otli.builders.functionDeclaration
import com.monkopedia.otli.builders.type
import org.jetbrains.kotlin.ir.backend.js.utils.isDispatchReceiver
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.fqNameForIrSerialization

fun CodegenVisitor.buildFunction(expression: IrFunction, data: CodeBuilder): Symbol {
    if (expression.extensionReceiverParameter != null) {
        error("Extension receivers are not yet supported.")
    }

    return data.function {
        buildFunction(this, expression, this@buildFunction)
    }
}

fun buildFunctionDeclaration(expression: IrFunction, data: CodeBuilder): Symbol {
    return data.functionDeclaration {
        buildFunction(this, expression, null)
    }
}

private fun buildFunction(
    builder: FunctionBuilder,
    expression: IrFunction,
    visitor: CodegenVisitor?
) {
    builder.name = methodName(expression)
    builder.retType = type(ResolvedType(expression.returnType))
    expression.parameters.filter { !it.isDispatchReceiver }.map { arg ->
        builder.define(arg, arg.name.asString(), ResolvedType(arg.type)).also {
            visitor?.declarationLookup[arg] = it
        }
    }
    builder.body {
        val symbol = expression.body?.accept(visitor ?: return@body, this@body)
            ?: error("No body on function declaration")
        if (symbol is BlockSymbol) {
            symbol.symbols.forEach { addSymbol(it) }
        } else {
            addSymbol(symbol)
        }
    }
}

fun methodName(expression: IrFunction, suffix: String = ""): Symbol {
    val prefix = expression.parent.fqNameForIrSerialization.asString().replace(".", "_")
        .takeIf { it.isNotEmpty() }?.plus("_") ?: ""
    val methodName = prefix + expression.symbol.owner.name.asString() + suffix
    return Included(methodName, headerName(expression.file), false)
}
