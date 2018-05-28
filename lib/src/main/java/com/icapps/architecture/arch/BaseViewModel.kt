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