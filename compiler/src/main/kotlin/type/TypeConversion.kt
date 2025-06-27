package com.monkopedia.otli.type

import com.monkopedia.otli.builders.RawCast
import com.monkopedia.otli.builders.Symbol
import com.monkopedia.otli.builders.type

fun WrappedType.canConvert(other: WrappedType): Boolean {
    if (isNative && other.coreType == "int32_t") {
        return true
    }
    return false
}

fun WrappedType.coerce(symbol: Symbol, to: WrappedType): Symbol {
    return RawCast(type(to), symbol)
}
