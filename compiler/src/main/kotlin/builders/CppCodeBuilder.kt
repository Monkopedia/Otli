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


typealias CCodeBuilder = CodeBuilder<CFactory>

fun CCodeBuilder(): CCodeBuilder =
    CodeBuilderBase(
        CFactory(),
        addSemis = true
    )

class CFactory : LangFactory {
    override fun define(
        name: String,
        type: ResolvedType,
        initializer: Symbol?,
        constructorArgs: List<Symbol>?
    ): LocalVar = CppLocalVar(name, type, initializer, constructorArgs)

    override fun funSig(name: String, retType: Symbol?, args: List<LocalVar>): Symbol =
        CFunctionSignature(name, retType ?: CType("void"), args)

    override fun createType(type: ResolvedType): Symbol = CType(type)
}

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

class CppLocalVar(
    override val name: String,
    val type: ResolvedType,
    private val initializer: Symbol?,
    private val constructorArgs: List<Symbol>?
) : LocalVar,
    SymbolContainer {
    private val typeSymbol = CType(type)
    override val symbols: List<Symbol>
        get() = listOfNotNull(typeSymbol, initializer)

    override fun build(builder: CodeStringBuilder) {
        typeSymbol.build(builder)
        builder.append(' ')
        builder.append(name)
        if (constructorArgs != null) {
            builder.append('(')
            constructorArgs.forEachIndexed { index, symbol ->
                if (index != 0) {
                    builder.append(", ")
                }
                symbol.build(builder)
            }
            builder.append(')')
        }
        initializer?.let {
            builder.append(" = ")
            it.build(builder)
        }
    }
}

class CType(private val typeStr: String) : Symbol {
    constructor(type: ResolvedType) : this(type.toString())

    override fun build(builder: CodeStringBuilder) {
        builder.append(typeStr)
    }
}

inline fun CCodeBuilder.include(target: String) {
    addSymbol(PreprocessorSymbol("include \"$target\""))
}

inline fun CCodeBuilder.includeSys(target: String) {
    addSymbol(PreprocessorSymbol("include <$target>"))
}

inline fun CCodeBuilder.define(condition: String) = +PreprocessorSymbol("define $condition")

inline fun CCodeBuilder.ifdef(condition: String, builder: CCodeBuilder.() -> Unit) = block(
    PreprocessorSymbol("ifdef $condition\n"),
    PreprocessorSymbol("endif //$condition"),
    builder
)

inline fun CCodeBuilder.ifndef(condition: String, builder: CCodeBuilder.() -> Unit) = block(
    PreprocessorSymbol("ifndef $condition\n"),
    PreprocessorSymbol("endif //$condition"),
    builder
)

class PreprocessorSymbol(private val target: String) :
    Symbol {
    override val blockSemi: Boolean
        get() = true

    override fun build(builder: CodeStringBuilder) {
        builder.append("#$target")
    }
}

