package com.jarvis.core.common.extensions

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val supportedDateFormat = listOf(
    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
    "yyyy-MM-dd'T'HH:mm:ss.SSS",
    "yyyy-MM-dd'T'HH:mm:ss",
    "yyyy-MM-dd",
    "dd-MM-yyyy'T'HH:mm:ss.SSS'Z'",
    "dd-MM-yyyy'T'HH:mm:ss.SSS",
    "dd-MM-yyyy'T'HH:mm:ss",
    "dd-MM-yyyy",
    "yyyy/MM/dd'T'HH:mm:ss.SSS'Z'",
    "yyyy/MM/dd'T'HH:mm:ss.SSS",
    "yyyy/MM/dd'T'HH:mm:ss",
    "yyyy/MM/dd",
    "dd/MM/yyyy'T'HH:mm:ss.SSS'Z'",
    "dd/MM/yyyy'T'HH:mm:ss.SSS",
    "dd/MM/yyyy'T'HH:mm:ss",
    "dd/MM/yyyy",
    "yyyy.MM.dd'T'HH:mm:ss.SSS'Z'",
    "yyyy.MM.dd'T'HH:mm:ss.SSS",
    "yyyy.MM.dd'T'HH:mm:ss",
    "yyyy.MM.dd",
    "dd.MM.yyyy'T'HH:mm:ss.SSS'Z'",
    "dd.MM.yyyy'T'HH:mm:ss.SSS",
    "dd.MM.yyyy'T'HH:mm:ss",
    "dd.MM.yyyy",
)

fun Date?.format(format: String = "dd-MM-yyyy HH:mm"): String {
    return if (this != null) {
        SimpleDateFormat(format, Locale.getDefault()).format(this)
    } else {
        String.Companion.EMPTY
    }
}
