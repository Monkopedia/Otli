package com.monkopedia.otli.serialization

import org.jetbrains.kotlin.backend.common.serialization.DeclarationTable
import org.jetbrains.kotlin.backend.common.serialization.IrFileSerializer
import org.jetbrains.kotlin.backend.common.serialization.IrSerializationSettings
import org.jetbrains.kotlin.ir.declarations.IrAnnotationContainer
import org.jetbrains.kotlin.ir.declarations.IrFile

fun interface OtliIrFileMetadataFactory {
    fun createOtliIrFileMetadata(irFile: IrFile): OtliIrFileMetadata
}

object OtliIrFileEmptyMetadataFactory : OtliIrFileMetadataFactory {
    override fun createOtliIrFileMetadata(irFile: IrFile) = OtliIrFileMetadata(emptyList())
}

class OtliIrFileSerializer(
    settings: IrSerializationSettings,
    declarationTable: DeclarationTable.Default,
    private val otliIrFileMetadataFactory: OtliIrFileMetadataFactory
) : IrFileSerializer(settings, declarationTable) {
    override fun backendSpecificExplicitRoot(node: IrAnnotationContainer) = true
    override fun backendSpecificExplicitRootExclusion(node: IrAnnotationContainer) = false
    override fun backendSpecificMetadata(irFile: IrFile) =
        otliIrFileMetadataFactory.createOtliIrFileMetadata(irFile)
}
