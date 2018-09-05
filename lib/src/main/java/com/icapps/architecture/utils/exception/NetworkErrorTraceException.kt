package com.icapps.architecture.utils.exception

import com.icapps.architecture.utils.retrofit.ServiceException
import java.io.IOException

/**
 * @author Nicola Verbeeck
 */
class NetworkErrorTraceException : IOException() {

    private var selfMessage: String? = null

    fun init(cause: Throwable): NetworkErrorTraceException {
        initCause(cause)
        selfMessage = cause.message
        return this
    }

    override val message: String?
        get() = selfMessage ?: super.message
}

fun Throwable.asServiceException(): Throwable? {
    if (this is ServiceException)
        return this
    if (cause is ServiceException)
        return cause
    return this
}