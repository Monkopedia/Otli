package com.monkopedia.otli.codegen

import com.monkopedia.otli.builders.CodeBuilder
import com.monkopedia.otli.builders.LocalVar
import com.monkopedia.otli.builders.ResolvedType
import com.monkopedia.otli.builders.Symbol
import com.monkopedia.otli.builders.define
import org.jetbrains.kotlin.ir.declarations.IrDeclarationWithName
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrWhileLoop
import org.jetbrains.kotlin.ir.types.IrType

fun CodegenVisitor.buildProperty(
    declaration: IrDeclarationWithName,
    data: CodeBuilder,
    type: IrType,
    initializer: Symbol? = null
): LocalVar = data.define(
    declaration,
    declaration.name.asString(),
    ResolvedType(type),
    initializer = initializer
).also {
    declarationLookup[declaration] = it
}
