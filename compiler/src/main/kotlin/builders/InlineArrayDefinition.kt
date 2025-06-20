package com.monkopedia.otli.builders

class InlineArrayDefinition(override val symbols: List<Symbol>) :
    Symbol,
    SymbolContainer {

    constructor(vararg symbols: Symbol) :
        this(symbols.toList())

    override fun build(builder: CodeStringBuilder) {
        builder.append("{")
        symbols.firstOrNull()?.build(builder)
        symbols.drop(1).forEach { symbol ->
            builder.append(", ")
            symbol.build(builder)
        }
        builder.append("}")
    }
}

inline fun buildArray(builder: MutableList<Symbol>.() -> Unit): Symbol = buildList {
    builder()
}.let(::InlineArrayDefinition)

val NULL = Raw("NULL")

fun nullArray(): Symbol = buildArray { add(NULL) }
