package com.monkopedia.otli.codegen

import com.monkopedia.otli.builders.Call
import com.monkopedia.otli.builders.CodeBuilder
import com.monkopedia.otli.builders.Dereference
import com.monkopedia.otli.builders.GroupSymbol
import com.monkopedia.otli.builders.Included
import com.monkopedia.otli.builders.LocalVar
import com.monkopedia.otli.builders.Raw
import com.monkopedia.otli.builders.Reference
import com.monkopedia.otli.builders.ResolvedType
import com.monkopedia.otli.builders.Return
import com.monkopedia.otli.builders.Symbol
import com.monkopedia.otli.builders.arrow
import com.monkopedia.otli.builders.buildIf
import com.monkopedia.otli.builders.define
import com.monkopedia.otli.builders.function
import com.monkopedia.otli.builders.not
import com.monkopedia.otli.builders.reference
import com.monkopedia.otli.builders.struct
import com.monkopedia.otli.builders.type
import com.monkopedia.otli.type.WrappedType
import com.monkopedia.otli.type.WrappedType.Companion.pointerTo
import org.jetbrains.kotlin.ir.backend.js.utils.isDispatchReceiver
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.isPropertyAccessor
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.IrStringConcatenation
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.isFakeOverride
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.ir.util.statements

@OptIn(UnsafeDuringIrConstructionAPI::class)
fun CodegenVisitor.buildStruct(cls: IrClass, data: CodeBuilder, isHeader: Boolean = false): Symbol =
    GroupSymbol().apply {
        val functions = cls.declarations.filterIsInstance<IrFunction>()
        functions
            .filter { it.name.asString() != "<init>" && it.name.asString() != "copy" }
            .filter { !it.isFakeOverride }
            .filter { !it.isPropertyAccessor }
            .forEach {
                symbolList.add(buildStructMember(it, data, isHeader = isHeader))
            }
    }

fun CodegenVisitor.buildStructMember(
    function: IrFunction,
    data: CodeBuilder,
    isHeader: Boolean = false
): Symbol = when (function.name.asString()) {
    "equals" -> buildEqualsDeclaration(
        function,
        function.dispatchReceiverParameter!!.type.classOrNull!!.owner,
        data,
        isHeader = isHeader
    )

    "toString" -> buildToString(
        function,
        function.dispatchReceiverParameter!!.type.classOrNull!!.owner,
        data,
        isHeader = isHeader
    )

    else -> buildFunction(
        function,
        data,
        this,
        isHeader = isHeader
    )
}

fun CodegenVisitor.defineStruct(cls: IrClass, data: CodeBuilder): Symbol =
    data.struct(type(ResolvedType(cls.defaultType))) {
        val primaryConstructor =
            cls.primaryConstructor ?: error("Data class without primary constructor?")
        if (cls.properties.toList().size != primaryConstructor.parameters.size) {
            error("Non-constructor parameters are not supported")
        }
        cls.properties.forEach {
            +buildProperty(
                it,
                data,
                it.backingField?.type ?: error("Properties must have backing fields")
            ).also { v ->
                println("Setting declaration ${it.symbol}")
                declarationLookup[it] = v
                it.backingField?.let {
                    declarationLookup[it] = v
                }
            }
        }
    }

private fun CodegenVisitor.buildEqualsDeclaration(
    function: IrFunction,
    owner: IrClass,
    data: CodeBuilder,
    isHeader: Boolean = false
): Symbol = data.function(isHeader = isHeader) {
    name = methodName(function)
    retType = type(ResolvedType(function.returnType))
    val thiz =
        this.define(function to "thiz", "thiz", pointerTo(ResolvedType(owner.defaultType)))
    val other =
        this.define(function to "other", "other", pointerTo(ResolvedType(owner.defaultType)))
    body {
        owner.properties.forEach { prop ->
            +buildIf {
                val propType = prop.backingField!!.type
                condition = data.not(
                    buildEquals(
                        propType,
                        propType,
                        thiz.reference.arrow(declarationLookup[prop]!!.reference),
                        other.reference.arrow(declarationLookup[prop]!!.reference)
                    )
                )
                ifBlock {
                    +Return(Raw("false"))
                }
            }
        }
        +Return(Raw("true"))
    }
}

val SNPRINTF = Included("snprintf", "stdio.h", true)

private fun CodegenVisitor.buildToString(
    function: IrFunction,
    owner: IrClass,
    data: CodeBuilder,
    isHeader: Boolean = false
): Symbol = data.function(isHeader = isHeader) {
    name = methodName(function)
    retType = type(WrappedType("int"))
    val thiz =
        this.define(function to "thiz", "thiz", pointerTo(ResolvedType(owner.defaultType)))
    val buffer =
        this.define(function to "buffer", "buffer", WrappedType("const char*"))
    val n =
        this.define(function to "n", "n", WrappedType("size_t"))
    body {
        val concat = function.body?.statements?.firstNotNullOfOrNull {
            (it as? IrReturn)?.value as? IrStringConcatenation
        }
        if (concat != null) {
            +Return(
                Call(
                    SNPRINTF,
                    buffer.reference,
                    n.reference,
                    *withThisScope(thiz) {
                        convertArgs(concat, data)
                    }
                )
            )
        } else {
            +Return(
                Call(
                    SNPRINTF,
                    buffer.reference,
                    n.reference,
                    Raw("\"${owner.name.asString()}@%p\""),
                    thiz.reference
                )
            )
        }
    }
}

fun CodegenVisitor.buildStructCopy(
    expression: IrCall?,
    name: String,
    arguments: List<IrExpression?>,
    data: CodeBuilder,
    pkg: String = ""
): Symbol {
    val thizParameter = expression!!.symbol.owner.parameters.find { it.isDispatchReceiver }
        ?: error("Copying without a receiver?")
    val thizValue = arguments[thizParameter.indexInParameters]?.accept(this, data)
        ?: error("Copying without a receiver value?")
    val thizReference = (thizValue as? Reference)?.arg
        ?: ((thizValue as? Dereference)?.arg as? Reference)?.arg
        ?: (thizValue as? LocalVar)?.also { data.addSymbol(thizValue) }
        ?: data.define(thizValue, "cpyv", ResolvedType(thizParameter.type), initializer = thizValue)
            .also {
                data.addSymbol(it)
            }
    val parameters = expression.symbol.owner.parameters - thizParameter

    withThisScope(thizReference) {
        return buildConstructor(
            expression!!.symbol.owner.returnType,
            parameters,
            arguments.filterIndexed { index, _ ->
                index != thizParameter.indexInParameters
            },
            data
        )
    }
}
