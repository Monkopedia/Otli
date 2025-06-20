/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.reflect

import kotlin.*

/**
 * Represents a function with introspection capabilities.
 */
public interface KFunction<out R> : KCallable<R>, Function<R>
