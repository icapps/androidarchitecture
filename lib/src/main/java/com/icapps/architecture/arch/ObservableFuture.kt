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

package com.icapps.architecture.arch

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.os.Handler
import android.os.Looper
import android.support.annotation.WorkerThread
import com.icapps.architecture.utils.async.assertNotMain
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * @author Nicola Verbeeck
 * @version 1
 */
/**
 * Tagging class used to indicate that the future should execute the callbacks on the thread that sets the result
 */
class OnCallerTag internal constructor()

/**
 * Tagging class used to indicate that the future should dispatch the results on the main thread. Whenever possible use the lifecycle version of observe which automatically
 * cancels the call when the lifecycle enters the stopped state
 */
class OnMainThreadTag internal constructor()

/** Instance of the [OnCallerTag] to be used with [ObservableFuture] */
val onCaller = OnCallerTag()

/** Instance of the [OnMainThreadTag] to be used with [ObservableFuture] */
val onMain = OnMainThreadTag()

/**
 * Future concept which provides convenient methods for listening for results and/or errors
 */
interface ObservableFuture<T> {

    companion object {
        /** Dispatcher used to dispatch callbacks on the android main thread */
        val mainDispatcher = Handler(Looper.getMainLooper())

        /**
         * Create a simple [ObservableFuture] that simply returns the provided data
         *
         * @param data The data to be returned as success for this future
         * @return A future that simply returns the provided data
         */
        fun <T> withData(data: T): ObservableFuture<T> {
            return ConcreteMutableObservableFuture<T>().apply {
                isSimple = true
                onResult(data)
            }
        }

        /**
         * Create a simple [ObservableFuture] that simply returns the provided error
         *
         * @param error The error to be returned as failure for this future
         * @return A future that simply returns the provided error
         */
        fun <T> withError(error: Throwable): ObservableFuture<T> {
            return ConcreteMutableObservableFuture<T>().apply {
                onResult(error)
            }
        }

        /**
         * Creates an [ObservableFuture] which combines the results of the two provided observables into a single unified result
         * The result will be delivered when both the futures have been completed. If an error is returned by either of the
         * futures, the combination is considered to be in error and the 'first' exception will be returned
         *
         * @param first The first observable to use
         * @param second The second observable to use
         * @return An observable which combines the results of the provided observables. You can use destructuring in the success callback
         * for cleanliness
         */
        fun <T, V> of(first: ObservableFuture<T>, second: ObservableFuture<V>): ObservableFuture<Pair<T, V>> {
            val merged = ConcreteMutableObservableFuture<Pair<T, V>>()
            val listVersion = DelegateMergedMutableObservableFuture(listOf(first, second))
            listVersion.onSuccess {
                @Suppress("UNCHECKED_CAST")
                merged.onResult(Pair(it[0] as T, it[1] as V))
            }
            listVersion.onFailure(merged::onResult)
            listVersion observe onCaller
            return merged
        }

        /**
         * Creates an [ObservableFuture] which combines the results of the three provided observables into a single unified result
         * The result will be delivered when all the futures have been completed. If an error is returned by any of the
         * futures, the combination is considered to be in error and the 'first' exception will be returned
         *
         * @param first The first observable to use
         * @param second The second observable to use
         * @param third The third observable to use
         * @return An observable which combines the results of the provided observables. You can use destructuring in the success callback
         * for cleanliness
         */
        fun <A, B, C> of(first: ObservableFuture<A>, second: ObservableFuture<B>, third: ObservableFuture<C>): ObservableFuture<Triple<A, B, C>> {
            val merged = ConcreteMutableObservableFuture<Triple<A, B, C>>()
            val listVersion = DelegateMergedMutableObservableFuture(listOf(first, second, third))
            listVersion.onSuccess {
                @Suppress("UNCHECKED_CAST")
                merged.onResult(Triple(it[0] as A, it[1] as B, it[2] as C))
            }
            listVersion.onFailure(merged::onResult)
            listVersion observe onCaller
            return merged
        }

        /**
         * Creates a grouped version using an arbitrary number of futures to combine. For the logic see the specialized versions taking
         * two or three arguments
         */
        fun of(list: List<ObservableFuture<*>>): ObservableFuture<List<*>> {
            return DelegateMergedMutableObservableFuture(list)
        }

    }

