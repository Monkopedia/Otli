/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("NON_ABSTRACT_FUNCTION_WITH_NO_BODY", "MUST_BE_INITIALIZED_OR_BE_ABSTRACT", "UNUSED_PARAMETER")

package kotlin

/**
 * The `String` class represents character strings. All string literals in Kotlin programs, such as `"abc"`, are
 * implemented as instances of this class.
 */
public class String : Comparable<String>, CharSequence {
    public companion object {}

    /**
     * Returns a string obtained by concatenating this string with the string representation of the given [other] object.
     *
     * @sample samples.text.Strings.stringPlus
     */
    @kotlin.internal.IntrinsicConstEvaluation
    public operator fun plus(other: Any?): String

    @kotlin.internal.IntrinsicConstEvaluation
    public override val length: Int

    /**
     * Returns the character of this string at the specified [index].
     *
     * If the [index] is out of bounds of this string, crashes.
     */
    @kotlin.internal.IntrinsicConstEvaluation
    public override fun get(index: Int): Char

    public override fun subSequence(startIndex: Int, endIndex: Int): CharSequence

    @kotlin.internal.IntrinsicConstEvaluation
    public override fun compareTo(other: String): Int

    /**
     * Indicates if [other] object is equal to this [String].
     *
     * An [other] object is equal to this [String] if and only if it is also a [String],
     * it has the same [length] as this String,
     * and characters at the same positions in each string are equal to each other.
     *
     * @sample samples.text.Strings.stringEquals
     */
    @kotlin.internal.IntrinsicConstEvaluation
    public override fun equals(other: Any?): Boolean

    public override fun hashCode(): Int

    @kotlin.internal.IntrinsicConstEvaluation
    public override fun toString(): String
}
