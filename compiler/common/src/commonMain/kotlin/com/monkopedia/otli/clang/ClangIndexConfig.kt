package com.monkopedia.otli.clang

import kotlinx.serialization.Serializable

@Serializable
data class ClangIndexConfig(
    val compiler: String,
    val includePaths: List<String>,
    val compilerFlags: List<String>,
    val targetFile: String
)
