package com.icapps.architecture.utils.ext

import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.databinding.ObservableList
import androidx.lifecycle.Lifecycle

/**
 * Provides a [callback] that gets called whenever the ObservableField's value changes.
 * The [callback] gets registered immediately, and gets deregistered when [lifecycle] hits ON_STOP
 * Upon registration, the [callback] gets called with the ObservableField's current value
 */
fun <T> ObservableField<T>.observe(lifecycle: Lifecycle, callback: (T?) -> Unit) {
    val field = this
    val propertyChangedCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            callback(field.get())
        }
    }
    addOnPropertyChangedCallback(propertyChangedCallback)
    lifecycle.addOnStopObserver {
        field.removeOnPropertyChangedCallback(propertyChangedCallback)
    }
    callback(field.get())
}

/**
 * Provides a [callback] that gets called whenever the ObservableInt's value changes.
 * The [callback] gets registered immediately, and gets deregistered when [lifecycle] hits ON_STOP
 * Upon registration, the [callback] gets called with the ObservableInt's current value
 */
fun ObservableInt.observe(lifecycle: Lifecycle, callback: (Int) -> Unit) {
    val field = this
    val propertyChangedCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            callback(field.get())
        }
    }
    addOnPropertyChangedCallback(propertyChangedCallback)
    lifecycle.addOnStopObserver {
        field.removeOnPropertyChangedCallback(propertyChangedCallback)
    }
    callback(field.get())
}

/**
 * Provides a [callback] that gets called whenever the ObservableBoolean's value changes.
 * The [callback] gets registered immediately, and gets deregistered when [lifecycle] hits ON_STOP
 * Upon registration, the [callback] gets called with the ObservableBoolean's current value
 */
fun ObservableBoolean.observe(lifecycle: Lifecycle, callback: (Boolean) -> Unit) {
    val field = this
    val propertyChangedCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            callback(field.get())
        }
    }
    addOnPropertyChangedCallback(propertyChangedCallback)
    lifecycle.addOnStopObserver {
        field.removeOnPropertyChangedCallback(propertyChangedCallback)
    }
    callback(field.get())
}

/**
 * Provides a [callback] that gets called whenever the ObservableInt's value changes.
 * The [callback] gets registered immediately, and gets deregistered when [lifecycle] hits ON_STOP
 * Upon registration, the [callback] gets called with the ObservableField's current value
 */
fun <T> ObservableList<T>.observe(lifecycle: Lifecycle, callback: (List<T>) -> Unit) {
    val field = this
    val listChangedCallback = object : ObservableList.OnListChangedCallback<ObservableList<T>>() {
        override fun onChanged(sender: ObservableList<T>) {
            callback(field)
        }

        override fun onItemRangeRemoved(sender: ObservableList<T>, positionStart: Int, itemCount: Int) {
            callback(field)
        }

        override fun onItemRangeMoved(sender: ObservableList<T>, fromPosition: Int, toPosition: Int, itemCount: Int) {
            callback(field)
        }

        override fun onItemRangeInserted(sender: ObservableList<T>, positionStart: Int, itemCount: Int) {
            callback(field)
        }

        override fun onItemRangeChanged(sender: ObservableList<T>, positionStart: Int, itemCount: Int) {
            callback(field)
        }
    }
    addOnListChangedCallback(listChangedCallback)
    lifecycle.addOnStopObserver {
        field.removeOnListChangedCallback(listChangedCallback)
    }
    callback(field)
}