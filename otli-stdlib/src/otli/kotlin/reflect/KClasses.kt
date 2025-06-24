/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("UNCHECKED_CAST")

package kotlin.reflect

import kotlin.*


/**
 * Casts the given [value] to the class represented by this [KClass] object.
 * Throws an exception if the value is `null` or if it is not an instance of this class.
 *
 * This is an experimental function that behaves as a similar function from kotlin.reflect.full on JVM.
 *
 * @see [KClass.isInstance]
 * @see [KClass.safeCast]
 */
@SinceKotlin("1.4")
public fun <T : Any> KClass<T>.cast(value: Any?): T {
    if (!isInstance(value)) throw Throwable("Value cannot be cast to $qualifiedOrSimpleName")
    return value as T
}

// TODO: replace with qualifiedName when it is fully supported in K/JS
internal val KClass<*>.qualifiedOrSimpleName: String?
    get() = error("Stub")

/**
 * Casts the given [value] to the class represented by this [KClass] object.
 * Returns `null` if the value is `null` or if it is not an instance of this class.
 *
 * This is an experimental function that behaves as a similar function from kotlin.reflect.full on JVM.
 *
 * @see [KClass.isInstance]
 * @see [KClass.cast]
 */
@SinceKotlin("1.4")
public fun <T : Any> KClass<T>.safeCast(value: Any?): T? {
    return if (isInstance(value)) value as T else null
}
