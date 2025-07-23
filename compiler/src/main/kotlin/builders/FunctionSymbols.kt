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

inline fun funSig(name: String, retType: Symbol?, args: List<LocalVar>): Symbol =
    CFunctionSignature(name, retType ?: CType("void"), args)

sealed class FunctionBuilder(
    var name: Symbol? = null,
    var retType: Symbol? = null,
    val functionBuilder: CodeBuilder
) {
    protected val args = mutableListOf<LocalVar>()
    abstract val body: CodeBuilder?

    inline fun body(block: BodyBuilder) {
        body?.apply(block)
    }

    fun define(objKey: Any, name: String, type: ResolvedType): LocalVar = functionBuilder
        .define(
            objKey,
            name,
            type
        ).also(args::add)
}

private object EndFunction : Symbol {
    override fun build(builder: CodeStringBuilder) {
        builder.append('}')
    }
}

open class FunctionSymbol(functionBuilder: CodeBuilder) :
    FunctionBuilder(functionBuilder = functionBuilder),
    Symbol,
    SymbolContainer {
    private val block = BlockSymbol(
        functionBuilder,
        this,
        EndFunction
    )
    lateinit var signature: Symbol
    var nameSymbol: Any? = null
    val symbol: Symbol
        get() = block
    override val body: CodeBuilder
        get() = block

    override val symbols: List<Symbol>
        get() = listOfNotNull(signature, name)

    open fun init() {
        signature =
            funSig(
                functionBuilder.scope.allocateName(
                    name?.toString() ?: error("Name was not specified"),
                    nameSymbol
                ),
                retType,
                args
            )
    }

    override fun build(builder: CodeStringBuilder) {
        signature.build(builder)
        builder.append(" {\n")
    }

    override fun toString(): String = signature.toString()
}

inline fun CodeBuilder.function(
    isHeader: Boolean = false,
    functionBuilder: FunctionBuilder.() -> Unit
): Symbol = if (isHeader) {
    functionDeclaration(functionBuilder)
} else {
    val builder =
        varScope {
            FunctionSymbol(this).also(functionBuilder)
        }
    builder.init()
    builder.symbol
}

class FunctionDeclarationSymbol(functionBuilder: CodeBuilder) :
    FunctionBuilder(functionBuilder = functionBuilder) {
    override val body: CodeBuilder?
        get() = null

    fun init(): Symbol = funSig(name?.toString() ?: error("Name was not specified"), retType, args)
}

inline fun CodeBuilder.functionDeclaration(functionBuilder: FunctionBuilder.() -> Unit): Symbol {
    val builder =
        varScope {
            FunctionDeclarationSymbol(this).also(functionBuilder)
        }
    return builder.init()
}
