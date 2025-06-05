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

interface Symbol {
    fun build(builder: CodeStringBuilder)

    open val blockSemi: Boolean
        get() = false
}

object Empty : Symbol {
    override val blockSemi: Boolean
        get() = true

    override fun build(builder: CodeStringBuilder) = Unit
}

interface LocalVar : Symbol {
    val name: String
}

interface LangFactory {
    fun define(
        name: String,
        type: ResolvedType,
        initializer: Symbol?,
        constructorArgs: List<Symbol>?
    ): LocalVar

    fun funSig(name: String, retType: Symbol?, args: List<LocalVar>): Symbol

    fun createType(type: ResolvedType): Symbol
}

interface CodeBuilder<T : LangFactory> {
    val parent: CodeBuilder<T>?
    val factory: T

    fun addSymbol(symbol: Symbol)

    operator fun <T : Symbol> T.unaryPlus(): T = apply {
        addSymbol(this)
    }
}

class CodeBuilderBase<T : LangFactory>(
    override val factory: T,
    rootScope: Scope<T> = Scope<T>(),
    internal val addSemis: Boolean = true
) : CodeBuilder<T>,
    SymbolContainer {
    private val symbolList = mutableListOf<Symbol>()
    override val symbols: List<Symbol>
        get() = symbolList
    override val parent: CodeBuilder<T>? = null
    private val scopes = mutableListOf(rootScope)
    val currentScope: Scope<T>
        get() = scopes.last()

    override fun addSymbol(symbol: Symbol) {
        symbolList.add(symbol)
    }

    override fun toString(): String = builder().build()

    private fun builder(): CodeStringBuilder = buildCode {
            for (symbol in symbolList) {
                symbol.build(this)
                if (addSemis && !symbol.blockSemi) {
                    append(';')
                }
                append('\n')
            }
        }

    fun files(): Map<String, String> {
        return builder().allFiles()
    }

    fun pushScope() {
        scopes.add(Scope(currentScope))
    }

    fun popScope() {
        scopes.removeLast()
    }
}

inline fun CodeBuilder<*>.appendLine() {
    addSymbol(Empty)
}

inline fun CodeBuilder<*>.type(type: ResolvedType): Symbol = factory.createType(type)
