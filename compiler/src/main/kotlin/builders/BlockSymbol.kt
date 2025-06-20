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

class BlockSymbol(
    override val parent: CodeBuilder,
    private val baseSymbol: Symbol,
    private val postSymbol: Symbol? = null
) : Symbol,
    CodeBuilder,
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

    override fun toString(): String =
        "Block@${hashCode()}: [ start=$baseSymbol, end=$postSymbol\n    " +
            symbolList.joinToString("\n    ") + "end block@${hashCode()}"
}

typealias BodyBuilder = CodeBuilder.() -> Unit

inline fun CodeBuilder.block(symbol: Symbol, postSymbol: Symbol? = null, block: BodyBuilder) {
    addSymbol(block(this, symbol, postSymbol, block))
}

inline fun block(
    parent: CodeBuilder,
    symbol: Symbol,
    postSymbol: Symbol? = null,
    block: BodyBuilder
) = BlockSymbol(parent, symbol, postSymbol).apply(block)
