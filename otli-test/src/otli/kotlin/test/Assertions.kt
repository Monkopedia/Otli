/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

/**
 * A number of helper methods for writing unit tests.
 */
@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package kotlin.test

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/** Asserts that the given [block] returns `true`. */
public inline fun assertTrue(message: String? = null, block: () -> Boolean): Unit = error("Stub")
/** Asserts that the expression is `true` with an optional [message]. */
public fun assertTrue(actual: Boolean, message: String? = null): Unit = error("Stub")
/** Asserts that the given [block] returns `false`. */
public inline fun assertFalse(message: String? = null, block: () -> Boolean): Unit = error("Stub")
/** Asserts that the expression is `false` with an optional [message]. */
public fun assertFalse(actual: Boolean, message: String? = null): Unit = error("Stub")
/** Asserts that the [expected] value is equal to the [actual] value, with an optional [message]. */
public fun <T> assertEquals(expected: T, actual: T, message: String? = null): Unit = error("Stub")
/** Asserts that the difference between the [actual] and the [expected] is within an [absoluteTolerance], with an optional [message]. */
@SinceKotlin("1.5")
public fun assertEquals(expected: Double, actual: Double, absoluteTolerance: Double, message: String? = null): Unit = error("Stub")
/** Asserts that the difference between the [actual] and the [expected] is within an [absoluteTolerance], with an optional [message]. */
@SinceKotlin("1.5")
public fun assertEquals(expected: Float, actual: Float, absoluteTolerance: Float, message: String? = null): Unit = error("Stub")
/** Asserts that the [actual] value is not equal to the illegal value, with an optional [message]. */
public fun <T> assertNotEquals(illegal: T, actual: T, message: String? = null): Unit = error("Stub")
/** Asserts that the difference between the [actual] and the [illegal] is not within an [absoluteTolerance], with an optional [message]. */
@SinceKotlin("1.5")
public fun assertNotEquals(illegal: Double, actual: Double, absoluteTolerance: Double, message: String? = null): Unit = error("Stub")
/** Asserts that the difference between the [actual] and the [illegal] is not within an [absoluteTolerance], with an optional [message]. */
@SinceKotlin("1.5")
public fun assertNotEquals(illegal: Float, actual: Float, absoluteTolerance: Float, message: String? = null): Unit = error("Stub")
/** Asserts that [expected] is the same instance as [actual], with an optional [message]. */
public fun <T> assertSame(expected: T, actual: T, message: String? = null): Unit = error("Stub")
/** Asserts that [actual] is not the same instance as [illegal], with an optional [message]. */
public fun <T> assertNotSame(illegal: T, actual: T, message: String? = null): Unit = error("Stub")
/**
 * Asserts that [value] is of type [T], with an optional [message].
 *
 * Note that due to type erasure the type check may be partial (e.g. `assertIs<List<String>>(value)`
 * only checks for the class being [List] and not the type of its elements because it's erased).
 */
@SinceKotlin("1.5")
public inline fun <reified T> assertIs(value: Any?, message: String? = null): T = error("Stub")

@PublishedApi
internal fun assertIsOfType(value: Any?, type: KType, result: Boolean, message: String?): Unit = error("Stub")
/**
 * Asserts that [value] is not of type [T], with an optional [message].
 *
 * Note that due to type erasure the type check may be partial (e.g. `assertIsNot<List<String>>(value)`
 * only checks for the class being [List] and not the type of its elements because it's erased).
 */
@SinceKotlin("1.5")
public inline fun <reified T> assertIsNot(value: Any?, message: String? = null): Unit = error("Stub")
@PublishedApi
internal fun assertIsNotOfType(@Suppress("UNUSED_PARAMETER") value: Any?, type: KType, result: Boolean, message: String?): Unit = error("Stub")
/** Asserts that the [actual] value is not `null`, with an optional [message]. */
public fun <T : Any> assertNotNull(actual: T?, message: String? = null): T = error("Stub")

/** Asserts that the [actual] value is not `null`, with an optional [message] and a function [block] to process the not-null value. */
public inline fun <T : Any, R> assertNotNull(actual: T?, message: String? = null, block: (T) -> R): Unit = error("Stub")

