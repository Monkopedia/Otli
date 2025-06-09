package kotlin

public abstract class Number public constructor() {
    public abstract fun toByte(): kotlin.Byte

    @kotlin.Deprecated("Just is") public open fun toChar(): kotlin.Char = error("Stub")

    public abstract fun toDouble(): kotlin.Double

    public abstract fun toFloat(): kotlin.Float

    public abstract fun toInt(): kotlin.Int

    public abstract fun toLong(): kotlin.Long

    public abstract fun toShort(): kotlin.Short
}
