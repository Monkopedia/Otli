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

class HeaderSymbol(
    override val parent: CodeBuilder,
    val fileName: String,
    val groupSymbol: GroupSymbol = GroupSymbol()
) : Symbol by groupSymbol, SymbolContainer by groupSymbol, CodeBuilder {

    override fun add(symbol: Symbol) {
        groupSymbol.symbolList.add(symbol)
    }

    override fun build(builder: CodeStringBuilder) {
        val defName = "__${fileName.uppercase().replace(".", "_")}__"
        builder.append("#ifndef $defName\n")
        builder.append("#define $defName\n")
        groupSymbol.build(builder)
        builder.append("#endif // $defName\n")
    }
}

class FileSymbol(
    override val parent: CodeBuilder,
    val fileName: String,
    val groupSymbol: GroupSymbol = GroupSymbol()
) : Symbol by groupSymbol,
    SymbolContainer by groupSymbol,
    CodeBuilder {

    override fun add(symbol: Symbol) {
        groupSymbol.symbolList.add(symbol)
    }

    init {
        groupSymbol.symbolList.add(IncludeBlock(groupSymbol))
    }

    override fun build(builder: CodeStringBuilder) {
        builder.file(fileName) {
            groupSymbol.build(this)
        }
    }
}
