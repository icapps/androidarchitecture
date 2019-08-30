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
 *
 * @return The underlying property change callback, can be used to manually unsubscribe
 */
fun <T> ObservableField<T>.observe(lifecycle: Lifecycle, callback: (T?) -> Unit) : Observable.OnPropertyChangedCallback {
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
    return propertyChangedCallback
}

/**
 * Provides a [callback] that gets called whenever the ObservableInt's value changes.
 * The [callback] gets registered immediately, and gets deregistered when [lifecycle] hits ON_STOP
 * Upon registration, the [callback] gets called with the ObservableInt's current value
 *
 * @return The underlying property change callback, can be used to manually unsubscribe
 */
fun ObservableInt.observe(lifecycle: Lifecycle, callback: (Int) -> Unit) : Observable.OnPropertyChangedCallback {
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
    return propertyChangedCallback
}

/**
 * Provides a [callback] that gets called whenever the ObservableBoolean's value changes.
 * The [callback] gets registered immediately, and gets deregistered when [lifecycle] hits ON_STOP
 * Upon registration, the [callback] gets called with the ObservableBoolean's current value
 *
 * @return The underlying property change callback, can be used to manually unsubscribe
 */
fun ObservableBoolean.observe(lifecycle: Lifecycle, callback: (Boolean) -> Unit) : Observable.OnPropertyChangedCallback {
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
    return propertyChangedCallback
}

/**
 * Provides a [callback] that gets called whenever the ObservableInt's value changes.
 * The [callback] gets registered immediately, and gets deregistered when [lifecycle] hits ON_STOP
 * Upon registration, the [callback] gets called with the ObservableField's current value
 *
 * @return The underlying list change callback, can be used to manually unsubscribe
 */
fun <T> ObservableList<T>.observe(lifecycle: Lifecycle, callback: (List<T>) -> Unit) : ObservableList.OnListChangedCallback<ObservableList<T>> {
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
    return listChangedCallback
}