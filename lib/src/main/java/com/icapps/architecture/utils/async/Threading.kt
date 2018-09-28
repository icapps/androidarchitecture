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

package com.icapps.architecture.utils.async

import android.os.Looper
import com.icapps.architecture.arch.ConcreteMutableObservableFuture
import com.icapps.architecture.arch.ObservableFuture
import java.util.concurrent.Executor

/**
 * Asserts that the current thread is not the android main thread. If so, throw an [IllegalStateException]
 */
@Suppress("NOTHING_TO_INLINE")
inline fun assertNotMain() {
    val myLooper = Looper.myLooper()
    @Suppress("SuspiciousEqualsCombination")
    if (myLooper != null && myLooper === Looper.getMainLooper())
        throw IllegalStateException("Should not be called from the main thread")
}

/**
 * Executes the given function on a new background thread
 *
 * @param lambda The function to execute, the resulting value of the function will be reported to the returned [ObservableFuture]
 * @return An [ObservableFuture] that can be used to observe the result of the function
 */
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

/**
 * Executes the given function on a background thread provided by the given executor.
 *
 * # Warning
 * See [com.icapps.architecture.utils.async.ScalingThreadPoolExecutor] for a correct example of
 * an unbounded [java.util.concurrent.ThreadPoolExecutor.ThreadPoolExecutor]
 *
 * @param executor The executor to execute the function on. See [com.icapps.architecture.utils.async.ScalingThreadPoolExecutor]
 * @param lambda The function to execute, the resulting value of the function will be reported to the returned [ObservableFuture]
 * @return An [ObservableFuture] that can be used to observe the result of the function
 */
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

/**
 * Executes the given function on a background thread. If the calling thread is already a background thread, the function will be executed immediately
 *
 * @param lambda The function to execute, the resulting value of the function will be reported to the returned [ObservableFuture]
 * @return An [ObservableFuture] that can be used to observe the result of the function
 */
inline fun <T> offMain(crossinline lambda: () -> T): ObservableFuture<T> {
    if (Looper.myLooper() !== Looper.getMainLooper()){
        return try {
            ObservableFuture.withData(lambda())
        }catch(e: Throwable){
            ObservableFuture.withError(e)
        }
    }
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

/**
 * Executes the given function off the main thread. If the calling thread is already a background thread, the function will be executed immediately.
 * Executes the given function on a background thread provided by the given executor.
 *
 *
 * # Warning
 * See [com.icapps.architecture.utils.async.ScalingThreadPoolExecutor] for a correct example of
 * an unbounded [java.util.concurrent.ThreadPoolExecutor.ThreadPoolExecutor]
 *
 * @param executor The executor to execute the function on. See [com.icapps.architecture.utils.async.ScalingThreadPoolExecutor]
 * @param lambda The function to execute, the resulting value of the function will be reported to the returned [ObservableFuture]
 * @return An [ObservableFuture] that can be used to observe the result of the function
 */
inline fun <T> offMain(executor: Executor, crossinline lambda: () -> T): ObservableFuture<T> {
    if (Looper.myLooper() !== Looper.getMainLooper()){
        return try {
            ObservableFuture.withData(lambda())
        }catch(e: Throwable){
            ObservableFuture.withError(e)
        }
    }
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