/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin


/**
 * The common base class of all enum classes.
 * See the [Kotlin language documentation](https://kotlinlang.org/docs/reference/enum-classes.html) for more
 * information on enum classes.
 */
public abstract class Enum<E : Enum<E>>
    public constructor()
    //public constructor(@kotlin.internal.IntrinsicConstEvaluation public val name: String, public val ordinal: Int)
    : Comparable<E> {
    public companion object {}

    @kotlin.internal.IntrinsicConstEvaluation public val name: String
    get() = error("Stub")
    public val ordinal: Int
    get() = error("Stub")

    /**
     * Returns the name of this enum constant, exactly as declared in its enum declaration.
     */

    /**
     * Returns the ordinal of this enumeration constant (its position in its enum declaration, where the initial constant
     * is assigned an ordinal of zero).
     */

    public final override fun compareTo(other: E): Int = error("Stub")

    public final override fun equals(other: Any?): Boolean = error("Stub")
    public final override fun hashCode(): Int = error("Stub")
    public override fun toString(): String = error("Stub")

    /**
     * Returns an array containing the constants of this enum type, in the order they're declared.
     * This method may be used to iterate over the constants.
     * @values
     */

    /**
     * Returns the enum constant of this type with the specified name. The string must match exactly an identifier used to declare an enum constant in this type. (Extraneous whitespace characters are not permitted.)
     * @throws IllegalArgumentException if this enum type has no constant with the specified name
     * @valueOf
     */

    /**
     * Returns an immutable [kotlin.enums.EnumEntries] list containing the constants of this enum type, in the order they're declared.
     * @entries
     */
}
