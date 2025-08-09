package com.monkopedia.otli.codegen

import com.monkopedia.otli.builders.Call
import com.monkopedia.otli.builders.CodeBuilder
import com.monkopedia.otli.builders.Empty
import com.monkopedia.otli.builders.Included
import com.monkopedia.otli.builders.Index
import com.monkopedia.otli.builders.InlineArrayDefinition
import com.monkopedia.otli.builders.Parens
import com.monkopedia.otli.builders.Raw
import com.monkopedia.otli.builders.RawCast
import com.monkopedia.otli.builders.ResolvedType
import com.monkopedia.otli.builders.Symbol
import com.monkopedia.otli.builders.define
import com.monkopedia.otli.builders.dot
import com.monkopedia.otli.builders.op
import com.monkopedia.otli.builders.reference
import com.monkopedia.otli.type.WrappedType
import com.monkopedia.otli.type.coerce
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.isPropertyAccessor
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStringConcatenation
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.DumpIrTreeVisitor
import org.jetbrains.kotlin.ir.util.getPackageFragment

@OptIn(UnsafeDuringIrConstructionAPI::class)
fun CodegenVisitor.buildCall(expression: IrCall, data: CodeBuilder): Symbol {
    val owner = expression.symbol.owner
    return buildCall(
        expression = expression,
        name = owner.name.asString(),
        arguments = expression.arguments,
        data = data,
        returnType = owner.returnType,
        pkg = owner.getPackageFragment().packageFqName.asString(),
        isOperator = owner.isOperator,
        isPropertyAccessor = owner.isPropertyAccessor,
        receiver = expression.dispatchReceiver,
        correspondingProperty = owner.correspondingPropertySymbol?.owner,
        fullName = owner.functionName(),
        include = owner.include()
    )
}

fun CodegenVisitor.buildCall(
    expression: IrCall?,
    name: String,
    arguments: List<IrExpression?>,
    data: CodeBuilder,
    returnType: IrType? = null,
    pkg: String = "",
    isOperator: Boolean = false,
    isPropertyAccessor: Boolean = false,
    receiver: IrExpression? = null,
    correspondingProperty: IrProperty? = null,
    fullName: String = name,
    include: Symbol? = null
): Symbol {
    if (isOperator) {
        return when (name) {
            "plus" -> operatorSymbol(arguments, data, "+")
            "minus" -> operatorSymbol(arguments, data, "-")
            "times" -> operatorSymbol(arguments, data, "*")
            "div" -> operatorSymbol(arguments, data, "/")
            "inc" -> operation(arguments, data, "+", Raw("1"))
            "dec" -> operation(arguments, data, "-", Raw("1"))
            "iterator" -> buildIterator(data, receiver)
            "hasNext" -> buildHasNext(data, receiver)
            "next" -> buildNext(data, receiver)

            else -> error("Unsupported operator: $name")
        }
    }
    if (receiver?.type?.classOrNull?.owner?.isData == true) {
        when (name) {
            "copy" -> return buildStructCopy(
                expression,
                name,
                arguments,
                data,
                pkg
            )
        }
    }
    if (pkg.startsWith("kotlin")) {
        if (pkg.startsWith("kotlin.test")) {
            return buildTestMethod(
                expression,
                name,
                arguments,
                data,
                pkg
            )
        }
        if (pkg.startsWith("kotlin.ranges")) {
            return buildRangeMethod(receiver, expression, name, arguments, data, returnType, pkg)
        }
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
                (arguments.singleOrNull() ?: receiver)?.accept(this, data)
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

            "repeat" -> Call(
                Included("OTLI_REPEAT", "OtliLoops.h", false),
                arguments.first()?.accept(this@buildCall, data) ?: error("Missing first argument"),
                arguments[1]!!.accept(this@buildCall, data)
            )

            "hashCode" -> {
                when (receiver?.type?.classFqName?.asString()) {
                    "kotlin.Int" -> receiver.accept(this@buildCall, data)
                    "kotlin.Boolean" -> receiver.accept(this@buildCall, data).let { base ->
                        val type = ResolvedType(receiver.type)
                        type.coerce(base, WrappedType("int32_t"))
                    }

                    else -> error("Unsupported type ${receiver?.type?.classFqName}")
                }
            }

            "let" -> buildLet(
                expression ?: error("Cannot build let without IR object"),
                arguments,
                data
            )

            "run" -> buildLet(
                expression ?: error("Cannot build let without IR object"),
                arguments,
                data
            )

            "also" -> buildAlso(
                expression ?: error("Cannot build let without IR object"),
                arguments,
                data
            )
//            "apply" -> buildApply(
//                expression ?: error("Cannot build let without IR object"),
//                arguments,
//                data
//            )

            else -> error("Unhandled stdlib method $pkg.$name")
        }
    }
    if (isPropertyAccessor) {
        val symbol = correspondingProperty?.let {
            declarationLookup[it] ?: it.backingField?.let(declarationLookup::get)
        } ?: error(
            "Cannot find declaration for ${
                buildString {
                    correspondingProperty?.accept(DumpIrTreeVisitor(this), "")
                }
            }"
        )
        val prop = expression?.dispatchReceiver?.accept(this, data)?.dot(symbol.reference)
            ?: symbol.reference
        val arg = if (expression?.dispatchReceiver != null) {
            arguments.getOrNull(1)
        } else {
            arguments.getOrNull(0)
        }
        println("Prop accessor $name $arguments $arg")
        if (name.startsWith("<set-") && arg != null) {
            return prop.op("=", arg.accept(this, data)).also { println("Returning $it") }
        }
        return prop
    }
    if (pkg.startsWith("otli")) {
        return buildOtliMethod(
            receiver,
            expression,
            name,
            arguments,
            data,
            returnType,
            pkg,
            include
        )
    }
    return Call(
        include?.let { Included(fullName, it) } ?: Raw(fullName),
        *arguments.map { it?.accept(this, data) ?: error("Missing argument") }
            .toTypedArray()
    )
}

