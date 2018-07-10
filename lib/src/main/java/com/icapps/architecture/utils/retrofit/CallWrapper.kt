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

import com.icapps.architecture.arch.ObservableFuture
import okhttp3.Headers
import okhttp3.Request
import retrofit2.Call
import java.io.IOException
import java.lang.reflect.Type

/**
 * Helper that wraps a call to an [ObservableFuture].
 * Reified inline to capture as much information about the type as possible (such as nullability, ...)
 *
 * @receiver Call to wrap
 * @param headerInspector Optional transformation function which receives the result and the headers. See [RetrofitObservableFuture]
 * @param errorTransformer Optional transformation function which receives the exception and can transform it before it is returned to the future
 * @return A future which observes the call
 */
inline fun <reified T> Call<T>.wrapToFuture(noinline headerInspector: ((Headers, T) -> T)? = null,
                                            noinline errorTransformer: ((ServiceException) -> Throwable)? = null): ObservableFuture<T> {
    return wrapToFuture(T::class.java, nullableType = (null is T), headerInspector = headerInspector, errorTransformer = errorTransformer)
}

/**
 * Helper that wraps a call to an [ObservableFuture].
 *
 * @receiver Call to wrap
 * @param type The type of the data we are wrapping. Should be equivalent to T
 * @param nullableType Boolean indicating if T is nullable
 * @param headerInspector Optional transformation function which receives the result and the headers. See [RetrofitObservableFuture]
 * @param errorTransformer Optional transformation function which receives the exception and can transform it before it is returned to the future
 * @return A future which observes the call
 */
@Suppress("UNCHECKED_CAST")
fun <T> Call<T>.wrapToFuture(type: Type, nullableType: Boolean, headerInspector: ((Headers, T) -> T)? = null,
                             errorTransformer: ((ServiceException) -> Throwable)? = null): ObservableFuture<T> {
    return RetrofitObservableFuture(this, type, nullableType, headerInspector, transform = null, errorTransformer = errorTransformer)
}

/**
 * Helper that wraps a call to an [ObservableFuture].
 *
 * @receiver Call to wrap
 * @param type The type of the data we are wrapping. Should be equivalent to T
 * @param nullableType Boolean indicating if T is nullable
 * @param transform Transformation function which transforms the result
 * @param headerInspector Optional transformation function which receives the result and the headers. See [RetrofitObservableFuture]
 * @param errorTransformer Optional transformation function which receives the exception and can transform it before it is returned to the future
 * @return A future which observes the call
 */
@Suppress("UNCHECKED_CAST")
fun <T, O> Call<T>.wrapToFuture(type: Type, nullableType: Boolean, transform: (T) -> O, headerInspector: ((Headers, T) -> T)? = null,
                                errorTransformer: ((ServiceException) -> Throwable)? = null): ObservableFuture<O> {
    return RetrofitObservableFuture(this, type, nullableType, headerInspector, transform, errorTransformer = errorTransformer)
}

/**
 * Wrapper for an okhttp network error (in application-land)
 *
 * @author Nicola Verbeeck
 * @version 1
 */
open class ServiceException : IOException {

    val response: okhttp3.Response?
    val request: Request?
    val errorBody: String?

    constructor(message: String, errorBody: String?, response: okhttp3.Response?, request: Request?) : super(message) {
        this.response = response
        this.request = request
        this.errorBody = errorBody
    }

    constructor(other: Throwable, request: Request?) : super(other) {
        this.response = null
        this.request = request
        this.errorBody = null
    }

    /**
     * Constructs a message containing details about the failed call such as url, response code, cache headers
     */
    override val message: String?
        get() = buildString {
            append(super.message)
            response?.let {
                append(" - ").append(it.code()).append(" (").append(it.message())
                append(") - url: ").append(it.request()?.url())
                append(" - cache: ").append(it.cacheControl())
            }
        }

}