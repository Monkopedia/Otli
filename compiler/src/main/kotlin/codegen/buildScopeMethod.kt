package com.monkopedia.otli.codegen

import com.monkopedia.otli.builders.Call
import com.monkopedia.otli.builders.CodeBuilder
import com.monkopedia.otli.builders.Included
import com.monkopedia.otli.builders.ResolvedType
import com.monkopedia.otli.builders.Return
import com.monkopedia.otli.builders.Symbol
import com.monkopedia.otli.builders.captureChildren
import com.monkopedia.otli.builders.define
import com.monkopedia.otli.builders.op
import com.monkopedia.otli.builders.reference
import com.monkopedia.otli.builders.scopeBlock
import com.monkopedia.otli.builders.type
import com.monkopedia.otli.type.WrappedType
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression

fun CodegenVisitor.buildLet(
    expression: IrCall,
    arguments: List<IrExpression?>,
    data: CodeBuilder
): Symbol {
    val retType = ResolvedType(expression.typeArguments[1] ?: error("Missing R argument for let"))
    if (retType == WrappedType("void")) {
        return buildLetUnit(expression, arguments, data)
    }
    val value = arguments[0]?.accept(this, data)
        ?: error("Missing receiver for let call")
    val arg = arguments[1] as? IrFunctionExpression
        ?: error("First argument to let must be a function expression")
    val inType = ResolvedType(expression.typeArguments[0] ?: error("Missing T argument for let"))
    val ret = data.define(expression, "letRet", retType)
    val args = arg.function.parameters.map {
        data.define(it to expression, it.name.asString(), ResolvedType(it.type)).also { v ->
            declarationLookup[it] = v
        }.reference
    }

    data.addSymbol(
        Call(
            Included("OTLI_LET", "OtliScopes.h", false),
            type(inType),
            type(retType),
            *args.toTypedArray(),
            ret.reference,
            value,
            data.scopeBlock {
                val builder = this.captureChildren { symbol ->
                    if (symbol is Return) {
                        this@scopeBlock.add(ret.reference.op("=", symbol.symbols.single()))
                    } else {
                        this@scopeBlock.add(symbol)
                    }
                }
                arg.function.body?.accept(this@buildLet, builder)?.let(builder::add)
            }
        )
    )
    return ret.reference
}

private fun CodegenVisitor.buildLetUnit(
    expression: IrCall,
    arguments: List<IrExpression?>,
    data: CodeBuilder
): Symbol {
    val value = arguments[0]?.accept(this, data)
        ?: error("Missing receiver for let call")
    val arg = arguments[1] as? IrFunctionExpression
        ?: error("First argument to let must be a function expression")
    val inType = ResolvedType(expression.typeArguments[0] ?: error("Missing T argument for let"))
    val args = arg.function.parameters.map {
        data.define(it to expression, it.name.asString(), ResolvedType(it.type)).also { v ->
            declarationLookup[it] = v
        }.reference
    }

    data.addSymbol(
        Call(
            Included("OTLI_LET_UNIT", "OtliScopes.h", false),
            type(inType),
            *args.toTypedArray(),
            value,
            data.scopeBlock {
                arg.function.body?.accept(this@buildLetUnit, this)?.let(::add)
            }
        )
    )
    return type(WrappedType("void"))
}

fun CodegenVisitor.buildRun(
    expression: IrCall,
    arguments: List<IrExpression?>,
    data: CodeBuilder
): Symbol {
    val retType = ResolvedType(expression.typeArguments[1] ?: error("Missing R argument for let"))
    if (retType == WrappedType("void")) {
        return buildRunUnit(expression, arguments, data)
    }
    val value = arguments[0]?.accept(this, data)
        ?: error("Missing receiver for let call")
    val arg = arguments[1] as? IrFunctionExpression
        ?: error("First argument to let must be a function expression")
    val inType = ResolvedType(expression.typeArguments[0] ?: error("Missing T argument for let"))
    val ret = data.define(expression, "runRet", retType)
    val args = arg.function.parameters.map {
        data.define(it to expression, it.name.asString(), ResolvedType(it.type)).also { v ->
            declarationLookup[it] = v
        }.reference
    }

    data.addSymbol(
        Call(
            Included("OTLI_LET", "OtliScopes.h", false),
            type(inType),
            type(retType),
            *args.toTypedArray(),
            ret.reference,
            value,
            data.scopeBlock {
                val builder = this.captureChildren { symbol ->
                    if (symbol is Return) {
                        this@scopeBlock.add(ret.reference.op("=", symbol.symbols.single()))
                    } else {
                        this@scopeBlock.add(symbol)
                    }
                }
                arg.function.body?.accept(this@buildRun, builder)?.let(builder::add)
            }
        )
    )
    return ret.reference
}

private fun CodegenVisitor.buildRunUnit(
    expression: IrCall,
    arguments: List<IrExpression?>,
    data: CodeBuilder
): Symbol {
    val value = arguments[0]?.accept(this, data)
        ?: error("Missing receiver for let call")
    val arg = arguments[1] as? IrFunctionExpression
        ?: error("First argument to let must be a function expression")
    val inType = ResolvedType(expression.typeArguments[0] ?: error("Missing T argument for let"))
    val args = arg.function.parameters.map {
        data.define(it to expression, it.name.asString(), ResolvedType(it.type)).also { v ->
            declarationLookup[it] = v
        }.reference
    }

    data.addSymbol(
        Call(
            Included("OTLI_LET_UNIT", "OtliScopes.h", false),
            type(inType),
            *args.toTypedArray(),
            value,
            data.scopeBlock {
                arg.function.body?.accept(this@buildRunUnit, this)?.let(::add)
            }
        )
    )
    return type(WrappedType("void"))
}