/** Asserts that the [actual] value is `null`, with an optional [message]. */
public fun assertNull(actual: Any?, message: String? = null): Unit = error("Stub")
/** Asserts that the [iterable] contains the specified [element], with an optional [message]. */
@SinceKotlin("1.5")
public fun <T> assertContains(iterable: Iterable<T>, element: T, message: String? = null): Unit = error("Stub")

/** Asserts that the [sequence] contains the specified [element], with an optional [message]. */
//@SinceKotlin("1.5")
//public fun <T> assertContains(sequence: Sequence<T>, element: T, message: String? = null): Unit = error("Stub")

/** Asserts that the [array] contains the specified [element], with an optional [message]. */
@SinceKotlin("1.5")
public fun <T> assertContains(array: Array<T>, element: T, message: String? = null): Unit = error("Stub")
/** Asserts that the [array] contains the specified [element], with an optional [message]. */
@SinceKotlin("1.5")
public fun assertContains(array: ByteArray, element: Byte, message: String? = null): Unit = error("Stub")
/** Asserts that the [array] contains the specified [element], with an optional [message]. */
@SinceKotlin("1.5")
public fun assertContains(array: ShortArray, element: Short, message: String? = null): Unit = error("Stub")
/** Asserts that the [array] contains the specified [element], with an optional [message]. */
@SinceKotlin("1.5")
public fun assertContains(array: IntArray, element: Int, message: String? = null): Unit = error("Stub")
/** Asserts that the [array] contains the specified [element], with an optional [message]. */
@SinceKotlin("1.5")
public fun assertContains(array: LongArray, element: Long, message: String? = null): Unit = error("Stub")
/** Asserts that the [array] contains the specified [element], with an optional [message]. */
@SinceKotlin("1.5")
public fun assertContains(array: BooleanArray, element: Boolean, message: String? = null): Unit = error("Stub")
/** Asserts that the [array] contains the specified [element], with an optional [message]. */
@SinceKotlin("1.5")
public fun assertContains(array: CharArray, element: Char, message: String? = null): Unit = error("Stub")
/** Asserts that the [array] contains the specified [element], with an optional [message]. */
@SinceKotlin("1.5")
public fun assertContains(array: UByteArray, element: UByte, message: String? = null): Unit = error("Stub")
/** Asserts that the [array] contains the specified [element], with an optional [message]. */
@SinceKotlin("1.5")
public fun assertContains(array: UShortArray, element: UShort, message: String? = null): Unit = error("Stub")
/** Asserts that the [array] contains the specified [element], with an optional [message]. */
@SinceKotlin("1.5")
public fun assertContains(array: UIntArray, element: UInt, message: String? = null): Unit = error("Stub")
/** Asserts that the [array] contains the specified [element], with an optional [message]. */
@SinceKotlin("1.5")
public fun assertContains(array: ULongArray, element: ULong, message: String? = null): Unit = error("Stub")
private inline fun <A, E> assertArrayContains(
    array: A,
    element: E,
    message: String? = null,
    contains: A.(E) -> Boolean,
    crossinline contentToString: A.() -> String
): Unit = error("Stub")
/** Asserts that the [range] contains the specified [value], with an optional [message]. */
@SinceKotlin("1.5")
public fun assertContains(range: IntRange, value: Int, message: String? = null): Unit = error("Stub")
/** Asserts that the [range] contains the specified [value], with an optional [message]. */
@SinceKotlin("1.5")
public fun assertContains(range: LongRange, value: Long, message: String? = null): Unit = error("Stub")
/** Asserts that the [range] contains the specified [value], with an optional [message]. */
@SinceKotlin("1.5")
public fun <T : Comparable<T>> assertContains(range: ClosedRange<T>, value: T, message: String? = null): Unit = error("Stub")
/** Asserts that the [range] contains the specified [value], with an optional [message]. */
@SinceKotlin("2.2")
public fun <T : Comparable<T>> assertContains(range: OpenEndRange<T>, value: T, message: String? = null): Unit = error("Stub")
/** Asserts that the [range] contains the specified [value], with an optional [message]. */
@SinceKotlin("1.5")
public fun assertContains(range: CharRange, value: Char, message: String? = null): Unit = error("Stub")
/** Asserts that the [range] contains the specified [value], with an optional [message]. */
@SinceKotlin("1.5")
public fun assertContains(range: UIntRange, value: UInt, message: String? = null): Unit = error("Stub")
/** Asserts that the [range] contains the specified [value], with an optional [message]. */
@SinceKotlin("1.5")
public fun assertContains(range: ULongRange, value: ULong, message: String? = null): Unit = error("Stub")
private inline fun <R, V> assertRangeContains(range: R, value: V, message: String? = null, contains: R.(V) -> Boolean): Unit = error("Stub")

