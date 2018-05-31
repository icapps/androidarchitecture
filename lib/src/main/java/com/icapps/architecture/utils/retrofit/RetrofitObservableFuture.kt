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

package com.icapps.architecture.utils.retrofit

import android.arch.lifecycle.Lifecycle
import com.icapps.architecture.arch.ConcreteMutableObservableFuture
import com.icapps.architecture.arch.OnCallerTag
import okhttp3.Headers
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type

/**
 * Specialized [com.icapps.architecture.arch.MutableObservableFuture] which wraps a retrofit call.
 * Unlike other [com.icapps.architecture.arch.ObservableFuture]s the call is not executed before [observe] or [execute] is called
 *
 * @author Nicola Verbeeck
 * @version 1
 * @param call The retrofit call to execute
 * @param type  The type of the data returned from the call (should match T)
 * @param nullableType  Boolean indicating if the type of T is actually nullable
 * @param headerInspector Optional function to be invoked when the call has executed with success, gets the headers of the response
 * and the response data and should return the response data optionally changed. The result of this function, if set, is the data that is
 * returned in the [com.icapps.architecture.arch.ObservableFuture]
 */
class RetrofitObservableFuture<T>(private val call: Call<T>,
                                  type: Type,
                                  private val nullableType: Boolean,
                                  private val headerInspector: ((Headers, T) -> T)?) : ConcreteMutableObservableFuture<T>() {

    /** Boolean indicating if the result is actually [Unit] and no body is expected */
    private val isEmptyBody = type == Unit::class.java
    /** Boolean indicating the expected result is a raw [ResponseBody]*/
    private val isResponseBody = type == ResponseBody::class.java

    /**
     * Cancels the future and also cancels the retrofit call
     */
    override fun cancel() {
        super.cancel()
        call.cancel()
    }

    /**
     * Starts the call on a background thread and delivers the results to the main thread
     */
    override fun observe(lifecycle: Lifecycle) {
        synchronized(lock) {
            if (cancelled)
                return

            super.observe(lifecycle)
            enqueue()
        }
    }

    /**
     * Starts the call on a background thread and delivers the results to the calling thread
     */
    override fun observe(onCaller: OnCallerTag) {
        synchronized(lock) {
            if (cancelled)
                return

            super.observe(onCaller)
            enqueue()
        }
    }

    /**
     * Executes the call on THIS thread. The timeout parameter is ignored since it cannot be set per-request
     *
     * @param timeout Ignored
     */
    override fun execute(timeout: Long): T {
        //Timeout is ignored here, timeout should be set on HTTP client and cannot be changed per-request
        return handleResponse(call.execute())
    }

    /**
     * Enqueues the request to be executed by retrofit
     */
    private fun enqueue() {
        call.enqueue(object : Callback<T> {
            override fun onFailure(theCall: Call<T>?, t: Throwable) {
                onResult(ServiceException(t, call.request()))
            }

            override fun onResponse(call: Call<T>?, response: Response<T>) {
                try {
                    onResult(handleResponse(response))
                } catch (e: Throwable) {
                    onResult(e)
                }
            }
        })
    }

    /**
     * Handle the raw retrofit response
     */
    @Suppress("UNCHECKED_CAST", "UNNECESSARY_NOT_NULL_ASSERTION")
    private fun handleResponse(response: Response<T>): T {
        if (response.isSuccessful) {
            if (isEmptyBody) {
                return Unit as T
            } else if (isResponseBody) {
                return if (headerInspector != null)
                    headerInspector!!(response.headers(), response.body() as T)
                else
                    response.body() as T
            } else {
                val body = response.body()
                if (body == null && !nullableType) {
                    val errorBody = response.errorBody()?.string()
                    throw ServiceException("Empty response where a body was expected", errorBody, response.raw(), call.request())
                } else {
                    return if (headerInspector != null)
                        headerInspector!!(response.headers(), body as T)
                    else
                        body as T
                }
            }
        } else {
            val errorBody = response.errorBody()?.string()
            throw ServiceException(response.message(), errorBody, response.raw(), call.request())
        }
    }

}