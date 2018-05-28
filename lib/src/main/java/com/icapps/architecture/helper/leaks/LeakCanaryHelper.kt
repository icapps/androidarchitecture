package com.icapps.architecture.helper.leaks

import android.app.Application
import com.squareup.leakcanary.LeakCanary

/**
 * @author Nicola Verbeeck
 * @version 1
 */
object LeakCanaryHelper {

    fun configure(application: Application) {
        if (LeakCanary.isInAnalyzerProcess(application))
            return

        LeakCanary.install(application)
    }

}