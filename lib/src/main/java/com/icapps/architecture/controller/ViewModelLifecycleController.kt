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

package com.icapps.architecture.controller

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.icapps.architecture.arch.BaseViewModel
import java.util.IdentityHashMap
import javax.inject.Inject
import kotlin.reflect.KClass

/**
 * Helper class which creates or retrieves [BaseViewModel]s based on the provided class
 *
 * @author Nicola Verbeeck
 * @version 1
 */
class ViewModelLifecycleController @Inject constructor(val factory: ViewModelProvider.Factory) {

    private val viewModels = IdentityHashMap<KClass<*>, BaseViewModel>()

    /**
     * Gets or creates the [BaseViewModel] denoted by the type parameter and optionally restores the state from the provided bundle
     *
     * @param activity The controlling activity
     * @param savedInstanceState Optional saved instance state to restore the [BaseViewModel]
     * @return Instance of the [BaseViewModel] tied to the activity
     */
    inline fun <reified T : BaseViewModel> getOrCreateViewModel(activity: FragmentActivity, savedInstanceState: Bundle? = null): T {
        return registerViewModel(ViewModelProvider(activity, factory)[T::class.java], T::class, savedInstanceState)
    }

    /**
     * Gets or creates the [BaseViewModel] denoted by the type parameter and optionally restores the state from the provided bundle
     *
     * @param fragment The controlling fragment
     * @param savedInstanceState Optional saved instance state to restore the [BaseViewModel]
     * @return Instance of the [BaseViewModel] tied to the fragment
     */
    inline fun <reified T : BaseViewModel> getOrCreateViewModel(fragment: Fragment, savedInstanceState: Bundle? = null): T {
        return registerViewModel(ViewModelProvider(fragment, factory)[T::class.java], T::class, savedInstanceState)
    }

    /**
     * Registers a ViewModel with this controller and dispatches saved state accordingly
     */
    fun <T : BaseViewModel> registerViewModel(viewModel: T, tClass: KClass<T>, savedInstanceState: Bundle?): T {
        savedInstanceState?.let {
            viewModel.restoreInstanceState(it)
        }

        viewModels[tClass] = viewModel
        return viewModel
    }

    /**
     * Call this when the view models of the controller should save their state
     *
     * @param outState The bundle to save the models to
     */
    fun onSaveInstanceState(outState: Bundle) {
        viewModels.forEach { it.value.saveInstanceState(outState) }
    }

    /**
     * Call this when the containing controller is being destroyed
     */
    fun onDestroy() {
        viewModels.clear()
    }

}