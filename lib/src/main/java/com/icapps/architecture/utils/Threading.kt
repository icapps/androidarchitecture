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