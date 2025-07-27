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
@file:Suppress("NOTHING_TO_INLINE")

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

data class EmptyInclude(val include: Include) :
    Symbol by Empty,
    Predefines {
    override val predefines: List<Symbol>
        get() = listOf(include)
}

interface LocalVar : Symbol {
    val name: String
    var isExtern: Boolean
    val include: Symbol?
        get() = null
}

interface TypedLocalVar : LocalVar {
    val type: ResolvedType
}

val LocalVar.isPointer: Boolean
    get() = (this as? TypedLocalVar)?.type?.isPointer == true

interface CodeBuilder {
    val parent: CodeBuilder?

    fun add(symbol: Symbol)

    fun addSymbol(symbol: Symbol) = add(symbol)

    operator fun <T : Symbol> T.unaryPlus(): T = apply {
        addSymbol(this)
    }
}

class CCodeBuilder(rootScope: Scope = Scope(), internal val addSemis: Boolean = true) :
    CodeBuilder,
    SymbolContainer {
    private val symbolList = mutableListOf<Symbol>()
    override val symbols: List<Symbol>
        get() = symbolList
    override val parent: CodeBuilder? = null
    private val scopes = mutableListOf(rootScope)
    val currentScope: Scope
        get() = scopes.last()

    override fun add(symbol: Symbol) {
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

    fun files(): Map<String, String> = builder().allFiles()

    fun pushScope(isFile: Boolean = false) {
        scopes.add(Scope(if (isFile) null else currentScope))
    }

    fun popScope() {
        scopes.removeLast()
    }
}

inline fun CodeBuilder.appendLine() {
    addSymbol(Empty)
}

inline fun type(type: ResolvedType): Symbol = CType(type)
