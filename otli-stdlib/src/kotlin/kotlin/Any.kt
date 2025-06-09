package kotlin

public open class Any public constructor() {
    public open operator fun equals(other: kotlin.Any?): kotlin.Boolean = error("Stub")

    public open fun hashCode(): kotlin.Int = error("Stub")

    public open fun toString(): kotlin.String = error("Stub")
}

public final class Nothing private constructor() {
}

public object Unit {
    public override fun toString(): kotlin.String = error("Stub")
}


