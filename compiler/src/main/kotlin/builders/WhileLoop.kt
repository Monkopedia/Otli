package com.monkopedia.otli.builders

fun CodeBuilder.whileLoop(condition: Symbol, builder: BodyBuilder) = block(
    WhileCondition(condition),
    WhileEnd,
    builder
)

fun CodeBuilder.doWhileLoop(condition: Symbol, builder: BodyBuilder) = block(
    DoWhileStart,
    DoWhileCondition(condition),
    builder
)

object WhileEnd : Symbol {
    override fun build(builder: CodeStringBuilder) {
        builder.append("}\n")
    }
}

class WhileCondition(val condition: Symbol) :
    Symbol,
    SymbolContainer {

    override val symbols: List<Symbol>
        get() = listOf(condition)

    override fun build(builder: CodeStringBuilder) {
        builder.append("while (")
        condition.build(builder)
        builder.append(") {\n")
    }
}

object DoWhileStart : Symbol {
    override fun build(builder: CodeStringBuilder) {
        builder.append("do {\n")
    }
}

class DoWhileCondition(val condition: Symbol) :
    Symbol,
    SymbolContainer {

    override val symbols: List<Symbol>
        get() = listOf(condition)

    override fun build(builder: CodeStringBuilder) {
        builder.append("} while (")
        condition.build(builder)
        builder.append(");\n")
    }
}
