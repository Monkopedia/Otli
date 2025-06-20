package com.monkopedia.otli.type

import com.monkopedia.otli.builders.Symbol

fun WrappedType.canConvert(other: WrappedType): Boolean {
    return false
}

fun WrappedType.coerce(symbol: Symbol, to: WrappedType): Symbol {
    error("Not implemented yet")
}
