package com.icapps.architecture.utils.caching

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * @author Maarten Van Giel
 */
class CachedTest {

    private var cache: String? by Cached(50L)

    @Test
    fun testSetCachedValueAndExpire() {
        assertNull(cache)

        cache = "abc"
        assertEquals("abc", cache)

        Thread.sleep(25)
        assertEquals("abc", cache)

        Thread.sleep(75)
        assertNull(cache)
    }

}