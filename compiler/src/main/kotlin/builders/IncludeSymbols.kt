package com.monkopedia.otli.builders

import kotlinx.serialization.Serializable
import org.jetbrains.kotlin.name.FqName

interface Predefines {
    val predefines: List<Symbol>
}

data class KotlinSymbol(val fqName: FqName)

@Serializable
data class Include(val include: String, val isSystem: Boolean) : Symbol {
    override fun build(builder: CodeStringBuilder) {
        builder.append("#include ")
        if (isSystem) {
            builder.append('<')
        } else {
            builder.append('"')
        }
        builder.append(include)
        if (isSystem) {
            builder.append('>')
        } else {
            builder.append('"')
        }
    }

    fun sortString(): String {
        if (isSystem) {
            return "a" + include
        } else {
            return "b" + include
        }
    }
}

data class Define(val name: String, val value: Symbol) :
    Symbol,
    SymbolContainer {

    override val symbols: List<Symbol>
        get() = listOf(value)

    override fun build(builder: CodeStringBuilder) {
        builder.append("#define ")
        builder.append(name)
        builder.append(' ')
        value.build(builder)
    }
}

data class DefineReference(val define: Define, override val type: ResolvedType) :
    Predefines,
    Symbol by Empty,
    TypedLocalVar {
    override val predefines: List<Symbol>
        get() = listOf(define)
    override val name: String
        get() = define.name
    override var isExtern: Boolean
        get() = false
        set(value) {}
}

data class Included(val text: String, val symbol: Symbol) :
    Symbol,
    Predefines {
    override val predefines: List<Symbol>
        get() = listOf(symbol)
    override val blockSemi: Boolean
        get() = text.isEmpty()
    constructor(
        text: String,
        include: String,
        isSystem: Boolean
    ) : this(text, Include(include, isSystem))

    override fun build(builder: CodeStringBuilder) {
        builder.append(text)
    }

    override fun toString(): String = text
}

class PreprocessorBlock(val parent: Symbol) : Symbol {
    override val blockSemi: Boolean
        get() = true

    override fun build(builder: CodeStringBuilder) {
        val predefs = parent.symbolAndChildren.filterIsInstance<Predefines>()
            .flatMap { it.predefines }
        val defines = predefs.filterIsInstance<Define>()
        val includes = (predefs - defines).toSet().toMutableSet()
        for (def in defines) {
            val defIncludes = def.symbolAndChildren.filterIsInstance<Predefines>()
                .flatMap { it.predefines }
                .toSet()
                .sortedIncludes()
            for (inc in defIncludes) {
                if (inc in includes) {
                    includes -= inc
                    inc.build(builder)
                    builder.append("\n")
                }
            }
            def.build(builder)
            builder.append("\n")
        }
        includes.sortedIncludes().forEach {
            it.build(builder)
            builder.append("\n")
        }
    }

    private fun Iterable<Symbol>.sortedIncludes(): List<Symbol> = sortedBy {
        (it as? Include)?.sortString() ?: ("a" + it.toString())
    }
}

val Symbol.symbolAndChildren: Sequence<Symbol>
    get() = sequence {
        symbolAndChildren(this@symbolAndChildren)
    }

private suspend fun SequenceScope<Symbol>.symbolAndChildren(symbol: Symbol) {
    yield(symbol)
    if (symbol is SymbolContainer) {
        symbol.symbols.forEach { symbolAndChildren(it) }
    }
}
