package com.monkopedia.kot

import org.jetbrains.kotlin.ir.declarations.IdSignatureRetriever
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.declarations.StageController
import org.jetbrains.kotlin.ir.irAttribute
import org.jetbrains.kotlin.ir.util.IdSignature

class IrFactoryImplForKotIC :
    IrFactory(StageController()),
    IdSignatureRetriever {
    override fun <T : IrDeclaration> T.declarationCreated(): T {
        val parentSig =
            stageController.currentDeclaration?.let { declarationSignature(it) } ?: return this

        stageController.createSignature(parentSig)?.let { this.signatureForKotIC = it }

        return this
    }

    override fun declarationSignature(declaration: IrDeclaration): IdSignature? =
        declaration.signatureForKotIC ?: declaration.symbol.signature
            ?: declaration.symbol.privateSignature
}

private var IrDeclaration.signatureForKotIC: IdSignature? by irAttribute(
    followAttributeOwner = false
)
