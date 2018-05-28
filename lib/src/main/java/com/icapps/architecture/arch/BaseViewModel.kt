package com.icapps.architecture.arch

import android.arch.lifecycle.ViewModel
import android.os.Bundle
import android.support.annotation.CallSuper

/**
 * @author Nicola Verbeeck
 * @version 1
 */
abstract class BaseViewModel : ViewModel() {

    protected var isCleanInstance: Boolean = true
        private set

    @CallSuper
    open fun saveInstanceState(outState: Bundle) {
        isCleanInstance = false
    }

    open fun restoreInstanceState(savedInstanceState: Bundle) {
        // Implement in subclass
    }

}