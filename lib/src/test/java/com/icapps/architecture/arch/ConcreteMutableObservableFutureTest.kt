package com.icapps.architecture.arch

import junit.framework.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException

/**
 * @author Nicola Verbeeck
 * @version 1
 */
class ConcreteMutableObservableFutureTest {

    companion object {
        const val TIMEOUT = 1000L
    }

    @Test
    fun testAndThenThreaded() {
        val future = createThreadedObservable() andThen {
            createSecondObservable(it)
        }
        val result = future.execute(TIMEOUT)
        assertEquals("Second", result)
    }

    @Test
    fun testAndThenDirectAndThread() {
        val future = createDirectObservable() andThen {
            createSecondObservable(it)
        }
        val result = future.execute(TIMEOUT)
        assertEquals("Second", result)
    }

    @Test
    fun testAndThenDirectAndDirect() {
        val future = createDirectObservable() andThen {
            createSecondDirectObservable(it)
        }
        val result = future.execute(TIMEOUT)
        assertEquals("Second", result)
    }

    @Test
    fun testAndThenTreadAndDirect() {
        val future = createThreadedObservable() andThen {
            createSecondDirectObservable(it)
        }
        val result = future.execute(TIMEOUT)
        assertEquals("Second", result)
    }

    @Test(expected = IOException::class)
    fun testAndThenTreadAndFailDirect() {
        val future = createThreadedObservable() andThen {
            createDirectFailObservable(it)
        }
        future.execute(TIMEOUT)
        fail()
    }

    @Test(expected = IOException::class)
    fun testAndThenTreadAndFail() {
        val future = createThreadedObservable() andThen {
            createThreadedFailObservable(it)
        }
        future.execute(TIMEOUT)
        fail()
    }

    @Test(expected = IOException::class)
    fun testAndThenDirectAndFail() {
        val future = createDirectObservable() andThen {
            createThreadedFailObservable(it)
        }
        future.execute(TIMEOUT)
        fail()
    }

    private fun createThreadedObservable(): ObservableFuture<String> {
        val fut = ConcreteMutableObservableFuture<String>()
        Thread {
            Thread.sleep(100L)
            fut.onResult("First")
        }.start()
        return fut
    }

    private fun createSecondObservable(code: String): ObservableFuture<String> {
        assertEquals("First", code)

        val fut = ConcreteMutableObservableFuture<String>()
        Thread {
            Thread.sleep(100L)
            fut.onResult("Second")
        }.start()
        return fut
    }

    private fun createThreadedFailObservable(code: String): ObservableFuture<String> {
        assertEquals("First", code)

        val fut = ConcreteMutableObservableFuture<String>()
        Thread {
            Thread.sleep(100L)
            fut.onResult(IOException("Not found"))
        }.start()
        return fut
    }

    private fun createDirectObservable(): ObservableFuture<String> {
        return ObservableFuture.withData("First")
    }

    private fun createDirectFailObservable(code: String): ObservableFuture<String> {
        assertEquals("First", code)

        val ret = ConcreteMutableObservableFuture<String>()
        ret.onResult(IOException("Not found"))
        return ret
    }

    private fun createSecondDirectObservable(code: String): ObservableFuture<String> {
        assertEquals("First", code)
        return ObservableFuture.withData("Second")
    }
    
}
