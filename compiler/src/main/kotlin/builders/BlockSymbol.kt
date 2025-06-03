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

class BlockSymbol<T : LangFactory>(
    override val parent: CodeBuilder<T>,
    private val baseSymbol: Symbol,
    private val postSymbol: Symbol? = null
) : Symbol,
    CodeBuilder<T>,
    SymbolContainer {
    private val symbolList = mutableListOf<Symbol>()
    override val symbols: List<Symbol>
        get() = listOf(baseSymbol) + symbolList + listOfNotNull(postSymbol)

    override val blockSemi: Boolean
        get() = true

    override fun build(builder: CodeStringBuilder) {
        baseSymbol.build(builder)
        val base = base
        builder.block {
            for (symbol in symbolList) {
                symbol.build(builder)
                if (base.addSemis && !symbol.blockSemi) {
                    append(';')
                }
                append('\n')
            }
        }
        postSymbol?.build(builder)
    }

    override fun addSymbol(symbol: Symbol) {
        symbolList += symbol
    }

    override val factory: T
        get() = parent.factory

    override fun toString(): String =
        "Block@${hashCode()}: [ start=$baseSymbol, end=$postSymbol\n    " +
            symbolList.joinToString("\n    ") + "end block@${hashCode()}"
}

typealias BodyBuilder<T> = CodeBuilder<T>.() -> Unit

inline fun <T : LangFactory> CodeBuilder<T>.block(
    symbol: Symbol,
    postSymbol: Symbol? = null,
    block: BodyBuilder<T>
) {
    addSymbol(block(this, symbol, postSymbol, block))
}

inline fun <T : LangFactory> block(
    parent: CodeBuilder<T>,
    symbol: Symbol,
    postSymbol: Symbol? = null,
    block: BodyBuilder<T>
) = BlockSymbol(parent, symbol, postSymbol).apply(block)