    /**
     * Register the listener to be called when the future completes with success. The thread on which this callback is executed is
     * based on the manner of observing. This is an infix function for clean usage. This method should only be called once and this should
     * be enforced by implementations
     *
     * @param successListener The listener to be invoked in case of success
     * @return The future itself, allows chaining.
     */
    infix fun onSuccess(successListener: (T) -> Unit): ObservableFuture<T>

    /**
     * Register the listener to be called when the future completes with a failure. The thread on which this callback is executed is
     * based on the manner of observing. This is an infix function for clean usage. This method should only be called once and this should
     * be enforced by implementations
     *
     * @param failureListener The listener to be invoked in case of failure
     * @return The future itself, allows chaining.
     */
    infix fun onFailure(failureListener: (Throwable) -> Unit): ObservableFuture<T>

    /**
     * Execute and wait for the result. If the future would result in an error, this error will be thrown. NEVER execute this on the main thread,
     * implementations should enforce this
     *
     * Note that this method is generally dangerous and should be used with great care. Read the warning below carefully!
     *
     * # Warning
     * Except for [com.icapps.architecture.utils.retrofit.RetrofitObservableFuture] this method will use a latch to wait for the results
     * that are delivered using direct observe. If the underlying future cannot be executed for whatever reason (eg thread could not be started, ...),
     * this method will block. Also, since this is being executed in the context of the future in the first place, special care should be taken
     * with regards to reentrant locks (eg: synchronized(...), ...) as the thread calling [execute] will not be the same thread that is doing
     * the actual executing
     *
     * @param timeout Parameter which gives a suggestion on how long we should wait. For network observables this is ignored.
     * Pass a negative value to wait forever (not recommended)
     * @return The result of the future
     */
    fun execute(timeout: Long): T

    /**
     * Cancels the future. This means the callbacks will never be executed after this method has been called. Some implementations
     * may cancel the actual execution of the background task but this is not a requirement. Cancelling a future should also clear
     * the callback references to prevent memory leaks.
     *
     * # Warning
     * If the future is being observed on a different thread than the thread calling this method, it cannot be guaranteed that the callbacks
     * will not be executed as they could be executed simultaneously with the cancel
     */
    fun cancel()

    /**
     * Observes the future on the android main thread and cancel the future when the lifecycle enters the [Lifecycle.Event.ON_STOP] state.
     * If the results have already been set and/or the results are set from the main thread, the callbacks could be executed before this method
     * returns
     *
     * @param lifecycle The lifecycle to use to cancel the future at the appropriate time
     * @return The future itself
     */
    infix fun observe(lifecycle: Lifecycle): ObservableFuture<T>

    /**
     * Observes the future on the thread that sets the results
     *
     * @param onCaller Tagging object used to indicate observing on caller
     * @return The future itself
     */
    infix fun observe(onCaller: OnCallerTag): ObservableFuture<T>

    /**
     *
     */
    infix fun observe(onMain: OnMainThreadTag): ObservableFuture<T>

    /**
     * Peeks the future to receive its result in a success scenario. The listener is invoked on an unspecified thread but is guaranteed to be
     * called BEFORE the regular success listener (set using [onSuccess])
     *
     * @param listener  Listener to be invoked when the future completes with success.
     * @return The future itself, allows chaining.
     */
    infix fun peek(listener: (T) -> Unit): ObservableFuture<T>

    /**
     * Peeks the future to receive its result in both the success and failure scenarios. The listener is invoked on an unspecified thread but is guaranteed to be
     * called BEFORE the regular success or failure listener (set using [onSuccess] and/or [onFailure])
     *
     * @param listener  Listener to be invoked when the future completes.
     * @return The future itself, allows chaining.
     */
    infix fun peekBoth(listener: (T?, Throwable?) -> Unit): ObservableFuture<T>

    /**
     * Chains this future together with the provided future. On success of this future, pass the result to the provided callback to create
     * the second future. The result of the created future can be observed using the returned future. If this future retuns an error,
     * the error is propagated to the returned future
     *
     * @param chain Creator function taking the result of the first future and creating a new one that can be observed using the return value
     * @return A new future which can be used to observe the result of the future created by [chain]
     */
    infix fun <V> andThen(chain: (T) -> ObservableFuture<V>): ObservableFuture<V> {
        if (this is ConcreteMutableObservableFuture<T>) {
            if (isSimple) {
                @Suppress("UNCHECKED_CAST")
                return try {
                    chain(data as T)
                } catch (e: Throwable) {
                    withError(e)
                }
            }
        }
        val merged = ConcreteMutableObservableFuture<V>()
        onSuccess { firstResult ->
            chain(firstResult) onSuccess (merged::onResult) onFailure (merged::onResult) observe onCaller
        } onFailure (merged::onResult)
        this observe onCaller
        return merged
    }

