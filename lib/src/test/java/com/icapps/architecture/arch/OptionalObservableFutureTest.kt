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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.IOException

/**
 * @author Nicola Verbeeck
 * @version 1
 */
class OptionalObservableFutureTest {

    @Test
    fun testOptionalSuccessDispatch() {
        val future = ConcreteMutableObservableFuture<String>()
        val optional = future.optional()

        var result: String? = null
        var error: Throwable? = null
        optional onSuccess {
            result = it
        } onFailure {
            error = it
        } observe onCaller

        future.onResult("Hello world!")

        assertEquals("Hello world!", result)
        assertNull(error)
    }

    @Test
    fun testOptionalFailureDispatch() {
        val future = ConcreteMutableObservableFuture<String>()
        val optional = future.optional()

        var result: String? = ""
        var error: Throwable? = null
        optional onSuccess {
            result = it
        } onFailure {
            error = it
        } observe onCaller

        future.onResult(IOException("Error"))

        assertNull(result)
        assertNull(error)
    }

    @Test
    fun testOptionalFailureInternalDispatch() {
        val future = ConcreteMutableObservableFuture<String>()
        val optional = future.optional()

        var result: String? = ""
        var error: Throwable? = null
        optional onSuccess {
            if (it != null)
                throw IOException()
            result = it
        } onFailure {
            error = it
        } observe onCaller

        future.onResult(IOException("Error"))

        assertNull(result)
        assertNull(error)
    }

    @Test
    fun testOptionalSuccessExecute() {
        val future = ConcreteMutableObservableFuture<String>()
        val optional = future.optional()

        future.onResult("Hello world!")

        val res = optional.execute(100L)

        assertEquals("Hello world!", res)
    }

    @Test
    fun testOptionalFailureExecute() {
        val future = ConcreteMutableObservableFuture<String>()
        val optional = future.optional()

        future.onResult(IOException("Error"))

        assertNull(optional.execute(100L))
    }

}