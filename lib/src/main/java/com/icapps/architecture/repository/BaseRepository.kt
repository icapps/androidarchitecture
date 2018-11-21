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

package com.icapps.architecture.repository

import com.icapps.architecture.arch.ObservableFuture
import com.icapps.architecture.utils.exception.NetworkErrorTraceException
import com.icapps.architecture.utils.retrofit.RawResponse
import com.icapps.architecture.utils.retrofit.ServiceException
import com.icapps.architecture.utils.retrofit.wrapToFuture
import com.icapps.architecture.utils.retrofit.wrapToFutureRaw
import retrofit2.Call

/**
 * Base class with helper methods for transforming [Call]s into [ObservableFuture]s
 *
 * @author Nicola Verbeeck
 * @version 1
 */
abstract class BaseRepository {

    /**
     * Flag indicating if exceptions reported by retrofit should record the source from where
     * they were called instead of only containing the trace inside retrofit. Disabled by default,
     * has a performance impact
     */
    protected val keepErrorSource: Boolean

    constructor() : this(false)
    constructor(keepErrorSource: Boolean) {
        this.keepErrorSource = keepErrorSource
    }

    /**
     * Overloaded version of [makeCall] which will transform the given call into an observable form
     *
     * @param call The call to execute
     * @return A future that can be used to observe the result of the call
     */
    protected inline fun <reified T> makeCall(call: Call<T>): ObservableFuture<T> {
        return makeCall(T::class.java, call, nullableType = (null is T), transform = null)
    }

    /**
     * Overloaded version of [makeCall] which will transform the given call into an observable form and includes the raw http response
     *
     * @param call The call to execute
     * @return A future that can be used to observe the result of the call
     */
    protected inline fun <reified T> makeRawCall(call: Call<T>): ObservableFuture<RawResponse<T>> {
        return makeRawCall(T::class.java, call, nullableType = (null is T), transform = null)
    }

    /**
     * Overloaded version of [makeCall] which also takes a transformation function
     *
     * @param call The call to execute
     * @param transform Transformation function that is invoked when the call has completed with success
     * @return A future that can be used to observe the result of the call
     */
    protected inline fun <reified T, O> makeCall(call: Call<T>, noinline transform: ((T) -> O)): ObservableFuture<O> {
        return makeCall(T::class.java, call, nullableType = (null is T), transform = transform)
    }

    /**
     * Overloaded version of [makeCall] which also takes a transformation function and includes the raw http response
     *
     * @param call The call to execute
     * @param transform Transformation function that is invoked when the call has completed with success
     * @return A future that can be used to observe the result of the call
     */
    protected inline fun <reified T, O> makeRawCall(call: Call<T>, noinline transform: ((T) -> O)): ObservableFuture<RawResponse<O>> {
        return makeRawCall(T::class.java, call, nullableType = (null is T), transform = transform)
    }

    /**
     * Overloaded version of [makeCall] which also takes the nullable status of T
     *
     * @param call The call to execute
     * @param nullableType Boolean indicating if T is nullable
     * @param transform Optional transformation function that is invoked when the call has completed with success
     * @return A future that can be used to observe the result of the call
     */
    protected inline fun <reified T, O> makeCall(call: Call<T>, nullableType: Boolean = false, noinline transform: ((T) -> O)): ObservableFuture<O> {
        return makeCall(T::class.java, call, nullableType = nullableType, transform = transform)
    }

    /**
     * Overloaded version of [makeCall] which also takes the nullable status of T and returns the http response
     *
     * @param call The call to execute
     * @param nullableType Boolean indicating if T is nullable
     * @param transform Optional transformation function that is invoked when the call has completed with success
     * @return A future that can be used to observe the result of the call
     */
    protected inline fun <reified T, O> makeRawCall(call: Call<T>, nullableType: Boolean = false, noinline transform: ((T) -> O)): ObservableFuture<RawResponse<O>> {
        return makeRawCall(T::class.java, call, nullableType = nullableType, transform = transform)
    }

    /**
     * Executes the provided call, optionally transforms the result
     *
     * @param type The type of the result of the call, maps to <T>
     * @param call The call to execute
     * @param nullableType Boolean indicating if T is nullable
     * @param transform Optional transformation function that is invoked when the call has completed with success. If transform is null, 'O is T' is required
     * @return A future that can be used to observe the result of the call
     */
    @Suppress("UNCHECKED_CAST")
    protected open fun <T, O> makeCall(type: Class<T>,
                                       call: Call<T>,
                                       nullableType: Boolean,
                                       transform: ((T) -> O)?): ObservableFuture<O> {
        val errorTransformer: ((ServiceException) -> Throwable)? = if (keepErrorSource) {
            val source = NetworkErrorTraceException();

            { e: ServiceException ->
                source.init(e)
                source
            }
        } else {
            null
        }
        return if (transform == null)
            call.wrapToFuture(type, nullableType, errorTransformer = errorTransformer) as ObservableFuture<O>
        else
            call.wrapToFuture(type, nullableType, transform, errorTransformer = errorTransformer)
    }


    /**
     * Executes the provided call, optionally transforms the result and returns the http response
     *
     * @param type The type of the result of the call, maps to <T>
     * @param call The call to execute
     * @param nullableType Boolean indicating if T is nullable
     * @param transform Optional transformation function that is invoked when the call has completed with success. If transform is null, 'O is T' is required
     * @return A future that can be used to observe the result of the call
     */
    @Suppress("UNCHECKED_CAST")
    protected open fun <T, O> makeRawCall(type: Class<T>,
                                       call: Call<T>,
                                       nullableType: Boolean,
                                       transform: ((T) -> O)?): ObservableFuture<RawResponse<O>> {
        val errorTransformer: ((ServiceException) -> Throwable)? = if (keepErrorSource) {
            val source = NetworkErrorTraceException();

            { e: ServiceException ->
                source.init(e)
                source
            }
        } else {
            null
        }
        return if (transform == null)
            call.wrapToFutureRaw(type, nullableType, errorTransformer = errorTransformer) as ObservableFuture<RawResponse<O>>
        else
            call.wrapToFutureRaw(type, nullableType, transform, errorTransformer = errorTransformer)
    }

    /**
     * Cleanup any memory caches held by the repository
     */
    open fun clear() {
    }
}