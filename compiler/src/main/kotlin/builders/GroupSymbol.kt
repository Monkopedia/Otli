package com.monkopedia.otli.builders

class GroupSymbol :
    Symbol,
    SymbolContainer {
    val symbolList = mutableListOf<Symbol>()

    override val blockSemi: Boolean
        get() = true

    override fun build(builder: CodeStringBuilder) {
        for (symbol in symbolList) {
            symbol.build(builder)
            if (!symbol.blockSemi) {
                builder.append(';')
            }
            builder.append('\n')
        }
    }

    override val symbols: List<Symbol>
        get() = symbolList
}

class FileSymbol(val fileName: String, val groupSymbol: GroupSymbol = GroupSymbol()) :
    Symbol by groupSymbol,
    SymbolContainer by groupSymbol {

    override fun build(builder: CodeStringBuilder) {
        builder.file(fileName) {
            groupSymbol.build(this)
        }
    }
}
