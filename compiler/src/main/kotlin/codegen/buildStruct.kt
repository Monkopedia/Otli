package com.monkopedia.otli.codegen

import com.monkopedia.otli.builders.CodeBuilder
import com.monkopedia.otli.builders.GroupSymbol
import com.monkopedia.otli.builders.ResolvedType
import com.monkopedia.otli.builders.Symbol
import com.monkopedia.otli.builders.struct
import com.monkopedia.otli.builders.type
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.util.properties

@OptIn(UnsafeDuringIrConstructionAPI::class)
fun CodegenVisitor.buildStruct(cls: IrClass, data: CodeBuilder): Symbol =
    GroupSymbol().apply {
        symbolList.add(defineStruct(cls, data))
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
            )
        }
    }