    /**
     * Same as [andThen] but in this case the returned future also includes the results of this future as well
     *
     * @param chain Creator function taking the result of the first future and creating a new one that can be observed using the return value
     * @return A new future which can be used to observe the result of the future created by [chain]
     */
    infix fun <V> andThenAlso(chain: (T) -> ObservableFuture<V>): ObservableFuture<Pair<T, V>> {
        val merged = ConcreteMutableObservableFuture<Pair<T, V>>()

        if (this is ConcreteMutableObservableFuture<T>) {
            if (isSimple) {
                @Suppress("UNCHECKED_CAST")
                val firstResult = data as T
                return try {
                    chain(firstResult) onSuccess {
                        merged.onResult(Pair(firstResult, it))
                    } onFailure (merged::onResult) observe onCaller
                    merged
                } catch (e: Throwable) {
                    merged.onResult(e)
                    merged
                }
            }
        }

        onSuccess { firstResult ->
            chain(firstResult) onSuccess {
                merged.onResult(Pair(firstResult, it))
            } onFailure (merged::onResult) observe onCaller
        } onFailure (merged::onResult)
        this observe onCaller
        return merged
    }

    /**
     * Creates a new observable future which will return null when this future results in an error. Not that calling this on an already optional future
     * is dangerous. The returned future has the same execute safety as this future. See {@link RetrofitObservableFuture}
     *
     * @return New future which returns null in case of an exception
     */
    fun optional(): ObservableFuture<T?> {
        return OptionalWrapper(this)
    }
}

/**
 * Interface for a future which can receive data or errors
 */
interface MutableObservableFuture<T> : ObservableFuture<T> {

    /**
     * Sets the result of the future to the provided value
     * @param value The 'success' result value
     */
    fun onResult(value: T)

    /**
     * Sets the result of the future to be the provided error
     * @param error The 'failure' result value
     */
    fun onResult(error: Throwable)

}

/**
 * Implementation of [MutableObservableFuture]
 */
open class ConcreteMutableObservableFuture<T> : MutableObservableFuture<T>, LifecycleObserver {

    protected val lock = Any()

    private var dataSet = false
    internal var data: T? = null
    protected var failure: Throwable? = null
    protected var cancelled = false
    protected var observing = false
    private var dispatchToMain = false

    protected var successListener: ((T) -> Unit)? = null
    protected var failureListener: ((Throwable) -> Unit)? = null
    protected var peek: ((T) -> Unit)? = null
    protected var peekBoth: ((T?, Throwable?) -> Unit)? = null
    private var lifecycle: Lifecycle? = null

    internal var isSimple = false

    override fun onSuccess(successListener: (T) -> Unit): ObservableFuture<T> {
        synchronized(lock) {
            if (this.successListener != null)
                throw IllegalStateException("Listener already set")
            this.successListener = successListener
        }
        return this
    }

