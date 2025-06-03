package com.monkopedia.otli.codegen

import com.monkopedia.otli.builders.CCodeBuilder
import com.monkopedia.otli.builders.comment
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.visitors.IrVisitor

class CodegenVisitor : IrVisitor<Unit, CCodeBuilder>() {
    override fun visitElement(element: IrElement, data: CCodeBuilder) {
        element.acceptChildren(this, data)
    }

    override fun visitFile(declaration: IrFile, data: CCodeBuilder) {
        data.apply {
            comment("Starting file ${declaration.name}")
        }
        super.visitFile(declaration, data)
    }

    override fun visitProperty(declaration: IrProperty, data: CCodeBuilder) {
        data.apply {
            comment("Starting property ${declaration.name} ${declaration.backingField?.type?.classFqName?.asString()}")
        }
        super.visitProperty(declaration, data)
    }
}
