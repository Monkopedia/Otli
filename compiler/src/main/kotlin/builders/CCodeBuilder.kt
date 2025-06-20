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

class CFunctionSignature(
    private val name: String,
    private val retType: Symbol,
    private val args: List<LocalVar>
) : Symbol {
    override fun build(builder: CodeStringBuilder) {
        retType.build(builder)
        builder.append(' ')
        builder.append(name)
        builder.append('(')
        for ((index, arg) in args.withIndex()) {
            if (index != 0) {
                builder.append(", ")
            }
            arg.build(builder)
        }
        builder.append(')')
    }
}

class CLocalVar(
    override val name: String,
    val type: ResolvedType,
    private val initializer: Symbol?,
    private val constructorArgs: List<Symbol>?,
    private val isArrayType: Boolean = false,
    private val arraySize: Int = 0
) : LocalVar,
    SymbolContainer {
    private val typeSymbol = CType(type)
    override val symbols: List<Symbol>
        get() = listOfNotNull(typeSymbol, initializer) + constructorArgs.orEmpty()
    override var isExtern: Boolean = false

    override fun build(builder: CodeStringBuilder) {
        if (isExtern) {
            builder.append("extern ")
        }
        typeSymbol.build(builder)
        builder.append(' ')
        builder.append(name)
        if (isArrayType) {
            if (arraySize != 0) {
                builder.append("[$arraySize]")
            } else {
                builder.append("[]")
            }
        }
        initializer?.let {
            builder.append(" = ")
            it.build(builder)
        }
        if (constructorArgs != null) {
            builder.append(" = ")
            builder.append('{')
            if (isArrayType) {
                builder.startBlock()
                builder.append("\n")
            }
            constructorArgs.forEachIndexed { index, symbol ->
                if (index != 0) {
                    builder.append(", ")
                    if (isArrayType) builder.append("\n")
                }
                symbol.build(builder)
            }
            if (isArrayType) {
                builder.endBlock()
                builder.append("\n")
            }
            builder.append('}')
        }
    }
}

class CType(private val typeStr: String) : Symbol {
    constructor(type: ResolvedType) : this(type.toString())

    override fun build(builder: CodeStringBuilder) {
        builder.append(typeStr)
    }
}

inline fun CodeBuilder.include(target: String) {
    addSymbol(PreprocessorSymbol("include \"$target\""))
}

inline fun CodeBuilder.includeSys(target: String) {
    addSymbol(PreprocessorSymbol("include <$target>"))
}

inline fun CodeBuilder.define(condition: String) = +PreprocessorSymbol("define $condition")

inline fun CodeBuilder.ifdef(condition: String, builder: CodeBuilder.() -> Unit) = block(
    PreprocessorSymbol("ifdef $condition\n"),
    PreprocessorSymbol("endif //$condition"),
    builder
)

inline fun CodeBuilder.ifndef(condition: String, builder: CodeBuilder.() -> Unit) = block(
    PreprocessorSymbol("ifndef $condition\n"),
    PreprocessorSymbol("endif //$condition"),
    builder
)

class PreprocessorSymbol(private val target: String) : Symbol {
    override val blockSemi: Boolean
        get() = true

    override fun build(builder: CodeStringBuilder) {
        builder.append("#$target")
    }
}
