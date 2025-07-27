package com.monkopedia.otli.codegen

import com.monkopedia.otli.builders.CodeBuilder
import com.monkopedia.otli.builders.Define
import com.monkopedia.otli.builders.DefineReference
import com.monkopedia.otli.builders.EmptyInclude
import com.monkopedia.otli.builders.Include
import com.monkopedia.otli.builders.LocalVar
import com.monkopedia.otli.builders.ResolvedType
import com.monkopedia.otli.builders.Symbol
import com.monkopedia.otli.builders.define
import com.monkopedia.otli.builders.scope
import org.jetbrains.kotlin.ir.declarations.IrDeclarationWithName
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.file

fun CodegenVisitor.buildProperty(
    declaration: IrDeclarationWithName,
    data: CodeBuilder,
    type: IrType,
    initializer: Symbol? = null
): LocalVar = data.define(
    declaration,
    declaration.name.asString(),
    ResolvedType(type),
    initializer = initializer,
    include = declaration.include()
).also {
    declarationLookup[declaration] = it
}

fun CodegenVisitor.buildConst(
    declaration: IrDeclarationWithName,
    data: CodeBuilder,
    type: IrType,
    initializer: Symbol? = null,
    isPublic: Boolean,
    isHeader: Boolean = false
): Symbol = if (isPublic == isHeader) {
    val name = data.scope.root.allocateName(declaration.name.asString(), declaration)
    DefineReference(
        Define(name, initializer ?: error("Consts must have an initializer")),
        ResolvedType(type)
    ).also {
        declarationLookup[declaration] = it
    }
} else {
    EmptyInclude(Include(headerName(declaration.file), false))
}
