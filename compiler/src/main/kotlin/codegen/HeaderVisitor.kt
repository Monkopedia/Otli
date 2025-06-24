@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package com.monkopedia.otli.codegen

import com.monkopedia.otli.builders.CodeBuilder
import com.monkopedia.otli.builders.FileSymbol
import com.monkopedia.otli.builders.HeaderSymbol
import com.monkopedia.otli.builders.Included
import com.monkopedia.otli.builders.ResolvedType
import com.monkopedia.otli.builders.define
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.isFakeOverriddenFromAny
import org.jetbrains.kotlin.ir.visitors.IrVisitor
import org.jetbrains.kotlin.synthetic.isVisibleOutside

class HeaderVisitor(val parentVisitor: CodegenVisitor) : IrVisitor<Unit, CodeBuilder>() {
    var currentFile: IrFile? = null

    override fun visitElement(element: IrElement, data: CodeBuilder) {
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
                }
            )
        }
    }

    override fun visitFunction(declaration: IrFunction, data: CodeBuilder) {
        if (declaration.parameters.none { it.kind == IrParameterKind.ExtensionReceiver } &&
            declaration.visibility.isVisibleOutside()
        ) {
            // Hack to avoid this for now
            if (!declaration.isFakeOverriddenFromAny() &&
                declaration.name.asString() != "equals" &&
                declaration.name.asString() != "<init>"
            ) {
                data.addSymbol(buildFunction(declaration, data, parentVisitor, isHeader = true))
            }
        }
    }

    override fun visitClass(declaration: IrClass, data: CodeBuilder) {
        if (declaration.isData) {
            data.addSymbol(parentVisitor.defineStruct(declaration, data))
            data.addSymbol(parentVisitor.buildStruct(declaration, data, isHeader = true))
        } else if (declaration.isTestClass) {
            super.visitClass(declaration, data)
            data.addSymbol(
                data.define(
                    declaration,
                    classTestsName(declaration),
                    ResolvedType("CU_TestInfo"),
                    isArray = true
                ).also {
                    it.isExtern = true
                }
            )
            data.addSymbol(Included("", "CUnit/CUnit.h", true))
        }
    }

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    override fun visitFile(declaration: IrFile, data: CodeBuilder) {
        data.addSymbol(
            FileSymbol(
                data,
                declaration.packageFqName.pkgPrefix() + declaration.name + ".h"
            ).apply {
                val lastFile = currentFile
                currentFile = declaration
                val includeBlock = groupSymbol.symbolList.removeFirst()
                addSymbol(
                    HeaderSymbol(this, declaration.name + ".h").apply {
                        addSymbol(includeBlock)
                        declaration.declarations.forEach {
                            it.accept(this@HeaderVisitor, this)
                        }
                    }
                )
                currentFile = lastFile
            }
        )
    }
}
