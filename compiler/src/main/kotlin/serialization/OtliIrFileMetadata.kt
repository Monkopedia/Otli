package com.monkopedia.otli.serialization

import org.jetbrains.kotlin.backend.common.serialization.IrFileSerializer
import org.jetbrains.kotlin.library.encodings.WobblyTF8
import org.jetbrains.kotlin.library.impl.IrArrayReader
import org.jetbrains.kotlin.library.impl.IrStringWriter
import org.jetbrains.kotlin.library.impl.toArray

class OtliIrFileMetadata(val exportedNames: List<String> = emptyList()) :
    IrFileSerializer.FileBackendSpecificMetadata {
    override fun toByteArray(): ByteArray = IrStringWriter(exportedNames).writeIntoMemory()

    companion object {
        fun fromByteArray(data: ByteArray): OtliIrFileMetadata = OtliIrFileMetadata(
            exportedNames = IrArrayReader(data).toArray().map(WobblyTF8::decode)
        )
    }
}
