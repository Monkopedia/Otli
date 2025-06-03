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

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class Scope<T : LangFactory>(internal val parent: Scope<T>? = null) {
    private val names =
        mutableSetOf(
            "object"
        )

    private fun isUsed(name: String): Boolean = names.contains(name) || parent?.isUsed(name) == true

    fun allocateName(desiredName: String): String {
        if (desiredName.isEmpty()) return allocateName("v")
        if (isUsed(desiredName)) {
            return allocateName("_$desiredName")
        }
        names.add(desiredName)
        return desiredName
    }

    override fun toString(): String = "Scope{${hashCode()}} (${
        names.reversed().take(5).joinToString(", ")
    }${if (names.size > 5) "..." else ""})"
}

val <T : LangFactory> CodeBuilder<T>.base: CodeBuilderBase<T>
    get() =
        (this as? CodeBuilderBase<T>)
            ?: (this as? BlockSymbol<T>)?.parent?.base
            ?: error("Dangling builder $this")

val <T : LangFactory> CodeBuilder<T>.scope: Scope<T>
    get() = base.currentScope

fun <T : LangFactory> CodeBuilder<T>.define(
    desiredName: String,
    type: ResolvedType,
    initializer: Symbol? = null,
    constructorArgs: List<Symbol>? = null
): LocalVar {
    val name = scope.allocateName(desiredName)
    return factory.define(name, type, initializer, constructorArgs)
}

@OptIn(ExperimentalContracts::class)
inline fun <T : LangFactory, R> CodeBuilder<T>.functionScope(inScope: CodeBuilder<T>.() -> R): R {
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
