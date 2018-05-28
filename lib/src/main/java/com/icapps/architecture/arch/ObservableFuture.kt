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
import com.icapps.architecture.utils.assertNotMain
import java.util.concurrent.CountDownLatch

/**
 * @author Nicola Verbeeck
 * @version 1
 */
class OnCallerTag

val onCaller = OnCallerTag()

interface ObservableFuture<T> {

    companion object {
        val mainDispatcher = Handler(Looper.getMainLooper())

        fun <T> withData(data: T): ObservableFuture<T> {
            return ConcreteMutableObservableFuture<T>().apply {
                onResult(data)
            }
        }

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

        fun of(list: List<ObservableFuture<*>>): ObservableFuture<List<*>> {
            return DelegateMergedMutableObservableFuture(list)
        }

        @WorkerThread
        fun <T> execute(observableFuture: ObservableFuture<T>): T {
            assertNotMain()
            val latch = CountDownLatch(1)
            var ex: Throwable? = null
            var res: T? = null
            var dataSet = false
            observableFuture onSuccess {
                res = it
                dataSet = true
                latch.countDown()
            } onFailure {
                ex = it
                latch.countDown()
            } observe onCaller
            latch.await()
            ex?.let { throw it }

            @Suppress("UNCHECKED_CAST")
            if (dataSet)
                return res as T

            throw IllegalStateException("Future finished without result or exception")
        }

    }

    infix fun onSuccess(successListener: (T) -> Unit): ObservableFuture<T>

    infix fun onFailure(failureListener: (Throwable) -> Unit): ObservableFuture<T>

    fun cancel()

    infix fun observe(lifecycle: Lifecycle)

    infix fun observe(onCaller: OnCallerTag)

    infix fun peek(listener: (T) -> Unit): ObservableFuture<T>

    infix fun <V> andThen(chain: (T) -> ObservableFuture<V>): ObservableFuture<V> {
        val merged = ConcreteMutableObservableFuture<V>()
        onSuccess { firstResult ->
            chain(firstResult) onSuccess (merged::onResult) onFailure (merged::onResult) observe onCaller
        } onFailure (merged::onResult)
        this observe onCaller
        return merged
    }

    infix fun <V> andThenAlso(chain: (T) -> ObservableFuture<V>): ObservableFuture<Pair<T, V>> {
        val merged = ConcreteMutableObservableFuture<Pair<T, V>>()
        onSuccess { firstResult ->
            chain(firstResult) onSuccess {
                merged.onResult(Pair(firstResult, it))
            } onFailure (merged::onResult) observe onCaller
        } onFailure (merged::onResult)
        this observe onCaller
        return merged
    }

}

interface MutableObservableFuture<T> : ObservableFuture<T> {

    fun onResult(value: T)

    fun onResult(error: Throwable)

}

open class ConcreteMutableObservableFuture<T> : MutableObservableFuture<T>, LifecycleObserver {

    protected val lock = Any()

    private var dataSet = false
    private var data: T? = null
    protected var failure: Throwable? = null
    protected var cancelled = false
    protected var observing = false
    private var dispatchToMain = false

    protected var successListener: ((T) -> Unit)? = null
    protected var failureListener: ((Throwable) -> Unit)? = null
    protected var peek: ((T) -> Unit)? = null
    private var lifecycle: Lifecycle? = null

    override fun onSuccess(successListener: (T) -> Unit): ObservableFuture<T> {
        if (this.successListener != null)
            throw IllegalStateException("Listener already set")
        this.successListener = successListener
        return this
    }

    override fun onFailure(failureListener: (Throwable) -> Unit): ObservableFuture<T> {
        if (this.failureListener != null)
            throw IllegalStateException("Listener already set")
        this.failureListener = failureListener
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

    override fun observe(lifecycle: Lifecycle) {
        lifecycle.addObserver(this)
        this.lifecycle = lifecycle
        synchronized(lock) {
            if (cancelled)
                return

            if (observing)
                throw IllegalStateException("Already observing")

            dispatchToMain = true
            observing = true
            checkDispatchState()
        }
    }

    override fun observe(onCaller: OnCallerTag) {
        synchronized(lock) {
            if (cancelled)
                return

            if (observing)
                throw IllegalStateException("Already observing")

            dispatchToMain = false
            observing = true
            checkDispatchState()
        }
    }

    override fun onResult(value: T) {
        synchronized(lock) {
            if (cancelled || failure != null)
                return

            dataSet = true
            data = value
            peek?.invoke(value)
            checkDispatchState()
        }
    }

    override fun onResult(error: Throwable) {
        synchronized(lock) {
            if (cancelled || failure != null)
                return

            failure = error
            checkDispatchState()
        }
    }

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

    protected fun doDispatch(data: T) {
        synchronized(lock) {
            successListener?.let { listener ->
                if (!dispatchToMain || (Looper.myLooper() == Looper.getMainLooper()))
                    listener(data)
                else
                    ObservableFuture.mainDispatcher.post {
                        synchronized(lock) {
                            if (!cancelled)
                                listener(data)
                        }
                    }
            }
        }
    }

    protected fun doDispatch(failure: Throwable) {
        synchronized(lock) {
            failureListener?.let { listener ->
                if (!dispatchToMain || (Looper.myLooper() == Looper.getMainLooper()))
                    listener(failure)
                else
                    ObservableFuture.mainDispatcher.post {
                        synchronized(lock) {
                            if (!cancelled)
                                listener(failure)
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
}

private data class MutableEntry(var data: Any? = null, var set: Boolean = false)

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

@Suppress("UNCHECKED_CAST")
fun <T> Any.asObservable(): ObservableFuture<T> {
    return ObservableFuture.withData(this) as ObservableFuture<T>
}