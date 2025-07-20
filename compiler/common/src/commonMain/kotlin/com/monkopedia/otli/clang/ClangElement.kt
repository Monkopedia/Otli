package com.monkopedia.otli.clang

import kotlinx.serialization.Serializable

@Serializable
data class ClangElement(
    val spelling: String,
    val type: String,
    val kind: ClangElementKind,
    val file: String,
    val children: List<ClangElement>
)
