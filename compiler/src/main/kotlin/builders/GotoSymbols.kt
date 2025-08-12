package com.monkopedia.otli.builders

data class GotoTarget(val label: String) : Symbol {
    override val blockSemi: Boolean
        get() = true
    override fun build(builder: CodeStringBuilder) {
        builder.endBlock()
        builder.append("$label:")
        builder.startBlock()
    }
}

data class Goto(val target: GotoTarget) : Symbol {
    override fun build(builder: CodeStringBuilder) {
        builder.append("goto ${target.label}")
    }
}
