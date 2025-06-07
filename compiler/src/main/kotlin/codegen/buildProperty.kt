package com.monkopedia.otli.codegen

import com.monkopedia.otli.builders.CodeBuilder
import com.monkopedia.otli.builders.LangFactory
import com.monkopedia.otli.builders.LocalVar
import com.monkopedia.otli.builders.ResolvedType
import com.monkopedia.otli.builders.Symbol
import com.monkopedia.otli.builders.define
import org.jetbrains.kotlin.ir.declarations.IrProperty

fun <T : LangFactory> CodegenVisitor<T>.buildProperty(
    declaration: IrProperty,
    data: CodeBuilder<T>,
    initializer: Symbol? = null
): LocalVar = data.define(
    declaration.name.asString(),
    ResolvedType(
        declaration.backingField?.type ?: error("Properties must have backing fields")
    ),
    initializer = initializer
).also {
    declarationLookup[declaration] = it
}
