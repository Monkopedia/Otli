/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.text

internal fun numberFormatError(number: Any): Nothing = error("Number format error: $number")

/**
 * Returns a string representation of this [Byte] value in the specified [radix].
 *
 * @throws IllegalArgumentException when [radix] is not a valid radix for number to string conversion.
 */
@SinceKotlin("1.5")
//@kotlin.internal.InlineOnly
public /*inline*/ fun UByte.toString(radix: Int): String = error("Stub")

/**
 * Returns a string representation of this [Short] value in the specified [radix].
 *
 * @throws IllegalArgumentException when [radix] is not a valid radix for number to string conversion.
 */
@SinceKotlin("1.5")
//@kotlin.internal.InlineOnly
public /*inline*/ fun UShort.toString(radix: Int): String = error("Stub")


/**
 * Returns a string representation of this [Int] value in the specified [radix].
 *
 * @throws IllegalArgumentException when [radix] is not a valid radix for number to string conversion.
 */
@SinceKotlin("1.5")
//@kotlin.internal.InlineOnly
public /*inline*/ fun UInt.toString(radix: Int): String = error("Stub")

/**
 * Returns a string representation of this [Long] value in the specified [radix].
 *
 * @throws IllegalArgumentException when [radix] is not a valid radix for number to string conversion.
 */
@SinceKotlin("1.5")
public fun ULong.toString(radix: Int): String = error("Stub")


/**
 * Parses the string as a signed [UByte] number and returns the result.
 * @throws NumberFormatException if the string is not a valid representation of a number.
 */
@SinceKotlin("1.5")
public fun String.toUByte(): UByte = toUByteOrNull() ?: numberFormatError(this)

/**
 * Parses the string as a signed [UByte] number and returns the result.
 * @throws NumberFormatException if the string is not a valid representation of a number.
 * @throws IllegalArgumentException when [radix] is not a valid radix for string to number conversion.
 */
@SinceKotlin("1.5")
public fun String.toUByte(radix: Int): UByte = error("Stub")


/**
 * Parses the string as a [UShort] number and returns the result.
 * @throws NumberFormatException if the string is not a valid representation of a number.
 */
@SinceKotlin("1.5")
public fun String.toUShort(): UShort = toUShortOrNull() ?: numberFormatError(this)

/**
 * Parses the string as a [UShort] number and returns the result.
 * @throws NumberFormatException if the string is not a valid representation of a number.
 * @throws IllegalArgumentException when [radix] is not a valid radix for string to number conversion.
 */
@SinceKotlin("1.5")
public fun String.toUShort(radix: Int): UShort = error("Stub")

/**
 * Parses the string as an [UInt] number and returns the result.
 * @throws NumberFormatException if the string is not a valid representation of a number.
 */
@SinceKotlin("1.5")
public fun String.toUInt(): UInt = toUIntOrNull() ?: numberFormatError(this)

/**
 * Parses the string as an [UInt] number and returns the result.
 * @throws NumberFormatException if the string is not a valid representation of a number.
 * @throws IllegalArgumentException when [radix] is not a valid radix for string to number conversion.
 */
@SinceKotlin("1.5")
public fun String.toUInt(radix: Int): UInt = error("Stub")

/**
 * Parses the string as a [ULong] number and returns the result.
 * @throws NumberFormatException if the string is not a valid representation of a number.
 */
@SinceKotlin("1.5")
public fun String.toULong(): ULong = toULongOrNull() ?: numberFormatError(this)

/**
 * Parses the string as a [ULong] number and returns the result.
 * @throws NumberFormatException if the string is not a valid representation of a number.
 * @throws IllegalArgumentException when [radix] is not a valid radix for string to number conversion.
 */
@SinceKotlin("1.5")
public fun String.toULong(radix: Int): ULong = error("Stub")





/**
 * Parses the string as an [UByte] number and returns the result
 * or `null` if the string is not a valid representation of a number.
 */
@SinceKotlin("1.5")
public fun String.toUByteOrNull(): UByte? = error("Stub")

/**
 * Parses the string as an [UByte] number and returns the result
 * or `null` if the string is not a valid representation of a number.
 *
 * @throws IllegalArgumentException when [radix] is not a valid radix for string to number conversion.
 */
@SinceKotlin("1.5")
public fun String.toUByteOrNull(radix: Int): UByte? = error("Stub")

/**
 * Parses the string as an [UShort] number and returns the result
 * or `null` if the string is not a valid representation of a number.
 */
@SinceKotlin("1.5")
public fun String.toUShortOrNull(): UShort? = toUShortOrNull(radix = 10)

/**
 * Parses the string as an [UShort] number and returns the result
 * or `null` if the string is not a valid representation of a number.
 *
 * @throws IllegalArgumentException when [radix] is not a valid radix for string to number conversion.
 */
@SinceKotlin("1.5")
public fun String.toUShortOrNull(radix: Int): UShort? = error("Stub")
/**
 * Parses the string as an [UInt] number and returns the result
 * or `null` if the string is not a valid representation of a number.
 */
@SinceKotlin("1.5")
public fun String.toUIntOrNull(): UInt? = toUIntOrNull(radix = 10)

/**
 * Parses the string as an [UInt] number and returns the result
 * or `null` if the string is not a valid representation of a number.
 *
 * @throws IllegalArgumentException when [radix] is not a valid radix for string to number conversion.
 */
@SinceKotlin("1.5")
public fun String.toUIntOrNull(radix: Int): UInt? = error("Stub")

/**
 * Parses the string as an [ULong] number and returns the result
 * or `null` if the string is not a valid representation of a number.
 */
@SinceKotlin("1.5")
public fun String.toULongOrNull(): ULong? = toULongOrNull(radix = 10)

/**
 * Parses the string as an [ULong] number and returns the result
 * or `null` if the string is not a valid representation of a number.
 *
 * @throws IllegalArgumentException when [radix] is not a valid radix for string to number conversion.
 */
@SinceKotlin("1.5")
public fun String.toULongOrNull(radix: Int): ULong? = error("Stub")
