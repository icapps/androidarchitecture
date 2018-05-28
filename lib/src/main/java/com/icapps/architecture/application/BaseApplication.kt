package com.icapps.architecture.application

import android.app.Application
import com.icapps.architecture.helper.leaks.LeakCanaryHelper
import com.icapps.architecture.helper.niddler.NiddlerHelper
import okhttp3.OkHttpClient

/**
 * @author Nicola Verbeeck
 * @version 1
 */
open class BaseApplication : Application() {

    protected fun configureLeakCanary() {
        LeakCanaryHelper.configure(this)
    }

    protected fun configureNiddler(okHttpBuilder: OkHttpClient.Builder){
        NiddlerHelper.configure(this, okHttpBuilder)
    }

}