/** Asserts that the [map] contains the specified [key], with an optional [message]. */
@SinceKotlin("1.5")
public fun <K, V> assertContains(map: Map<K, V>, key: K, message: String? = null): Unit = error("Stub")
/**
 * Asserts that the [charSequence] contains the specified [char], with an optional [message].
 *
 * @param ignoreCase `true` to ignore character case when comparing characters. By default `false`.
 */
//@SinceKotlin("1.5")
//public fun assertContains(charSequence: CharSequence, char: Char, ignoreCase: Boolean = false, message: String? = null): Unit = error("Stub")

/**
 * Asserts that the [charSequence] contains the specified [other] char sequence as a substring, with an optional [message].
 *
 * @param ignoreCase `true` to ignore character case when comparing strings. By default `false`.
 */
@SinceKotlin("1.5")
public fun assertContains(charSequence: CharSequence, other: CharSequence, ignoreCase: Boolean = false, message: String? = null): Unit = error("Stub")

/**
 * Asserts that the [expected] iterable is *structurally* equal to the [actual] iterable, with an optional [message].
 *
 * Two iterables are considered structurally equal if they have the same size,
 * and elements at corresponding positions, following the iteration order, are equal.
 * Elements are compared for equality using the [equals][Any.equals] function.
 * For floating point numbers, this means `NaN` is equal to itself and `-0.0` is not equal to `0.0`.
 *
 * The iterables are also considered equal if both are `null`.
 */
@SinceKotlin("1.5")
public fun <T> assertContentEquals(expected: Iterable<T>?, actual: Iterable<T>?, message: String? = null): Unit = error("Stub")

@SinceKotlin("1.5")
@Deprecated("'assertContentEquals' for Set arguments is ambiguous. Use 'assertEquals' to compare content with the unordered set equality, or cast one of arguments to Iterable to compare the set elements in order of iteration.",
            level = DeprecationLevel.ERROR,
            replaceWith = ReplaceWith("assertContentEquals(expected, actual?.asIterable(), message)"))
public fun <T> assertContentEquals(expected: Set<T>?, actual: Set<T>?, message: String? = null): Unit =
    error("Stub")

/**
 * Asserts that the [expected] sequence is *structurally* equal to the [actual] sequence, with an optional [message].
 *
 * Two sequences are considered structurally equal if they have the same size,
 * and elements at corresponding positions, following the iteration order, are equal.
 * Elements are compared for equality using the [equals][Any.equals] function.
 * For floating point numbers, this means `NaN` is equal to itself and `-0.0` is not equal to `0.0`.
 *
 * The sequences are also considered equal if both are `null`.
 */
//@SinceKotlin("1.5")
//public fun <T> assertContentEquals(expected: Sequence<T>?, actual: Sequence<T>?, message: String? = null): Unit = error("Stub")
/**
 * Asserts that the [expected] array is *structurally* equal to the [actual] array, with an optional [message].
 *
 * Two arrays are considered structurally equal if they have the same size, and elements at corresponding indices are equal.
 * Elements are compared for equality using the [equals][Any.equals] function.
 * For floating point numbers, this means `NaN` is equal to itself and `-0.0` is not equal to `0.0`.
 *
 * The arrays are also considered equal if both are `null`.
 */
