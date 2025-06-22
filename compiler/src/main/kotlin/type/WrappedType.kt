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


private val existingTypes = mutableMapOf<String, WrappedType>()

abstract class WrappedType {
    //    abstract val cType: WrappedType
    abstract val include: Include?

    open fun clone(): WrappedType = this

    abstract val coreType: String
    abstract val isNative: Boolean
    abstract val isReturnable: Boolean
    abstract val isVoid: Boolean

    abstract val pointed: WrappedType
    abstract val isPointer: Boolean

    abstract val isArray: Boolean

    abstract val unreferenced: WrappedType

    abstract val isReference: Boolean
    abstract val isConst: Boolean
    abstract val unconst: WrappedType

    companion object :
            (String) -> WrappedType {
        const val LONG_DOUBLE_STR = "long double"
        val LONG_DOUBLE = WrappedTypeReference(LONG_DOUBLE_STR)
        val VOID = WrappedTypeReference("void")

        override fun invoke(type: String): WrappedType {
            if (type == "void") return VOID
            return existingTypes.getOrPut(type) {
                if (type.startsWith("const ")) return const(invoke(type.substring("const ".length)))
                if (type.endsWith("]")) {
                    val splitPoint = type.indexOfLast { it == '[' }
                    return arrayOf(
                        invoke(type.substring(0, splitPoint)),
                        type.substring(splitPoint + 1, type.length - 1).toIntOrNull()
                    )
                }
                if (type.endsWith("*")) {
                    return pointerTo(invoke(type.substring(0, type.length - 1).trim()))
                }
                if (type.endsWith("&")) {
                    return referenceTo(invoke(type.substring(0, type.length - 1).trim()))
                }
                if (type.isEmpty()) {
                    throw IllegalArgumentException("Empty type")
                }
                WrappedTypeReference(type)
            }
        }

        private inline fun WrappedType.maybeConst(isConst: Boolean): WrappedType =
            if (isConst) const(this) else this

        fun pointerTo(type: WrappedType): WrappedType = WrappedModifiedType(type, "*")

        fun referenceTo(type: WrappedType): WrappedType = WrappedModifiedType(type, "&")

        fun arrayOf(type: WrappedType, size: Int? = null): WrappedType =
            WrappedModifiedType(type, "[${size ?: ""}]")

        fun const(type: WrappedType): WrappedType {
            if (type.isConst) return type
            return WrappedPrefixedType(type, "const")
        }

        val UNRESOLVABLE: WrappedTypeReference
            get() = WrappedTypeReference("unresolveable")
    }

    abstract val elementType: WrappedType
    abstract val arraySize: Int?
}

val WrappedType.isString: Boolean
    get() = isPointer && (coreType == "char" || coreType == "unsigned char")
