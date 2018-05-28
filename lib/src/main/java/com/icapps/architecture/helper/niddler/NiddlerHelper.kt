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

package com.icapps.architecture.helper.niddler

import android.app.Application
import com.icapps.niddler.core.AndroidNiddler
import com.icapps.niddler.interceptor.okhttp.NiddlerOkHttpInterceptor
import okhttp3.OkHttpClient

/**
 * @author Nicola Verbeeck
 * @version 1
 */
object NiddlerHelper {

    private const val DEFAULT_CACHE_SIZE = 10485760L

    fun configure(application: Application, okHttpBuilder: OkHttpClient.Builder) {
        val niddler = AndroidNiddler.Builder()
            .setNiddlerInformation(AndroidNiddler.fromApplication(application))
            .setPort(0)
            .setCacheSize(DEFAULT_CACHE_SIZE)
            .build()
        okHttpBuilder.addInterceptor(NiddlerOkHttpInterceptor(niddler))
    }

}