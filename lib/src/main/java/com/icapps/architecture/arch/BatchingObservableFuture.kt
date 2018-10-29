package com.icapps.architecture.arch

/**
 * Wrapper for creating groups of futures which only execute once per delegate. This means, for example, that you can create the 10 observers for the same network call result.
 *
 * @author Nicola Verbeeck
 */
class BatchingObservableFuture<T>(private val peek: ((T) -> Unit)? = null) {

    private val lock = Any()
    private var inFlight = false
    private var delegate: ObservableFuture<T>? = null
    private val listeners = mutableListOf<MutableObservableFuture<T>>()

    /**
     * Creates a new listener for the batching future, optionally creating a new delegate to actually execute the underlying request
     *
     * @return A new listener that will be notified when the delegate finishes executing
     */
    fun create(delegateCreator: () -> ObservableFuture<T>): ObservableFuture<T> {
        val newListener = synchronized(lock) {
            if (inFlight)
                return createListener()
            inFlight = true
            createListener()
        }
        delegate = delegateCreator() onSuccess (::notifySuccess) onFailure (::notifyFailure) observe onCaller
        return newListener
    }

    /**
     * Cancels the future, all listeners will be detached and the delegate will be cancelled
     */
    fun cancel() {
        synchronized(lock) {
            listeners.clear()
            delegate?.cancel()
            delegate = null
            inFlight = false
        }
    }

    private fun createListener(): ObservableFuture<T> {
        val future = ConcreteMutableObservableFuture<T>()
        listeners += future
        return future
    }

    private fun notifySuccess(data: T) {
        peek?.invoke(data)
        synchronized(lock) {
            listeners.forEach {
                try {
                    it.onResult(data)
                } catch (e: Throwable) {
                    try {
                        it.onResult(e)
                    } catch (e: Throwable) {
                        //Swallow to prevent affecting other listeners
                    }
                }
            }
            listeners.clear()
            inFlight = false
            delegate = null
        }
    }

    private fun notifyFailure(error: Throwable) {
        synchronized(lock) {
            listeners.forEach {
                try {
                    it.onResult(error)
                } catch (e: Throwable) {
                    //Swallow to prevent affecting other listeners
                }
            }
            listeners.clear()
            inFlight = false
            delegate = null
        }
    }

}