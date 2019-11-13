package com.icapps.architecture.utils.caching

import android.os.SystemClock
import kotlin.reflect.KProperty

/**
 * @author Maarten Van Giel
 * A utility class to create variables that expire (return null) after a while.
 * Usage: `var cachedValue: T? by Cached(CACHE_VALIDITY_MS)`
 */
class Cached<T>(private val cacheValidity: Long = DEFAULT_CACHE_DURATION) {

    companion object {
        const val DEFAULT_CACHE_DURATION = 60000L
    }

    private var cache: T? = null
    private var timestamp: Long? = null

    operator fun getValue(thisRef: Any, property: KProperty<*>): T? {
        synchronized(this) {
            return timestamp?.let {
                if (SystemClock.elapsedRealtime() - it <= cacheValidity) {
                    cache
                } else {
                    cache = null // Reset cache so we don't keep unnecessary objects in memory
                    null
                }
            }
        }
    }

    operator fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
        synchronized(this) {
            cache = value
            timestamp = if (value == null) null else SystemClock.elapsedRealtime()
        }
    }

}