private fun CodegenVisitor.operation(
    arguments: List<IrExpression?>,
    data: CodeBuilder,
    operand: String,
    other: Symbol
): Symbol {
    arguments.takeIf { it.size == 1 }?.takeIf {
        val type1 = ResolvedType(it[0]?.type ?: return@takeIf false)
        type1.isNative
    } ?: error("Can't handle $arguments")
    val symbol1 = arguments[0]?.accept(this, data)
        ?: error("Lost child during resolution")
    return Parens(symbol1.op(operand, other))
}

private fun CodegenVisitor.operatorSymbol(
    arguments: List<IrExpression?>,
    data: CodeBuilder,
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

private fun CodegenVisitor.buildRangeMethod(
    receiver: IrExpression?,
    exp: IrCall?,
    name: String,
    arguments: List<IrExpression?>,
    data: CodeBuilder,
    returnType: IrType?,
    pkg: String
): Symbol = when (name) {
    "<get-first>",
    "<get-start>" -> Index(
        receiver!!.accept(this@buildRangeMethod, data),
        Raw("0")
    )

    "<get-last>",
    "<get-endInclusive>" -> Index(
        receiver!!.accept(this@buildRangeMethod, data),
        Raw("1")
    )

    "isEmpty" -> {
        val base = receiver!!.accept(this@buildRangeMethod, data)
        Index(base, Raw("0")).op(">", Index(base, Raw("1")))
    }

    "until" -> {
        InlineArrayDefinition(
            arguments[0]!!.accept(this@buildRangeMethod, data),
            arguments[1]!!.accept(this@buildRangeMethod, data)
        )
    }

    else -> error("Unhandled stdlib method kotlin.ranges.$name")
}

private fun CodegenVisitor.buildIterator(data: CodeBuilder, receiver: IrExpression?): Symbol {
    val receiverType = receiver?.type ?: error("iterator not supported as generic type")
    return when (receiverType.classFqName?.asString()) {
        "kotlin.ranges.IntRange" -> IntRangeIterator(
            receiver.accept(this@buildIterator, data),
            data
        )

        else -> error("iterator is not supported for type $receiverType")
    }
}

private fun CodegenVisitor.buildHasNext(data: CodeBuilder, receiver: IrExpression?): Symbol {
    val iteratorHandler = getBoundIterator(receiver, data)
    return iteratorHandler.hasNext(data)
}

private fun CodegenVisitor.buildNext(data: CodeBuilder, receiver: IrExpression?): Symbol {
    val iteratorHandler = getBoundIterator(receiver, data)
    return iteratorHandler.next(data)
}

private fun CodegenVisitor.getBoundIterator(
    receiver: IrExpression?,
    data: CodeBuilder
): BoundIterator {
    val receiverType = receiver?.type ?: error("iterator not supported as generic type")
    val symbol = receiver.accept(this, data)
    val iteratorHandler = (symbol as? BoundIterator)
        ?: (symbol as? IteratorSymbol)?.let {
            data.define(
                receiver,
                "tmp_iter",
                ResolvedType(receiverType),
                it.initialize(data)
            ).also(data::addSymbol) as? BoundIterator
        }
        ?: error("Cannot find iterator for $receiver")
    return iteratorHandler
}
