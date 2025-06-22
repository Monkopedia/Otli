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
package com.monkopedia.otli.builders

import com.monkopedia.otli.codegen.IteratorSymbol
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class Scope(internal val parent: Scope? = null) {
    private val names =
        mutableSetOf(
            "object"
        )
    private val keyedMap = mutableMapOf<Any, String>()

    private fun isUsed(name: String): Boolean = names.contains(name) || parent?.isUsed(name) == true

    fun allocateName(desiredName: String, objKey: Any? = null): String {
        objKey?.let(keyedMap::get)?.let { return it }
        if (desiredName.isEmpty()) return allocateName("v")
        if (isUsed(desiredName)) {
            return allocateName("_$desiredName", objKey)
        }
        names.add(desiredName)
        objKey?.let { keyedMap[it] = desiredName }
        return desiredName
    }

    override fun toString(): String = "Scope{${hashCode()}} (${
        names.reversed().take(5).joinToString(", ")
    }${if (names.size > 5) "..." else ""})"
}

val CodeBuilder.base: CCodeBuilder
    get() =
        (this as? CCodeBuilder)
            ?: this.parent?.base
            ?: error("Dangling builder $this")

val CodeBuilder.scope: Scope
    get() = base.currentScope

fun CodeBuilder.define(
    objKey: Any,
    desiredName: String,
    type: ResolvedType,
    initializer: Symbol? = null,
    isArray: Boolean = type.isArray,
    arraySize: Int = type.takeIf { it.isArray }?.arraySize ?: 0,
    constructorArgs: List<Symbol>? = null
): LocalVar {
    val name = scope.allocateName(desiredName, objKey)
    if (initializer is IteratorSymbol) {
        return CLocalVarIterator(
            initializer,
            name,
            type,
            initializer,
            constructorArgs,
            isArray,
            arraySize
        )
    }
    return CLocalVar(name, type, initializer, constructorArgs, isArray, arraySize)
}

@OptIn(ExperimentalContracts::class)
inline fun <R> CodeBuilder.functionScope(inScope: CodeBuilder.() -> R): R {
    contract {
        callsInPlace(inScope, InvocationKind.EXACTLY_ONCE)
    }
    try {
        base.pushScope()
        return inScope()
    } finally {
        base.popScope()
    }
}
