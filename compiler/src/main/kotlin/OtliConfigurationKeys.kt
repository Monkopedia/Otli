package com.monkopedia.otli

import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.konan.file.ZipFileSystemAccessor

object OtliConfigurationKeys {
    @JvmField
    val FAKE_OVERRIDE_VALIDATOR = CompilerConfigurationKey.create<Boolean>("IR fake override validator")

    @JvmField
    val ZIP_FILE_SYSTEM_ACCESSOR = CompilerConfigurationKey.create<ZipFileSystemAccessor>("zip file system accessor, used for klib reading")
}
