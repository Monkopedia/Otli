@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package com.monkopedia.otli.codegen

import com.monkopedia.otli.builders.CodeBuilder
import com.monkopedia.otli.builders.FileSymbol
import com.monkopedia.otli.builders.HeaderSymbol
import com.monkopedia.otli.builders.Included
import com.monkopedia.otli.builders.ResolvedType
import com.monkopedia.otli.builders.Symbol
import com.monkopedia.otli.builders.define
import com.monkopedia.otli.type.WrappedType
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.fir.declarations.builder.buildProperty
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.isFakeOverriddenFromAny
import org.jetbrains.kotlin.ir.visitors.IrVisitor
import org.jetbrains.kotlin.synthetic.isVisibleOutside

class HeaderVisitor : IrVisitor<Unit, CodeBuilder>() {
    var currentFile: IrFile? = null

    override fun visitElement(
        element: IrElement,
        data: CodeBuilder
    ): Unit {
        element.acceptChildren(this, data)
    }

    override fun visitProperty(declaration: IrProperty, data: CodeBuilder) {
        if (declaration.visibility.isVisibleOutside()) {
            data.addSymbol(
                data.define(
                    declaration,
                    declaration.name.asString(),
                    ResolvedType(
                        declaration.backingField?.type
                            ?: error("Properties must have backing fields")
                    ),
                    null
                ).also {
                    it.isExtern = true
                })
        }
    }

    override fun visitFunction(declaration: IrFunction, data: CodeBuilder) {
        if (declaration.extensionReceiverParameter == null && declaration.visibility.isVisibleOutside()) {
            // Hack to avoid this for now
            if (!declaration.isFakeOverriddenFromAny() && declaration.name.asString() != "equals" && declaration.name.asString() != "<init>") {
                data.addSymbol(buildFunctionDeclaration(declaration, data))
            }
        }
    }

    override fun visitClass(declaration: IrClass, data: CodeBuilder) {
        super.visitClass(declaration, data)
        if (declaration.declarations.all { it.hasTestDeclaration }) {
            data.addSymbol(
                data.define(
                    declaration,
                    classTestsName(declaration),
                    ResolvedType("CU_TestInfo"),
                    isArray = true
                ).also {
                    it.isExtern = true
                })
            data.addSymbol(Included("", "CUnit/CUnit.h", true))
        }
    }

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    override fun visitFile(declaration: IrFile, data: CodeBuilder): Unit {
        data.addSymbol(
            FileSymbol(data, declaration.packageFqName.pkgPrefix() + declaration.name + ".h").apply {
                val lastFile = currentFile
                currentFile = declaration
                val includeBlock = groupSymbol.symbolList.removeFirst()
                addSymbol(HeaderSymbol(this, declaration.name + ".h").apply {
                    addSymbol(includeBlock)
                    declaration.declarations.forEach {
                        it.accept(this@HeaderVisitor, this)
                    }
                })
                currentFile = lastFile
            }
        )
    }
}
