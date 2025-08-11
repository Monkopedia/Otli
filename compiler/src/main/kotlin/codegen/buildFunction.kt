@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package com.monkopedia.otli.codegen

import com.monkopedia.otli.builders.BlockSymbol
import com.monkopedia.otli.builders.CodeBuilder
import com.monkopedia.otli.builders.FunctionBuilder
import com.monkopedia.otli.builders.GroupSymbol
import com.monkopedia.otli.builders.Included
import com.monkopedia.otli.builders.ResolvedType
import com.monkopedia.otli.builders.Symbol
import com.monkopedia.otli.builders.function
import com.monkopedia.otli.builders.type
import com.monkopedia.otli.type.WrappedType.Companion.pointerTo
import org.jetbrains.kotlin.ir.backend.js.utils.isDispatchReceiver
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.fqNameForIrSerialization

fun buildFunction(
    expression: IrFunction,
    data: CodeBuilder,
    visitor: CodegenVisitor?,
    isHeader: Boolean = false
): Symbol {
    if (expression.parameters.any { it.kind == IrParameterKind.ExtensionReceiver }) {
        error("Extension receivers are not yet supported.")
    }

    return data.function(isHeader = isHeader) {
        buildFunction(this, expression, visitor)
    }
}

private fun buildFunction(
    builder: FunctionBuilder,
    expression: IrFunction,
    visitor: CodegenVisitor?
) {
    builder.name = methodName(expression)
    builder.retType = type(ResolvedType(expression.returnType))
    val disptachReceiverType = expression.dispatchReceiverParameter?.type
    if (disptachReceiverType != null &&
        disptachReceiverType.classOrNull?.owner?.isTestClass == false
    ) {
        val thiz = builder.define(
            expression.dispatchReceiverParameter to "thiz",
            "thiz",
            pointerTo(ResolvedType(disptachReceiverType))
        )
        visitor?.withThisScope(thiz) {
            buildFunctionInner(expression, builder, visitor)
        }
        return
    }
    buildFunctionInner(expression, builder, visitor)
}

private fun buildFunctionInner(
    expression: IrFunction,
    builder: FunctionBuilder,
    visitor: CodegenVisitor?
) {
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
        } else if (symbol is GroupSymbol) {
            symbol.symbolList.forEach { addSymbol(it) }
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