@SinceKotlin("1.5")
public fun <T> assertContentEquals(expected: Array<T>?, actual: Array<T>?, message: String? = null): Unit = error("Stub")
/**
 * Asserts that the [expected] array is *structurally* equal to the [actual] array, with an optional [message].
 *
 * Two arrays are considered structurally equal if they have the same size, and elements at corresponding indices are equal.
 *
 * The arrays are also considered equal if both are `null`.
 */
@SinceKotlin("1.5")
public fun assertContentEquals(expected: ByteArray?, actual: ByteArray?, message: String? = null): Unit = error("Stub")
/**
 * Asserts that the [expected] array is *structurally* equal to the [actual] array, with an optional [message].
 *
 * Two arrays are considered structurally equal if they have the same size, and elements at corresponding indices are equal.
 *
 * The arrays are also considered equal if both are `null`.
 */
@SinceKotlin("1.5")
public fun assertContentEquals(expected: ShortArray?, actual: ShortArray?, message: String? = null): Unit = error("Stub")
/**
 * Asserts that the [expected] array is *structurally* equal to the [actual] array, with an optional [message].
 *
 * Two arrays are considered structurally equal if they have the same size, and elements at corresponding indices are equal.
 *
 * The arrays are also considered equal if both are `null`.
 */
@SinceKotlin("1.5")
public fun assertContentEquals(expected: IntArray?, actual: IntArray?, message: String? = null): Unit = error("Stub")
/**
 * Asserts that the [expected] array is *structurally* equal to the [actual] array, with an optional [message].
 *
 * Two arrays are considered structurally equal if they have the same size, and elements at corresponding indices are equal.
 *
 * The arrays are also considered equal if both are `null`.
 */
@SinceKotlin("1.5")
public fun assertContentEquals(expected: LongArray?, actual: LongArray?, message: String? = null): Unit = error("Stub")
/**
 * Asserts that the [expected] array is *structurally* equal to the [actual] array, with an optional [message].
 *
 * Two arrays are considered structurally equal if they have the same size, and elements at corresponding indices are equal.
 * Elements are compared for equality using the [equals][Any.equals] function.
 * For floating point numbers, this means `NaN` is equal to itself and `-0.0` is not equal to `0.0`.
 *
 * The arrays are also considered equal if both are `null`.
 */
@SinceKotlin("1.5")
public fun assertContentEquals(expected: FloatArray?, actual: FloatArray?, message: String? = null): Unit = error("Stub")
/**
 * Asserts that the [expected] array is *structurally* equal to the [actual] array, with an optional [message].
 *
 * Two arrays are considered structurally equal if they have the same size, and elements at corresponding indices are equal.
 * Elements are compared for equality using the [equals][Any.equals] function.
 * For floating point numbers, this means `NaN` is equal to itself and `-0.0` is not equal to `0.0`.
 *
 * The arrays are also considered equal if both are `null`.
 */
@SinceKotlin("1.5")
public fun assertContentEquals(expected: DoubleArray?, actual: DoubleArray?, message: String? = null): Unit = error("Stub")
/**
 * Asserts that the [expected] array is *structurally* equal to the [actual] array, with an optional [message].
 *
 * Two arrays are considered structurally equal if they have the same size, and elements at corresponding indices are equal.
 *
 * The arrays are also considered equal if both are `null`.
 */
@SinceKotlin("1.5")
public fun assertContentEquals(expected: BooleanArray?, actual: BooleanArray?, message: String? = null): Unit = error("Stub")
/**
 * Asserts that the [expected] array is *structurally* equal to the [actual] array, with an optional [message].
 *
 * Two arrays are considered structurally equal if they have the same size, and elements at corresponding indices are equal.
 *
 * The arrays are also considered equal if both are `null`.
 */
@SinceKotlin("1.5")
public fun assertContentEquals(expected: CharArray?, actual: CharArray?, message: String? = null): Unit = error("Stub")
/**
 * Asserts that the [expected] array is *structurally* equal to the [actual] array, with an optional [message].
 *
 * Two arrays are considered structurally equal if they have the same size, and elements at corresponding indices are equal.
 *
 * The arrays are also considered equal if both are `null`.
 */
