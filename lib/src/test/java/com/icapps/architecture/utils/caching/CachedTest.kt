package com.icapps.architecture.utils.caching

import android.os.SystemClock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

/**
 * @author Maarten Van Giel
 */
@RunWith(PowerMockRunner::class)
@PrepareForTest(SystemClock::class)
class CachedTest {

    private var cache: String? by Cached(50L)

    @Test
    fun testSetCachedValueAndExpire() {
        PowerMockito.mockStatic(SystemClock::class.java)
        PowerMockito.`when`(SystemClock.elapsedRealtime()).thenReturn(0, 0, 25, 75)

        assertNull(cache)

        cache = "abc"
        assertEquals("abc", cache)

        assertEquals("abc", cache)

        assertNull(cache)
    }

}