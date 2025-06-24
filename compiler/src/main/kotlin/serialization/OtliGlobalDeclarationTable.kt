package com.monkopedia.otli.serialization

import com.monkopedia.otli.OtliManglerIr
import org.jetbrains.kotlin.backend.common.serialization.GlobalDeclarationTable
import org.jetbrains.kotlin.backend.common.serialization.IrSerializationSettings
import org.jetbrains.kotlin.ir.IrBuiltIns

class OtliGlobalDeclarationTable(builtIns: IrBuiltIns, settings: IrSerializationSettings) :
    GlobalDeclarationTable(OtliManglerIr, settings.reuseExistingSignaturesForSymbols) {
    init {
        loadKnownBuiltins(builtIns)
    }
}