@SinceKotlin("1.5")
public fun assertContentEquals(expected: UByteArray?, actual: UByteArray?, message: String? = null): Unit = error("Stub")
/**
 * Asserts that the [expected] array is *structurally* equal to the [actual] array, with an optional [message].
 *
 * Two arrays are considered structurally equal if they have the same size, and elements at corresponding indices are equal.
 *
 * The arrays are also considered equal if both are `null`.
 */
@SinceKotlin("1.5")
public fun assertContentEquals(expected: UShortArray?, actual: UShortArray?, message: String? = null): Unit = error("Stub")
/**
 * Asserts that the [expected] array is *structurally* equal to the [actual] array, with an optional [message].
 *
 * Two arrays are considered structurally equal if they have the same size, and elements at corresponding indices are equal.
 *
 * The arrays are also considered equal if both are `null`.
 */
@SinceKotlin("1.5")
public fun assertContentEquals(expected: UIntArray?, actual: UIntArray?, message: String? = null): Unit = error("Stub")
/**
 * Asserts that the [expected] array is *structurally* equal to the [actual] array, with an optional [message].
 *
 * Two arrays are considered structurally equal if they have the same size, and elements at corresponding indices are equal.
 *
 * The arrays are also considered equal if both are `null`.
 */
@SinceKotlin("1.5")
public fun assertContentEquals(expected: ULongArray?, actual: ULongArray?, message: String? = null): Unit = error("Stub")
/** Marks a test as having failed if this point in the execution path is reached, with an optional [message]. */
public fun fail(message: String? = null): Nothing = error("Stub")
/**
 * Marks a test as having failed if this point in the execution path is reached, with an optional [message]
 * and [cause] exception.
 *
 * The [cause] exception is set as the root cause of the test failure.
 */
@SinceKotlin("1.4")
public fun fail(message: String? = null, cause: Throwable? = null): Nothing = error("Stub")
/** Asserts that given function [block] returns the given [expected] value. */
public inline fun <T> expect(expected: T, block: () -> T): Unit = error("Stub")

/** Asserts that given function [block] returns the given [expected] value and use the given [message] if it fails. */
public inline fun <T> expect(expected: T, message: String?, block: () -> T): Unit = error("Stub")

/**
 * Asserts that given function [block] fails by throwing an exception.
 *
 * @return An exception that was expected to be thrown and was successfully caught.
 * The returned exception can be inspected further, for example by asserting its property values.
 */
public inline fun assertFails(block: () -> Unit): Throwable =
    error("Stub")

/**
 * Asserts that given function [block] fails by throwing an exception.
 *
 * If the assertion fails, the specified [message] is used unless it is null as a prefix for the failure message.
 *
 * @return An exception that was expected to be thrown and was successfully caught.
 * The returned exception can be inspected further, for example by asserting its property values.
 */
@SinceKotlin("1.1")
public inline fun assertFails(message: String?, block: () -> Unit): Throwable =
    error("Stub")

/** Asserts that a [block] fails with a specific exception of type [T] being thrown.
 *
 * If the assertion fails, the specified [message] is used unless it is null as a prefix for the failure message.
 *
 * @return An exception of the expected exception type [T] that successfully caught.
 * The returned exception can be inspected further, for example by asserting its property values.
 */
public inline fun <reified T : Throwable> assertFailsWith(message: String? = null, block: () -> Unit): T =
    error("Stub")

/**
 * Asserts that a [block] fails with a specific exception of type [exceptionClass] being thrown.
 *
 * @return An exception of the expected exception type [T] that successfully caught.
 * The returned exception can be inspected further, for example by asserting its property values.
 */
public inline fun <T : Throwable> assertFailsWith(exceptionClass: KClass<T>, block: () -> Unit): T = error("Stub")

/**
 * Asserts that a [block] fails with a specific exception of type [exceptionClass] being thrown.
 *
 * If the assertion fails, the specified [message] is used unless it is null as a prefix for the failure message.
 *
 * @return An exception of the expected exception type [T] that successfully caught.
 * The returned exception can be inspected further, for example by asserting its property values.
 */
public inline fun <T : Throwable> assertFailsWith(exceptionClass: KClass<T>, message: String?, block: () -> Unit): T =
    error("Stub")

