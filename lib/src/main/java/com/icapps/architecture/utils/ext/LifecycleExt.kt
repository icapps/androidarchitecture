package com.icapps.architecture.utils.ext

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent

/**
 * Adds an observer to the specified Lifecycle, which will trigger the provided [callback] when the lifecycle hits ON_STOP.
 * The observer is registered immediately and gets cleaned up after the lifecycle hits ON_STOP, so it will only be called once.
 */
fun Lifecycle.addOnStopObserver(callback: () -> Unit) {
    addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onStop() {
            callback()
            this@addOnStopObserver.removeObserver(this)
        }
    })
}