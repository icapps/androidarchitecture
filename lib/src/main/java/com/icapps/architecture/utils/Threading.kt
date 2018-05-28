/*
 * Copyright 2018 icapps
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.icapps.architecture.utils

import android.os.Looper
import com.icapps.architecture.arch.ConcreteMutableObservableFuture
import com.icapps.architecture.arch.ObservableFuture
import java.util.concurrent.Executor

/**
 * @author Nicola Verbeeck
 * @version 1
 */
@Suppress("NOTHING_TO_INLINE")
inline fun assertNotMain() {
    val myLooper = Looper.myLooper()
    @Suppress("SuspiciousEqualsCombination")
    if (myLooper != null && myLooper === Looper.getMainLooper())
        throw IllegalStateException("Should not be called from the main thread")
}

inline fun <T> onBackground(crossinline lambda: () -> T): ObservableFuture<T> {
    val ret = ConcreteMutableObservableFuture<T>()
    Thread {
        try {
            ret.onResult(lambda())
        } catch (e: Throwable) {
            ret.onResult(e)
        }
    }.start()
    return ret
}

inline fun <T> onBackground(executor: Executor, crossinline lambda: () -> T): ObservableFuture<T> {
    val ret = ConcreteMutableObservableFuture<T>()
    executor.execute {
        try {
            ret.onResult(lambda())
        } catch (e: Throwable) {
            ret.onResult(e)
        }
    }
    return ret
}