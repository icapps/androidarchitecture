package com.icapps.architecture.arch

/**
 * @author Nicola Verbeeck
 */
class AsyncMemoizer<T> {

    private val lock = Any()
    private var didRun = false
    private var value: T? = null
    private var valueSet: Boolean = false
    private var error: Throwable? = null

    private val waitingFutures = mutableListOf<ConcreteMutableObservableFuture<T>>()

    @Suppress("UNCHECKED_CAST")
    val future: ObservableFuture<T>
        get() {
            synchronized(lock) {
                if (valueSet) return ObservableFuture.withData(value as T)
                if (error != null) return ObservableFuture.withError(error!!)

                val future = ConcreteMutableObservableFuture<T>()
                waitingFutures.add(future)
                return future
            }
        }

    fun runOnce(futureBuilder: () -> ObservableFuture<T>): ObservableFuture<T> {
        synchronized(lock) {
            if (didRun) return future
            didRun = true
        }
        futureBuilder().onSuccess { result ->
            synchronized(lock) {
                valueSet = true
                value = result

                waitingFutures.forEach { it.onResult(result) }
                waitingFutures.clear()
            }
        } onFailure { error ->
            synchronized(lock) {
                this.error = error

                waitingFutures.forEach { it.onResult(error) }
                waitingFutures.clear()
            }
        } observe onCaller

        return future
    }

}