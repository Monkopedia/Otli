package com.monkopedia.otli.codegen

import com.monkopedia.otli.builders.Call
import com.monkopedia.otli.builders.CodeBuilder
import com.monkopedia.otli.builders.Included
import com.monkopedia.otli.builders.InlineArrayDefinition
import com.monkopedia.otli.builders.Op
import com.monkopedia.otli.builders.Raw
import com.monkopedia.otli.builders.RawCast
import com.monkopedia.otli.builders.ResolvedType
import com.monkopedia.otli.builders.Symbol
import com.monkopedia.otli.builders.addressOf
import com.monkopedia.otli.builders.define
import com.monkopedia.otli.builders.dereference
import com.monkopedia.otli.builders.dot
import com.monkopedia.otli.builders.op
import com.monkopedia.otli.builders.reference
import com.monkopedia.otli.builders.type
import com.monkopedia.otli.type.WrappedType.Companion.pointerTo
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.properties

fun CodegenVisitor.buildOtliMethod(
    receiver: IrExpression? = null,
    expression: IrCall?,
    name: String,
    arguments: List<IrExpression?>,
    data: CodeBuilder,
    returnType: IrType? = null,
    pkg: String = ""
): Symbol = when (name) {
    "adr" -> arguments.singleOrNull()?.accept(this, data)?.addressOf
        ?: error("Missing argument for adr")

    "get" -> arguments.singleOrNull()?.accept(this, data)?.dereference
        ?: error("Missing argument for get")

    "alloc" -> buildAlloc(arguments, data, returnType)
    else -> error("Unhandled otli method $name")
}

val MALLOC = Included("malloc", "malloc.h", true)
val SIZEOF = Raw("sizeof")
val FREE = Included("free", "malloc.h", true)

private fun CodegenVisitor.buildAlloc(
    arguments: List<IrExpression?>,
    data: CodeBuilder,
    returnType: IrType?
): Symbol {
    val arg = arguments.singleOrNull()
    val allocType = arg?.type
        ?: error("Missing argument for alloc")
    val allocTypeWrapper = ResolvedType(allocType)
    val tmpName = "_tmp_" + allocType.classOrNull?.owner?.name?.asString()
    val tmpVar = data.define(
        arguments.single()!!,
        tmpName,
        pointerTo(allocTypeWrapper),
        initializer = RawCast(
            type(pointerTo(allocTypeWrapper)),
            Call(MALLOC, Call(SIZEOF, type(allocTypeWrapper)))
        )
    ).also(data::addSymbol)
    val argSymbol = arg.accept(this, data)
    if (argSymbol is InlineArrayDefinition) {
        argSymbol.symbols.forEach {
            require(it is Op && it.operand == ":") {
                "Unexpected initialization symbol $it"
            }
            data.addSymbol(tmpVar.dereference.dot(it.first).op("=", it.second))
        }
    } else {
        allocType.classOrFail.owner.properties.forEach {
            val fieldRef =
                declarationLookup[it.backingField ?: return@forEach]?.reference ?: return@forEach
            data.addSymbol(tmpVar.dereference.dot(fieldRef).op("=", argSymbol.dot(fieldRef)))
        }
    }
    return tmpVar.reference
}
