package kotlin.collections

/**
 * Returns `true` if [element] is found in the array.
 */
public operator fun <@kotlin.internal.OnlyInputTypes T> Array<out T>.contains(element: T): Boolean = error("Stub")

/**
 * Returns `true` if [element] is found in the array.
 */
public operator fun ByteArray.contains(element: Byte): Boolean = error("Stub")

/**
 * Returns `true` if [element] is found in the array.
 */
public operator fun ShortArray.contains(element: Short): Boolean = error("Stub")

/**
 * Returns `true` if [element] is found in the array.
 */
public operator fun IntArray.contains(element: Int): Boolean = error("Stub")

/**
 * Returns `true` if [element] is found in the array.
 */
public operator fun LongArray.contains(element: Long): Boolean = error("Stub")

/**
 * Returns `true` if [element] is found in the array.
 */
@Deprecated("The function has unclear behavior when searching for NaN or zero values and will be removed soon. Use 'any { it == element }' instead to continue using this behavior, or '.asList().contains(element: T)' to get the same search behavior as in a list.", ReplaceWith("any { it == element }"))
@DeprecatedSinceKotlin(warningSince = "1.4", errorSince = "1.6", hiddenSince = "1.7")
public operator fun FloatArray.contains(element: Float): Boolean = error("Stub")

/**
 * Returns `true` if [element] is found in the array.
 */
@Deprecated("The function has unclear behavior when searching for NaN or zero values and will be removed soon. Use 'any { it == element }' instead to continue using this behavior, or '.asList().contains(element: T)' to get the same search behavior as in a list.", ReplaceWith("any { it == element }"))
@DeprecatedSinceKotlin(warningSince = "1.4", errorSince = "1.6", hiddenSince = "1.7")
public operator fun DoubleArray.contains(element: Double): Boolean = error("Stub")

/**
 * Returns `true` if [element] is found in the array.
 */
public operator fun BooleanArray.contains(element: Boolean): Boolean = error("Stub")

/**
 * Returns `true` if [element] is found in the array.
 */
public operator fun CharArray.contains(element: Char): Boolean = error("Stub")
