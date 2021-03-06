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

package com.icapps.architecture.helper.leaks

import android.app.Application
import com.squareup.leakcanary.LeakCanary

/**
 * Helper to configure LeakCanary
 *
 * @author Nicola Verbeeck
 * @version 1
 */
object LeakCanaryHelper {

    /**
     * Initializes LeakCanary for the application. Skips initialization if this process is the Leak analyzer process
     *
     * @param application The application to use to initialize LeakCanary with
     */
    fun configure(application: Application) {
        if (LeakCanary.isInAnalyzerProcess(application))
            return

        LeakCanary.install(application)
    }

}