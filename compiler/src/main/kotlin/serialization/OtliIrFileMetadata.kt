package com.monkopedia.otli.serialization

import org.jetbrains.kotlin.backend.common.serialization.IrFileSerializer
import org.jetbrains.kotlin.library.encodings.WobblyTF8
import org.jetbrains.kotlin.library.impl.IrArrayMemoryReader
import org.jetbrains.kotlin.library.impl.IrMemoryStringWriter
import org.jetbrains.kotlin.library.impl.toArray

class OtliIrFileMetadata(val exportedNames: List<String> = emptyList()) :
    IrFileSerializer.FileBackendSpecificMetadata {
    override fun toByteArray(): ByteArray = IrMemoryStringWriter(exportedNames).writeIntoMemory()

    companion object {
        fun fromByteArray(data: ByteArray): OtliIrFileMetadata = OtliIrFileMetadata(
            exportedNames = IrArrayMemoryReader(data).toArray().map(WobblyTF8::decode)
        )
    }
}
