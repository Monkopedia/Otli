package com.monkopedia.otli.serialization

import org.jetbrains.kotlin.backend.common.serialization.DeclarationTable
import org.jetbrains.kotlin.backend.common.serialization.IrModuleSerializer
import org.jetbrains.kotlin.backend.common.serialization.IrSerializationSettings
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrDiagnosticReporter
import org.jetbrains.kotlin.ir.declarations.IrFile

class OtliIrModuleSerializer(
    settings: IrSerializationSettings,
    diagnosticReporter: IrDiagnosticReporter,
    irBuiltIns: IrBuiltIns,
    private val otliIrFileMetadataFactory: OtliIrFileMetadataFactory =
        OtliIrFileEmptyMetadataFactory
) : IrModuleSerializer<OtliIrFileSerializer>(settings, diagnosticReporter) {

    override val globalDeclarationTable = OtliGlobalDeclarationTable(irBuiltIns, settings)

    override fun createSerializerForFile(file: IrFile): OtliIrFileSerializer = OtliIrFileSerializer(
        settings,
        DeclarationTable.Default(globalDeclarationTable),
        otliIrFileMetadataFactory
    )
}
