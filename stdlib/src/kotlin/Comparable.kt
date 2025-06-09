package kotlin

public interface CharSequence {
    public abstract val length: kotlin.Int

    public abstract operator fun get(index: kotlin.Int): kotlin.Char

    public abstract fun subSequence(startIndex: kotlin.Int, endIndex: kotlin.Int): kotlin.CharSequence
}

public interface Comparable<in T> {
    public abstract operator fun compareTo(other: T): kotlin.Int
}

