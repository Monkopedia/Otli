package com.monkopedia.otli.builders

fun ifSymbol(
    parent: CodeBuilder,
    condition: Symbol,
    elseBlock: Symbol,
    bodyBuilder: BodyBuilder
): BlockSymbol = block(parent, IfCondition(condition), elseBlock, bodyBuilder)

fun elseSymbol(parent: CodeBuilder, bodyBuilder: BodyBuilder): BlockSymbol =
    block(parent, ElseSymbol, EndIfSymbol, bodyBuilder)

private class IfCondition(val symbol: Symbol) :
    Symbol,
    SymbolContainer {

    override val symbols: List<Symbol>
        get() = listOf(symbol)

    override fun build(builder: CodeStringBuilder) {
        builder.append("if (")
        symbol.build(builder)
        builder.append(") {\n")
    }
}

data class IfBuilder(
    var condition: Symbol? = null,
    var ifBlock: BodyBuilder? = null,
    var elseBlock: BodyBuilder? = null
) {
    fun ifBlock(body: BodyBuilder) {
        ifBlock = body
    }

    fun elseBlock(body: BodyBuilder) {
        elseBlock = body
    }
}

private object ElseSymbol : Symbol {
    override fun build(builder: CodeStringBuilder) {
        builder.endBlock()
        builder.append("} else {\n")
        builder.startBlock()
    }
}

private object EndIfSymbol : Symbol {
    override fun build(builder: CodeStringBuilder) {
        builder.append("}\n")
    }
}

fun CodeBuilder.buildIf(builder: IfBuilder.() -> Unit): Symbol {
    val builtIf = IfBuilder().also(builder)
    return ifSymbol(
        this,
        builtIf.condition ?: error("Condition is required for if"),
        elseBlock = builtIf.elseBlock?.let { elseSymbol(this, it) } ?: EndIfSymbol,
        bodyBuilder = builtIf.ifBlock ?: error("ifBlock is required for if")
    )
}
