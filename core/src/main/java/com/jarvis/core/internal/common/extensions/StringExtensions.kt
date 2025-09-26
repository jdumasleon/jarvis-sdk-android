@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)

package com.jarvis.core.internal.common.extensions

import androidx.annotation.RestrictTo

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal val String.Companion.EMPTY: String
    get() = ""

internal val String.Companion.HYPHEN: String
    get() = "-"

fun String.toDate(): Date? {
    for (supportedFormat in supportedDateFormat) {
        try {
            val dateFormat = SimpleDateFormat(supportedFormat, Locale.getDefault())
            dateFormat.isLenient = false
            return dateFormat.parse(this)
        } catch (_: Exception) {
        }
    }
    return null
}

fun String.isValidDate(): Boolean {
    for (format in supportedDateFormat) {
        try {
            val dateFormat = SimpleDateFormat(format, Locale.getDefault())
            dateFormat.isLenient = false
            dateFormat.parse(this)
            return true
        } catch (e: Exception) {
            // Ignore exception and try the next format
        }
    }
    return false
}