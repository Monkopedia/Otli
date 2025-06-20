package com.monkopedia.otli.builders

import org.jetbrains.kotlin.name.FqName

interface Includes {
    val includes: List<Symbol>
}

data class KotlinSymbol(val fqName: FqName)

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

data class Included(val text: String, val include: String, val isSystem: Boolean) : Symbol,
    Includes {
    override val includes: List<Symbol>
        get() = listOf(Include(include, isSystem))
    override val blockSemi: Boolean
        get() = text.isEmpty()

    override fun build(builder: CodeStringBuilder) {
        builder.append(text)
    }

    override fun toString(): String {
        return text
    }
}

class IncludeBlock(val parent: Symbol) : Symbol {
    override val blockSemi: Boolean
        get() = true

    override fun build(builder: CodeStringBuilder) {
        val includes = parent.symbolAndChildren.filterIsInstance<Includes>()
            .flatMap { it.includes }
            .toSet()
            .sortedBy {
                (it as? Include)?.sortString() ?: ("a" + it.toString())
            }
        includes.forEach {
            it.build(builder)
            builder.append("\n")
        }
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
