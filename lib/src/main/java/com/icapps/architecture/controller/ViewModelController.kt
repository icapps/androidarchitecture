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

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import com.icapps.architecture.arch.BaseViewModel
import java.util.IdentityHashMap
import javax.inject.Inject
import kotlin.reflect.KClass

/**
 * @author Nicola Verbeeck
 * @version 1
 */
class ViewModelProvider @Inject constructor(val factory: ViewModelProvider.Factory) {

    private val viewModels = IdentityHashMap<KClass<*>, BaseViewModel>()

    inline fun <reified T : BaseViewModel> getOrCreateViewModel(activity: FragmentActivity, savedInstanceState: Bundle? = null): T {
        return registerViewModel(ViewModelProviders.of(activity, factory)[T::class.java], T::class, savedInstanceState)
    }

    inline fun <reified T : BaseViewModel> getOrCreateViewModel(fragment: Fragment, savedInstanceState: Bundle? = null): T {
        return registerViewModel(ViewModelProviders.of(fragment, factory)[T::class.java], T::class, savedInstanceState)
    }

    fun <T : BaseViewModel> registerViewModel(viewModel: T, tClass: KClass<T>, savedInstanceState: Bundle?): T {
        savedInstanceState?.let {
            viewModel.restoreInstanceState(it)
        }

        viewModels[tClass] = viewModel
        return viewModel
    }

    fun onSaveInstanceState(outState: Bundle) {
        viewModels.forEach { it.value.saveInstanceState(outState) }
    }

    fun onDestroy() {
        viewModels.clear()
    }

}


