package com.icapps.architecture.utils.ext

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.database.Cursor
import android.net.ConnectivityManager
import java.io.Closeable

/**
 * @author Nicola Verbeeck
 */
fun Closeable?.closeSilently() {
    if (this == null)
        return

    try {
        close()
    } catch (ignore: Throwable) {
    }
}

inline fun Cursor.forEach(block: Cursor.() -> Unit) {
    do {
        block()
    } while (moveToNext())
}

inline fun <T> Cursor.map(block: Cursor.() -> T): List<T> {
    if (!moveToFirst())
        return emptyList()

    val data = mutableListOf<T>()
    do {
        data.add(block())
    } while (moveToNext())
    return data
}

inline fun TypedArray.use(block: TypedArray.() -> Unit) {
    try {
        block()
    } finally {
        recycle()
    }
}

@SuppressLint("MissingPermission")
fun Context.hasNetwork(): Boolean {
    return (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo != null
}
