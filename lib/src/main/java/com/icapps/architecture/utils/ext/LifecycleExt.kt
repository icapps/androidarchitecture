package com.icapps.architecture.utils.ext

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

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

/**
 * Adds an observer to the specified Lifecycle, which will trigger the provided [callback] when the lifecycle hits ON_PAUSE.
 * The observer is registered immediately and gets cleaned up after the lifecycle hits ON_PAUSE, so it will only be called once.
 */
fun Lifecycle.addOnPauseObserver(callback: () -> Unit) {
    addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        fun onStop() {
            callback()
            this@addOnPauseObserver.removeObserver(this)
        }
    })
}

/**
 * Adds an observer to the specified Lifecycle, which will trigger the provided [callback] when the lifecycle hits ON_START and ON_STOP.
 * The observer is registered immediately and gets cleaned up after the lifecycle hits ON_STOP OR ON_DESTROY,
 * this means the callbacks can be called only once
 */
fun Lifecycle.addStartAndStopObservers(onStartCallback: () -> Unit, onStopCallback: () -> Unit) {
    addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onStop() {
            onStopCallback()
            this@addStartAndStopObservers.removeObserver(this)
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onStart() {
            onStartCallback()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            this@addStartAndStopObservers.removeObserver(this)
        }
    })
}