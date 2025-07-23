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

private class EndEnum(val name: Symbol) :
    Symbol,
    SymbolContainer {
    override val symbols: List<Symbol>
        get() = listOf(name)

    override fun build(builder: CodeStringBuilder) {
        builder.append("} ")
        name.build(builder)
        builder.append(";\n")
    }
}

open class EnumSymbol(name: Symbol, structBuilder: CodeBuilder) :
    Symbol,
    SymbolContainer {
    private val block = BlockSymbol(
        structBuilder,
        this,
        EndEnum(name)
    )
    lateinit var signature: Symbol
    val symbol: Symbol
        get() = block
    val body: CodeBuilder
        get() = block

    override val symbols: List<Symbol>
        get() = listOf(signature)

    open fun init() {
        signature = Raw("typedef enum")
    }

    override fun build(builder: CodeStringBuilder) {
        signature.build(builder)
        builder.append(" {\n")
    }

    override fun toString(): String = signature.toString()
}

inline fun CodeBuilder.enum(name: Symbol, buildBody: BodyBuilder): Symbol {
    val builder = varScope {
        EnumSymbol(name, this)
    }
    builder.init()
    builder.body.buildBody()
    return builder.symbol
}
