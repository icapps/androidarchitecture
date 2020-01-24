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

package com.icapps.architecture.utils.ext

import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

/**
 * @author Nicola Verbeeck
 * @version 1
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <T : ViewDataBinding> Int.bindContentView(activity: Activity): T {
    return DataBindingUtil.setContentView(activity, this)!!
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T : ViewDataBinding> Int.inflate(layoutInflater: LayoutInflater, into: ViewGroup?, attach: Boolean = false): T {
    return DataBindingUtil.inflate(layoutInflater, this, into, attach)!!
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T : ViewDataBinding> Int.inflate(context: Context, into: ViewGroup?, attach: Boolean = false): T {
    return inflate(LayoutInflater.from(context), into, attach)
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T : ViewDataBinding> Int.inflate(into: ViewGroup, attach: Boolean = false): T {
    return inflate(LayoutInflater.from(into.context), into, attach)
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T : ViewDataBinding> Int.inflate(fragment: Fragment, into: ViewGroup?, attach: Boolean = false): T {
    return inflate(fragment.activity, into, attach)
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T : ViewDataBinding> Int.inflate(fragment: androidx.fragment.app.Fragment, into: ViewGroup?, attach: Boolean = false): T {
    return inflate(fragment.requireContext(), into, attach)
}

@Suppress("NOTHING_TO_INLINE")
inline fun Int.str(context: Context): String = context.getString(this)

@Suppress("NOTHING_TO_INLINE", "SpreadOperator")
inline fun Int.str(context: Context, vararg formatArgs: Any): String = context.getString(this, *formatArgs)

@Suppress("NOTHING_TO_INLINE")
inline fun Int.str(resources: Resources): String = resources.getString(this)

@Suppress("NOTHING_TO_INLINE")
@ColorInt
inline fun Int.color(context: Context): Int = ContextCompat.getColor(context, this)

@Suppress("NOTHING_TO_INLINE")
inline fun Int.colors(context: Context): ColorStateList = ContextCompat.getColorStateList(context, this)!!

@Suppress("NOTHING_TO_INLINE")
inline fun Int.drawable(context: Context): Drawable? = ContextCompat.getDrawable(context, this)

@Suppress("NOTHING_TO_INLINE")
inline fun Int.dpi(context: Context): Int = context.resources.getDimensionPixelSize(this)

@Suppress("NOTHING_TO_INLINE")
inline fun Int.dpif(context: Context): Float = context.resources.getDimension(this)

@Suppress("NOTHING_TO_INLINE")
inline fun Int.intRes(context: Context): Int = context.resources.getInteger(this)

@Suppress("NOTHING_TO_INLINE")
inline fun Int.sp(context: Context): Float = context.resources.getDimension(this)

/**
 * @receiver The integer to scale using the android screen density
 * @param context The context to extract the metrics from
 * @return The integer scaled to the density
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Int.toDpi(context: Context): Float = this * context.resources.displayMetrics.density

/**
 * @receiver The integer to scale interpret as SP and convert into pixels
 * @param context The context to extract the metrics from
 * @return The integer scaled to the current SP density
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Int.spToPx(context: Context): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.toFloat(), context.resources.displayMetrics).toInt()

@Suppress("NOTHING_TO_INLINE")
inline fun View.hideKeyboard(): Boolean {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    return imm.hideSoftInputFromWindow(windowToken, 0)
}

@Suppress("NOTHING_TO_INLINE")
inline fun View.showKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
}

@Suppress("NOTHING_TO_INLINE")
inline fun Activity.hideKeyboard(): Boolean {
    return findViewById<View>(android.R.id.content)?.hideKeyboard() ?: false
}

@Suppress("NOTHING_TO_INLINE")
inline fun Activity.showKeyboard() {
    findViewById<View>(android.R.id.content)?.showKeyboard()
}

@Suppress("NOTHING_TO_INLINE")
inline fun Int.bool(context: Context) = context.resources.getBoolean(this)

@Suppress("NOTHING_TO_INLINE")
inline fun Int.stringArray(context: Context) = context.resources.getStringArray(this)