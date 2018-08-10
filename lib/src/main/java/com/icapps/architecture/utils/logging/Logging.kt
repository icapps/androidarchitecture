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

@file:Suppress("TooManyFunctions")

package com.icapps.architecture.utils.logging

import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter


/**
 * @author Nicola Verbeeck
 * @version 1
 */
object Logger {
    var ENABLED = true
    var VERBOSE_ENABLED = true

    private const val MAX_LOG_LENGTH = 4000
    private const val MAX_TAG_LENGTH = 23
    private const val STACK_TRACE_BUFFER_SIZE = 256
    private val placeHolderRegex = Regex("\\{\\}")

    @Suppress("NOTHING_TO_INLINE")
    inline fun debug(tag: String, message: String, arguments: Array<out Any?>?) {
        if (ENABLED) {
            val args = extractException(arguments)
            doLog(Log.DEBUG, tag, prefix(createMessage(message, args?.second)), args?.first)
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun info(tag: String, message: String, arguments: Array<out Any?>?) {
        if (ENABLED) {
            val args = extractException(arguments)
            doLog(Log.INFO, tag, prefix(createMessage(message, args?.second)), args?.first)
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun warning(tag: String, message: String, arguments: Array<out Any?>?) {
        if (ENABLED) {
            val args = extractException(arguments)
            doLog(Log.WARN, tag, prefix(createMessage(message, args?.second)), args?.first)
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun error(tag: String, message: String, arguments: Array<out Any?>?) {
        if (ENABLED) {
            val args = extractException(arguments)
            doLog(Log.ERROR, tag, prefix(createMessage(message, args?.second)), args?.first)
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun verbose(tag: String, message: String, arguments: Array<out Any?>?) {
        if (ENABLED && VERBOSE_ENABLED) {
            val args = extractException(arguments)
            doLog(Log.VERBOSE, tag, prefix(createMessage(message, args?.second)), args?.first)
        }
    }

    /**
     * Public for inline
     */
    fun prefix(message: String): String {
        return "[${Thread.currentThread().name}] $message"
    }

    /**
     * Public for inline
     */
    fun extractException(arguments: Array<out Any?>?): Pair<Throwable?, Array<out Any?>>? {
        if (arguments == null || arguments.isEmpty())
            return null

        val ex: Throwable?
        val args: Array<out Any?>
        if (arguments[0] is Throwable) {
            ex = arguments[0] as Throwable
            args = arguments.sliceArray(1 until arguments.size)
        } else {
            ex = null
            args = arguments
        }

        return ex to args
    }

    /**
     * Public for inline
     */
    fun createMessage(message: String, second: Array<out Any?>?): String {
        if (second == null || second.isEmpty())
            return message

        var index = 0
        return message.replace(placeHolderRegex) {
            val idxCopy = index++
            if (idxCopy < second.size)
                eval(second[idxCopy])
            else
                "<error not enough arguments>"
        }
    }

    private fun eval(any: Any?): String {
        if (any == null)
            return "null"

        if (any is Function0<*>)
            return eval(any.invoke())

        return any.toString()
    }

    /**
     * Public for inline
     */
    fun doLog(priority: Int, tag: String, message: String, t: Throwable?) {
        val actualTag = if (tag.length > MAX_TAG_LENGTH) tag.substring(0, MAX_TAG_LENGTH) else tag

        val actualMessage: String = if (t != null)
            "$message\n+${getStackTraceString(t)}"
        else
            message

        if (actualMessage.length < MAX_LOG_LENGTH) {
            if (priority == Log.ASSERT) {
                Log.wtf(actualTag, actualMessage)
            } else {
                Log.println(priority, actualTag, actualMessage)
            }
            return
        }

        var i = 0
        val length = actualMessage.length
        while (i < length) {
            var newline = actualMessage.indexOf('\n', i)
            newline = if (newline != -1) newline else length
            do {
                val end = Math.min(newline, i + MAX_LOG_LENGTH)
                val part = actualMessage.substring(i, end)
                if (priority == Log.ASSERT) {
                    Log.wtf(actualTag, part)
                } else {
                    Log.println(priority, actualTag, part)
                }
                i = end
            } while (i < newline)
            ++i
        }
    }

    private fun getStackTraceString(t: Throwable): String {
        val sw = StringWriter(STACK_TRACE_BUFFER_SIZE)
        val pw = PrintWriter(sw, false)
        t.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }

}

@Suppress("NOTHING_TO_INLINE")
inline fun Any.logVerbose(msg: String, vararg args: Any?) {
    Logger.verbose(javaClass.simpleName, msg, args)
}

@Suppress("NOTHING_TO_INLINE")
inline fun Any.logDebug(msg: String, vararg args: Any?) {
    Logger.debug(javaClass.simpleName, msg, args)
}

@Suppress("NOTHING_TO_INLINE")
inline fun Any.logInfo(msg: String, vararg args: Any?) {
    Logger.info(javaClass.simpleName, msg, args)
}

@Suppress("NOTHING_TO_INLINE")
inline fun Any.logWarn(msg: String, vararg args: Any?) {
    Logger.warning(javaClass.simpleName, msg, args)
}

@Suppress("NOTHING_TO_INLINE")
inline fun Any.logError(msg: String, vararg args: Any?) {
    Logger.error(javaClass.simpleName, msg, args)
}