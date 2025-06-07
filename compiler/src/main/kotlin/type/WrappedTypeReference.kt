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

import kotlinx.serialization.Serializable

private val NATIVE =
    listOf(
        "uint8_t",
        "uint16_t",
        "uint32_t",
        "uint64_t",
        "int8_t",
        "int16_t",
        "int32_t",
        "int64_t",
        "uintptr_t",
        "size_t",
        "void",
        "bool",
        "char",
        "signed char",
        "unsigned char",
        "short",
        "signed short",
        "unsigned short",
        "int",
        "signed int",
        "unsigned int",
        "long",
        "signed long",
        "unsigned long",
        "long long",
        "signed long long",
        "unsigned long long",
        "float",
        "double"
    )

@Serializable
data class WrappedTypeReference(val name: String) : WrappedType() {
    override val isArray: Boolean
        get() = name.endsWith("]")
    val arraySize: Int
        get() {
            require(isArray) {
                "Can't get array size of non-array"
            }
            val openIndex = name.indexOf("[")
            val closeIndex = name.indexOf("]")
            if (openIndex < 0 || closeIndex < 0) return -1
            if (openIndex + 1 == closeIndex) return -1
            return name.substring(openIndex + 1, closeIndex).toInt()
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
