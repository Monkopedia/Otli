package com.monkopedia.kot

import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.konan.file.ZipFileSystemAccessor

object KotConfigurationKeys {

    @JvmField
    val ZIP_FILE_SYSTEM_ACCESSOR = CompilerConfigurationKey.create<ZipFileSystemAccessor>("zip file system accessor, used for klib reading")
}
