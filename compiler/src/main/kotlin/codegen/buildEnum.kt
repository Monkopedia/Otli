@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package com.monkopedia.otli.codegen

import com.monkopedia.otli.builders.CodeBuilder
import com.monkopedia.otli.builders.Raw
import com.monkopedia.otli.builders.ResolvedType
import com.monkopedia.otli.builders.Symbol
import com.monkopedia.otli.builders.enum
import com.monkopedia.otli.builders.enumEntry
import com.monkopedia.otli.builders.type
import org.jetbrains.kotlin.ir.backend.js.correspondingField
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrEnumEntry
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.isEnumClass
import org.jetbrains.kotlin.ir.util.isFakeOverride
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.parentClassOrNull

val IrClass.isSupportedEnum: Boolean
    get() = isEnumClass &&
        (
            constructors.firstOrNull() != null ||
                constructors.singleOrNull()?.parameters?.isEmpty() == true
            )

fun CodegenVisitor.buildEnum(cls: IrClass, data: CodeBuilder): Symbol = data.enum(
    type(ResolvedType(cls.defaultType))
) {
    cls.declarations.filter { it !is IrConstructor }.forEach {
        if (it is IrDeclaration &&
            (it.isFakeOverride || it.origin == IrDeclarationOrigin.ENUM_CLASS_SPECIAL_MEMBER)
        ) {
            return@forEach
        }
        addSymbol(it.accept(this@buildEnum, data))
    }
}

fun CodegenVisitor.buildEnumEntry(entry: IrEnumEntry, data: CodeBuilder): Symbol =
    data.enumEntry(entry, identifier(entry)).also {
        declarationLookup[entry] = it
    }

fun identifier(entry: IrEnumEntry): Symbol {
    entry.parentClassOrNull?.cNames()?.let { names ->
        val index = entry.parentAsClass.declarations.indexOf(entry)
        return Raw(names[index - 1])
    }
    return Raw(entry.name.asString())
}