    override fun onFailure(failureListener: (Throwable) -> Unit): ObservableFuture<T> {
        synchronized(lock) {
            if (this.failureListener != null)
                throw IllegalStateException("Listener already set")
            this.failureListener = failureListener
        }
        return this
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    override fun cancel() {
        lifecycle?.removeObserver(this)
        synchronized(lock) {
            if (cancelled)
                return

            cancelled = true
            successListener = null
            failureListener = null
            failure = null
            data = null
            dataSet = false
            peek = null
        }
    }

    override fun observe(lifecycle: Lifecycle): ConcreteMutableObservableFuture<T> {
        synchronized(lock) {
            if (cancelled)
                return this

            if (observing)
                throw IllegalStateException("Already observing")

            lifecycle.addObserver(this)
            this.lifecycle = lifecycle

            dispatchToMain = true
            observing = true
            checkDispatchState()
        }
        return this
    }

    override fun observe(onCaller: OnCallerTag): ConcreteMutableObservableFuture<T> {
        synchronized(lock) {
            if (cancelled)
                return this

            if (observing)
                throw IllegalStateException("Already observing")

            dispatchToMain = false
            observing = true
            checkDispatchState()
        }
        return this
    }

    override fun observe(onMain: OnMainThreadTag): ConcreteMutableObservableFuture<T> {
        synchronized(lock) {
            if (cancelled)
                return this

            if (observing)
                throw IllegalStateException("Already observing")

            dispatchToMain = true
            observing = true
            checkDispatchState()
        }
        return this
    }

    override fun onResult(value: T) {
        synchronized(lock) {
            if (cancelled || failure != null)
                return

            dataSet = true
            data = value
            peek?.invoke(value)
            peekBoth?.invoke(value, null)
            checkDispatchState()
        }
    }

    override fun onResult(error: Throwable) {
        synchronized(lock) {
            if (cancelled || failure != null)
                return

            failure = error
            peekBoth?.invoke(null, error)
            checkDispatchState()
        }
    }

    /**
     * Check if we have to dispatch the results of the future
     */
    protected open fun checkDispatchState() {
        if (cancelled || !observing)
            return

        failure?.let {
            doDispatch(it)
            successListener = null
            failureListener = null
            peek = null
            observing = false
            return
        }

        if (dataSet) {
            @Suppress("UNCHECKED_CAST")
            doDispatch(data as T)
            data = null
            dataSet = false
        }
    }

    /**
     * Dispatch the results to the listener, delegating to the correct thread if required
     */
    protected fun doDispatch(data: T) {
        synchronized(lock) {
            successListener?.let { listener ->
                if (!dispatchToMain || (Looper.myLooper() === Looper.getMainLooper())) {
                    try {
                        listener(data)
                    } catch (e: Throwable) {
                        onResult(e)
                    }
                } else
                    ObservableFuture.mainDispatcher.post {
                        synchronized(lock) {
                            if (!cancelled) {
                                try {
                                    listener(data)
                                } catch (e: Throwable) {
                                    onResult(e)
                                }
                            }
                        }
                    }
            }
        }
    }

    /**
     * Dispatch the results to the listener, delegating to the correct thread if required
     */
    protected fun doDispatch(failure: Throwable) {
        synchronized(lock) {
            failureListener?.let { listener ->
                if (!dispatchToMain || (Looper.myLooper() == Looper.getMainLooper()))
                    try {
                        listener(failure)
                    } catch (e: Throwable) {
                        //Ignore failure in failure
                    }
                else
                    ObservableFuture.mainDispatcher.post {
                        synchronized(lock) {
                            if (!cancelled) {
                                try {
                                    listener(failure)
                                } catch (e: Throwable) {
                                    //Ignore failure in failure
                                }
                            }
                        }
                    }
            }
        }
    }

    override fun peek(listener: (T) -> Unit): ObservableFuture<T> {
        synchronized(lock) {
            if (!cancelled) {
                peek = listener
                if (dataSet) {
                    @Suppress("UNCHECKED_CAST")
                    listener(data as T)
                }
            }
        }
        return this
    }

    override fun peekBoth(listener: (T?, Throwable?) -> Unit): ObservableFuture<T> {
        synchronized(lock) {
            if (!cancelled) {
                peekBoth = listener
                if (dataSet) {
                    @Suppress("UNCHECKED_CAST")
                    listener(data as T, null)
                } else if (failure != null) {
                    listener(null, failure)
                }
            }
        }
        return this
    }

    @WorkerThread
    override fun execute(timeout: Long): T {
        assertNotMain()
        val latch = CountDownLatch(1)
        var ex: Throwable? = null
        var res: T? = null
        var dataSet = false
        onSuccess {
            res = it
            dataSet = true
            latch.countDown()
        } onFailure {
            ex = it
            latch.countDown()
        } observe onCaller

        if (timeout >= 0)
            latch.await(timeout, TimeUnit.MILLISECONDS)
        else
            latch.await()

        ex?.let { throw it }

        @Suppress("UNCHECKED_CAST")
        if (dataSet)
            return res as T

        throw IllegalStateException("Future finished without result or exception")
    }
}

/**
 * Class used to track the results in [DelegateMergedMutableObservableFuture]
 */
private data class MutableEntry(var data: Any? = null, var set: Boolean = false)

/**
 * Specialized class which takes a list of futures to observe and combines the results into a single result callback
 *
 * @property delegates The delegates to subscribe to
 */
private class DelegateMergedMutableObservableFuture(private val delegates: Collection<ObservableFuture<*>>) : ConcreteMutableObservableFuture<List<*>>() {

