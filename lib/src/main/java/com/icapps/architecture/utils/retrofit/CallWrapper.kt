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
 * @author Nicola Verbeeck
 * @version 1
 */
inline fun <reified T> Call<T>.wrapToFuture(noinline headerInspector: ((Headers, T) -> T)? = null): ObservableFuture<T> {
    return wrapToFuture(T::class.java, nullableType = (null is T), headerInspector = headerInspector)
}

@Suppress("UNCHECKED_CAST")
fun <T> Call<T>.wrapToFuture(type: Type, nullableType: Boolean, headerInspector: ((Headers, T) -> T)? = null): ObservableFuture<T> {
    return RetrofitObservableFuture(this, type, nullableType, headerInspector)
}

/**
 * @author Nicola Verbeeck
 * @version 1
 */
class ServiceException : IOException {

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