/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// Auto-generated file. DO NOT EDIT!
// Generated by: org.jetbrains.kotlin.generators.builtins.iterators.GenerateIterators

package kotlin.collections

import kotlin.*

/**
 * An iterator over a sequence of values of type `Byte`.
 *
 * This is a substitute for `Iterator<Byte>` that provides a specialized version of `next(): T` method: `nextByte(): Byte`
 * and has a special handling by the compiler to avoid platform-specific boxing conversions as a performance optimization.
 *
 * In the following example:
 *
 * ```kotlin
 * class ByteContainer(private val data: ByteArray) {
 *
 *     // ByteIterator instead of Iterator<Byte> in the signature
 *     operator fun iterator(): ByteIterator = object : ByteIterator() {
 *         private var idx = 0
 *
 *         override fun nextByte(): Byte {
 *             if (!hasNext()) throw NoSuchElementException()
 *             return data[idx++]
 *         }
 *
 *         override fun hasNext(): Boolean = idx < data.size
 *     }
 * }
 *
 * for (element in ByteContainer(byteArrayOf(1, 2, 3))) {
 *     ... handle element ...
 * }
 * ```
 * No boxing conversion is performed during the for-loop iteration.
 * Note that the iterator itself will still be allocated.
 */
public abstract class ByteIterator : Iterator<Byte> {
    final override fun next(): Byte = nextByte()


    /**
     * Returns the next element in the iteration without boxing conversion.
     * @throws NoSuchElementException if the iteration has no next element.
     */
    public abstract fun nextByte(): Byte
}

/**
 * An iterator over a sequence of values of type `Char`.
 *
 * This is a substitute for `Iterator<Char>` that provides a specialized version of `next(): T` method: `nextChar(): Char`
 * and has a special handling by the compiler to avoid platform-specific boxing conversions as a performance optimization.
 *
 * In the following example:
 *
 * ```kotlin
 * class CharContainer(private val data: CharArray) {
 *
 *     // CharIterator instead of Iterator<Char> in the signature
 *     operator fun iterator(): CharIterator = object : CharIterator() {
 *         private var idx = 0
 *
 *         override fun nextChar(): Char {
 *             if (!hasNext()) throw NoSuchElementException()
 *             return data[idx++]
 *         }
 *
 *         override fun hasNext(): Boolean = idx < data.size
 *     }
 * }
 *
 * for (element in CharContainer(charArrayOf('1', '2', '3'))) {
 *     ... handle element ...
 * }
 * ```
 * No boxing conversion is performed during the for-loop iteration.
 * Note that the iterator itself will still be allocated.
 */
public abstract class CharIterator : Iterator<Char> {
    final override fun next(): Char = nextChar()


    /**
     * Returns the next element in the iteration without boxing conversion.
     * @throws NoSuchElementException if the iteration has no next element.
     */
    public abstract fun nextChar(): Char
}

/**
 * An iterator over a sequence of values of type `Short`.
 *
 * This is a substitute for `Iterator<Short>` that provides a specialized version of `next(): T` method: `nextShort(): Short`
 * and has a special handling by the compiler to avoid platform-specific boxing conversions as a performance optimization.
 *
 * In the following example:
 *
 * ```kotlin
 * class ShortContainer(private val data: ShortArray) {
 *
 *     // ShortIterator instead of Iterator<Short> in the signature
 *     operator fun iterator(): ShortIterator = object : ShortIterator() {
 *         private var idx = 0
 *
 *         override fun nextShort(): Short {
 *             if (!hasNext()) throw NoSuchElementException()
 *             return data[idx++]
 *         }
 *
 *         override fun hasNext(): Boolean = idx < data.size
 *     }
 * }
 *
 * for (element in ShortContainer(shortArrayOf(1, 2, 3))) {
 *     ... handle element ...
 * }
 * ```
 * No boxing conversion is performed during the for-loop iteration.
 * Note that the iterator itself will still be allocated.
 */
public abstract class ShortIterator : Iterator<Short> {
    final override fun next(): Short = nextShort()


    /**
     * Returns the next element in the iteration without boxing conversion.
     * @throws NoSuchElementException if the iteration has no next element.
     */
    public abstract fun nextShort(): Short
}

/**
 * An iterator over a sequence of values of type `Int`.
 *
 * This is a substitute for `Iterator<Int>` that provides a specialized version of `next(): T` method: `nextInt(): Int`
 * and has a special handling by the compiler to avoid platform-specific boxing conversions as a performance optimization.
 *
 * In the following example:
 *
 * ```kotlin
 * class IntContainer(private val data: IntArray) {
 *
 *     // IntIterator instead of Iterator<Int> in the signature
 *     operator fun iterator(): IntIterator = object : IntIterator() {
 *         private var idx = 0
 *
 *         override fun nextInt(): Int {
 *             if (!hasNext()) throw NoSuchElementException()
 *             return data[idx++]
 *         }
 *
 *         override fun hasNext(): Boolean = idx < data.size
 *     }
 * }
 *
 * for (element in IntContainer(intArrayOf(1, 2, 3))) {
 *     ... handle element ...
 * }
 * ```
 * No boxing conversion is performed during the for-loop iteration.
 * Note that the iterator itself will still be allocated.
 */
public abstract class IntIterator : Iterator<Int> {
    final override fun next(): Int = nextInt()


    /**
     * Returns the next element in the iteration without boxing conversion.
     * @throws NoSuchElementException if the iteration has no next element.
     */
    public abstract fun nextInt(): Int
}

