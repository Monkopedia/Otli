/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.collections

/**
 * Returns `true` if all elements match the given [predicate].
 * 
 * Note that if the collection contains no elements, the function returns `true`
 * because there are no elements in it that _do not_ match the predicate.
 * See a more detailed explanation of this logic concept in ["Vacuous truth"](https://en.wikipedia.org/wiki/Vacuous_truth) article.
 * 
 * @sample samples.collections.Collections.Aggregates.all
 */
public inline fun <T> Iterable<T>.all(predicate: (T) -> Boolean): Boolean = error("Stub")
/**
 * Returns `true` if [element] is found in the collection.
 */
public operator fun <@kotlin.internal.OnlyInputTypes T> Iterable<T>.contains(element: T): Boolean = error("Stub")
/**
 * Returns first index of [element], or -1 if the collection does not contain element.
 */
public fun <@kotlin.internal.OnlyInputTypes T> Iterable<T>.indexOf(element: T): Int = error("Stub")
