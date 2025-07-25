/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// Auto-generated file. DO NOT EDIT!
// Generated by: org.jetbrains.kotlin.generators.builtins.unsigned.UnsignedArrayGenerator

package kotlin


@SinceKotlin("1.3")
public value class UByteArray
@PublishedApi
internal constructor(@PublishedApi internal val storage: ByteArray) : Collection<UByte> {

    /** Creates a new array of the specified [size], with all elements initialized to zero. */
    public constructor(size: Int) : this(ByteArray(size))

    /**
     * Returns the array element at the given [index]. This method can be called using the index operator.
     *
     * If the [index] is out of bounds of this array, throws an [IndexOutOfBoundsException] except in Kotlin/JS
     * where the behavior is unspecified.
     */
    public operator fun get(index: Int): UByte = storage[index].toUByte()

    /**
     * Sets the element at the given [index] to the given [value]. This method can be called using the index operator.
     *
     * If the [index] is out of bounds of this array, throws an [IndexOutOfBoundsException] except in Kotlin/JS
     * where the behavior is unspecified.
     */
    public operator fun set(index: Int, value: UByte) {
        storage[index] = value.toByte()
    }

    /** Returns the number of elements in the array. */
    public override val size: Int get() = storage.size

    /** Creates an iterator over the elements of the array. */
    public override operator fun iterator(): kotlin.collections.Iterator<UByte> = Iterator(storage)

    private class Iterator(private val array: ByteArray) : kotlin.collections.Iterator<UByte> {
        private var index = 0
        override fun hasNext() = index < array.size
        override fun next() = if (index < array.size) array[index++].toUByte() else throw Throwable(index.toString())
    }

    override fun contains(element: UByte): Boolean {
        return storage.contains(element.toByte())
    }

    override fun containsAll(elements: Collection<UByte>): Boolean {
        return (elements as Collection<*>).all { it is UByte && storage.contains(it.toByte()) }
    }

    override fun isEmpty(): Boolean = this.storage.size == 0
}

/**
 * Creates a new array of the specified [size], where each element is calculated by calling the specified
 * [init] function.
 *
 * The function [init] is called for each array element sequentially starting from the first one.
 * It should return the value for an array element given its index.
 */
@SinceKotlin("1.3")
@kotlin.internal.InlineOnly
public inline fun UByteArray(size: Int, init: (Int) -> UByte): UByteArray {
    return UByteArray(ByteArray(size) { index -> init(index).toByte() })
}

@SinceKotlin("1.3")
@kotlin.internal.InlineOnly
public inline fun ubyteArrayOf(vararg elements: UByte): UByteArray = elements