/**
 * An iterator over a sequence of values of type `Long`.
 *
 * This is a substitute for `Iterator<Long>` that provides a specialized version of `next(): T` method: `nextLong(): Long`
 * and has a special handling by the compiler to avoid platform-specific boxing conversions as a performance optimization.
 *
 * In the following example:
 *
 * ```kotlin
 * class LongContainer(private val data: LongArray) {
 *
 *     // LongIterator instead of Iterator<Long> in the signature
 *     operator fun iterator(): LongIterator = object : LongIterator() {
 *         private var idx = 0
 *
 *         override fun nextLong(): Long {
 *             if (!hasNext()) throw NoSuchElementException()
 *             return data[idx++]
 *         }
 *
 *         override fun hasNext(): Boolean = idx < data.size
 *     }
 * }
 *
 * for (element in LongContainer(longArrayOf(1, 2, 3))) {
 *     ... handle element ...
 * }
 * ```
 * No boxing conversion is performed during the for-loop iteration.
 * Note that the iterator itself will still be allocated.
 */
public abstract class LongIterator : Iterator<Long> {
    final override fun next(): Long = nextLong()


    /**
     * Returns the next element in the iteration without boxing conversion.
     * @throws NoSuchElementException if the iteration has no next element.
     */
    public abstract fun nextLong(): Long
}

/**
 * An iterator over a sequence of values of type `Float`.
 *
 * This is a substitute for `Iterator<Float>` that provides a specialized version of `next(): T` method: `nextFloat(): Float`
 * and has a special handling by the compiler to avoid platform-specific boxing conversions as a performance optimization.
 *
 * In the following example:
 *
 * ```kotlin
 * class FloatContainer(private val data: FloatArray) {
 *
 *     // FloatIterator instead of Iterator<Float> in the signature
 *     operator fun iterator(): FloatIterator = object : FloatIterator() {
 *         private var idx = 0
 *
 *         override fun nextFloat(): Float {
 *             if (!hasNext()) throw NoSuchElementException()
 *             return data[idx++]
 *         }
 *
 *         override fun hasNext(): Boolean = idx < data.size
 *     }
 * }
 *
 * for (element in FloatContainer(floatArrayOf(1f, 2f, 3f))) {
 *     ... handle element ...
 * }
 * ```
 * No boxing conversion is performed during the for-loop iteration.
 * Note that the iterator itself will still be allocated.
 */
public abstract class FloatIterator : Iterator<Float> {
    final override fun next(): Float = nextFloat()


    /**
     * Returns the next element in the iteration without boxing conversion.
     * @throws NoSuchElementException if the iteration has no next element.
     */
    public abstract fun nextFloat(): Float
}

/**
 * An iterator over a sequence of values of type `Double`.
 *
 * This is a substitute for `Iterator<Double>` that provides a specialized version of `next(): T` method: `nextDouble(): Double`
 * and has a special handling by the compiler to avoid platform-specific boxing conversions as a performance optimization.
 *
 * In the following example:
 *
 * ```kotlin
 * class DoubleContainer(private val data: DoubleArray) {
 *
 *     // DoubleIterator instead of Iterator<Double> in the signature
 *     operator fun iterator(): DoubleIterator = object : DoubleIterator() {
 *         private var idx = 0
 *
 *         override fun nextDouble(): Double {
 *             if (!hasNext()) throw NoSuchElementException()
 *             return data[idx++]
 *         }
 *
 *         override fun hasNext(): Boolean = idx < data.size
 *     }
 * }
 *
 * for (element in DoubleContainer(doubleArrayOf(1.0, 2.0, 3.0))) {
 *     ... handle element ...
 * }
 * ```
 * No boxing conversion is performed during the for-loop iteration.
 * Note that the iterator itself will still be allocated.
 */
public abstract class DoubleIterator : Iterator<Double> {
    final override fun next(): Double = nextDouble()


    /**
     * Returns the next element in the iteration without boxing conversion.
     * @throws NoSuchElementException if the iteration has no next element.
     */
    public abstract fun nextDouble(): Double
}

/**
 * An iterator over a sequence of values of type `Boolean`.
 *
 * This is a substitute for `Iterator<Boolean>` that provides a specialized version of `next(): T` method: `nextBoolean(): Boolean`
 * and has a special handling by the compiler to avoid platform-specific boxing conversions as a performance optimization.
 *
 * In the following example:
 *
 * ```kotlin
 * class BooleanContainer(private val data: BooleanArray) {
 *
 *     // BooleanIterator instead of Iterator<Boolean> in the signature
 *     operator fun iterator(): BooleanIterator = object : BooleanIterator() {
 *         private var idx = 0
 *
 *         override fun nextBoolean(): Boolean {
 *             if (!hasNext()) throw NoSuchElementException()
 *             return data[idx++]
 *         }
 *
 *         override fun hasNext(): Boolean = idx < data.size
 *     }
 * }
 *
 * for (element in BooleanContainer(booleanArrayOf(true, false, true))) {
 *     ... handle element ...
 * }
 * ```
 * No boxing conversion is performed during the for-loop iteration.
 * Note that the iterator itself will still be allocated.
 */
public abstract class BooleanIterator : Iterator<Boolean> {
    final override fun next(): Boolean = nextBoolean()


    /**
     * Returns the next element in the iteration without boxing conversion.
     * @throws NoSuchElementException if the iteration has no next element.
     */
    public abstract fun nextBoolean(): Boolean
}
