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