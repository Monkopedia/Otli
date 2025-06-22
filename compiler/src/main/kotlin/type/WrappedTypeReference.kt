/**
 * Copyright (C) 2024 Jason Monk <monkopedia@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.monkopedia.otli.type

import com.monkopedia.otli.builders.Include
import kotlinx.serialization.Serializable

private val NATIVE =
    mapOf(
        "uint8_t" to "stdint.h",
        "uint16_t" to "stdint.h",
        "uint32_t" to "stdint.h",
        "uint64_t" to "stdint.h",
        "int8_t" to "stdint.h",
        "int16_t" to "stdint.h",
        "int32_t" to "stdint.h",
        "int64_t" to "stdint.h",
        "uintptr_t" to "stdint.h",
        "size_t" to "stdint.h",
        "void" to null,
        "bool" to "stdbool.h",
        "char" to null,
        "signed char" to null,
        "unsigned char" to null,
        "short" to null,
        "signed short" to null,
        "unsigned short" to null,
        "int" to null,
        "signed int" to null,
        "unsigned int" to null,
        "long" to null,
        "signed long" to null,
        "unsigned long" to null,
        "long long" to null,
        "signed long long" to null,
        "unsigned long long" to null,
        "float" to null,
        "double" to null,
    )

@Serializable
data class WrappedTypeReference(val name: String) : WrappedType() {
    override val include: Include?
        get() = if (isNative) {
            NATIVE[name]?.let { Include(it, true) }
        } else null
    override val isArray: Boolean
        get() = name.endsWith("]")
    override val arraySize: Int?
        get() {
            if (!isArray) return null
            val openIndex = name.indexOf("[")
            val closeIndex = name.indexOf("]")
            if (openIndex < 0 || closeIndex < 0) return -1
            if (openIndex + 1 == closeIndex) return -1
            return name.substring(openIndex + 1, closeIndex).toIntOrNull()
        }
    override val elementType: WrappedType
        get() {
            require (isArray) {
                "Type is not an array"
            }
            return WrappedTypeReference(name.split("[").first())
        }
    override val coreType: String
        get() = name
    val arrayType: WrappedTypeReference
        get() {
            require(isArray) {
                "Can't get base type size of non-array"
            }
            val openIndex = name.indexOf("[")
            require(openIndex >= 0) {
                "Can't find type of array $name"
            }
            return WrappedTypeReference(name.substring(0, openIndex).trim())
        }
    override val isPointer: Boolean
        get() = false
    override val isReference: Boolean
        get() = false
    override val pointed: WrappedType
        get() = error("Cannot get pointed of non-pointer type $this")
    override val unreferenced: WrappedType
        get() = error("Cannot unreference of non-reference type $this")

    override val isConst: Boolean
        get() = false
    override val unconst: WrappedTypeReference
        get() = this
    override val isNative: Boolean
        get() = name in NATIVE || (isArray && arrayType.isNative)
    override val isReturnable: Boolean
        get() = name in NATIVE || name == LONG_DOUBLE_STR
    override val isVoid: Boolean
        get() = name == "void"
//    override val cType: WrappedType
//        get() {
//            if (isNative) {
//                return this
//            }
//            if (name == LONG_DOUBLE_STR) {
//                return WrappedType.Companion("double")
//            }
//            return pointerTo(VOID)
//        }

    override fun toString(): String = name
}
