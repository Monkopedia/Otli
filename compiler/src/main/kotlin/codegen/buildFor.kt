package com.monkopedia.otli.codegen

import com.monkopedia.otli.builders.CodeBuilder
import com.monkopedia.otli.builders.CodeStringBuilder
import com.monkopedia.otli.builders.InlineArrayDefinition
import com.monkopedia.otli.builders.LocalVar
import com.monkopedia.otli.builders.Raw
import com.monkopedia.otli.builders.Symbol
import com.monkopedia.otli.builders.SymbolContainer
import com.monkopedia.otli.builders.captureChildren
import com.monkopedia.otli.builders.op
import com.monkopedia.otli.builders.postfix
import com.monkopedia.otli.builders.prefix
import com.monkopedia.otli.builders.reference
import com.monkopedia.otli.builders.whileLoop
import org.jetbrains.kotlin.ir.expressions.IrWhileLoop

fun CodegenVisitor.buildLoop(loop: IrWhileLoop, data: CodeBuilder): Symbol {
    var extraExpressions: MutableList<Symbol>? = null
    val conditionCapture = data.captureChildren { symbol ->
        if (symbol is LocalVar) {
            data.addSymbol(symbol)
        } else {
            extraExpressions = (extraExpressions ?: mutableListOf()).also { it.add(symbol) }
        }
    }
    val condition = loop.condition.accept(this, conditionCapture)
    extraExpressions?.forEach { data.addSymbol(it) }
    return data.whileLoop(condition) {
        loop.body?.accept(this@buildLoop, this)?.let(::addSymbol)
        extraExpressions?.forEach { addSymbol(it) }
    }
}

interface IteratorSymbol {
    fun initialize(builder: CodeBuilder): Symbol
    fun hasNext(boundSymbol: LocalVar, builder: CodeBuilder): Symbol
    fun next(boundSymbol: LocalVar, builder: CodeBuilder): Symbol
}

interface BoundIterator {
    fun hasNext(builder: CodeBuilder): Symbol
    fun next(builder: CodeBuilder): Symbol
}

class IntRangeIterator(intRange: Symbol, data: CodeBuilder) :
    Symbol,
    SymbolContainer,
    IteratorSymbol {

    val start: Symbol
    val end: Symbol

    // Hacky way to handle removing from the symbol tree since we don't support that really
    var override: Symbol? = null

    init {
        if (intRange is InlineArrayDefinition) {
            start = intRange.symbols[0]
            end = intRange.symbols[1]
        } else {
            error("Unsupported iteration of symbol $intRange")
        }
    }

    private val replacementSymbol = start.op("-", Raw("1"))

    override val symbols: List<Symbol>
        get() = listOf(replacementSymbol)

    override fun initialize(builder: CodeBuilder): Symbol = this

    override fun hasNext(boundSymbol: LocalVar, builder: CodeBuilder): Symbol =
        boundSymbol.reference.op("<", end.op("-", Raw("1")))

    override fun next(boundSymbol: LocalVar, builder: CodeBuilder): Symbol =
        boundSymbol.reference.prefix("++")

    override fun build(builder: CodeStringBuilder) {
        replacementSymbol.build(builder)
    }
}
