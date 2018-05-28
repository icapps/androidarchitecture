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


