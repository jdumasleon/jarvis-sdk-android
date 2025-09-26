@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.core.internal.presentation.utils

import androidx.annotation.RestrictTo

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

fun openUrl(url: String, context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    context.startActivity(intent)
}

fun shareUrl(url: String, context: Context) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, url)
    }

    val shareIntent = Intent.createChooser(sendIntent, "Share link via")
    context.startActivity(shareIntent)
}