    private val results = Array(delegates.size) { MutableEntry() }

    init {
        delegates.forEachIndexed { index, delegate ->
            delegate onSuccess {
                synchronized(results) {
                    if (failure != null)
                        return@onSuccess

                    results[index].apply {
                        set = true
                        data = it
                    }
                    checkDispatchState()
                }
            } onFailure {
                synchronized(results) {
                    if (failure != null)
                        return@onFailure
                    failure = it
                    checkDispatchState()
                }
            }
        }
        delegates.forEach { it observe onCaller }
    }

    override fun cancel() {
        delegates.forEach(ObservableFuture<*>::cancel)
    }

    override fun onResult(value: List<*>) {
        synchronized(lock) {
            if (cancelled || failure != null)
                return

            value.forEachIndexed { index, item ->
                results[index].apply {
                    set = true
                    data = item
                }
            }
            checkDispatchState()
        }
    }

    /**
     * Special override which checks if all the results have been set
     */
    override fun checkDispatchState() {
        if (cancelled || !observing)
            return

        failure?.let {
            doDispatch(it)
            successListener = null
            failureListener = null
            observing = false
            return
        }

        val numResults = results.count(MutableEntry::set)
        if (numResults == results.size) {
            val value = results.map { it.data }
            peek?.invoke(value)
            doDispatch(value)
            successListener = null
            failureListener = null
            observing = false
        }
    }
}

/**
 * Helper extension to turn any value into an [ObservableFuture] which just returns the data. Wraps [ObservableFuture.withData]
 *
 * @receiver Any value that needs to be wrapped
 * @return An [ObservableFuture] which just returns the data
 */
@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
inline fun <T> T.asObservable(): ObservableFuture<T> {
    return ObservableFuture.withData(this)
}

/**
 * Class which wraps an observable future that returns null when the original future signals or throws an error
 */
private class OptionalWrapper<T>(private val delegate: ObservableFuture<T>) : ObservableFuture<T?> {

    private var cancelled = false
    private var failureDispatched = false
    private val lock = Any()
    private var successListener: ((T?) -> Unit)? = null
    private var peek: ((T?) -> Unit)? = null
    private var peekBoth: ((T?, Throwable?) -> Unit)? = null

    init {
        delegate.onFailure {
            synchronized(lock) {
                if (!cancelled && !failureDispatched) {
                    failureDispatched = true
                    successListener?.invoke(null)
                    peek?.invoke(null)
                    peekBoth?.invoke(null, null)
                }
            }
        }
    }

    override fun onSuccess(successListener: (T?) -> Unit): ObservableFuture<T?> {
        delegate.onSuccess(successListener)
        synchronized(lock) {
            if (!cancelled)
                this.successListener = successListener
        }
        return this
    }

    override fun onFailure(failureListener: (Throwable) -> Unit): ObservableFuture<T?> {
        //Ignore
        return this
    }

    override fun execute(timeout: Long): T? {
        return try {
            delegate.execute(timeout)
        } catch (e: Throwable) {
            null
        }
    }

    override fun cancel() {
        synchronized(lock) {
            cancelled = true
            successListener = null
        }
        delegate.cancel()
    }

    @Suppress("UNCHECKED_CAST")
    override fun observe(lifecycle: Lifecycle): ObservableFuture<T?> = delegate.observe(lifecycle) as ObservableFuture<T?>

    @Suppress("UNCHECKED_CAST")
    override fun observe(onCaller: OnCallerTag): ObservableFuture<T?> = delegate.observe(onCaller) as ObservableFuture<T?>

    @Suppress("UNCHECKED_CAST")
    override fun observe(onMain: OnMainThreadTag): ObservableFuture<T?> = delegate.observe(onMain) as ObservableFuture<T?>

    override fun peek(listener: (T?) -> Unit): ObservableFuture<T?> {
        synchronized(lock) {
            if (!cancelled)
                peek = listener
        }
        delegate.peek(listener)
        return this
    }

    override fun peekBoth(listener: (T?, Throwable?) -> Unit): ObservableFuture<T?> {
        synchronized(lock) {
            if (!cancelled)
                peekBoth = listener
        }
        delegate.peekBoth(listener)
        return this
    }

}