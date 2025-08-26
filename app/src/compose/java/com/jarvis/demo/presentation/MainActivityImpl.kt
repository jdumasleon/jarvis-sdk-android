package com.jarvis.demo.presentation

import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.demo.presentation.ui.JarvisDemoApp

fun MainActivity.setupUI() {
    setContent {
        val darkTheme = isSystemInDarkTheme()
        DSJarvisTheme(darkTheme = darkTheme) {
            JarvisDemoApp(
                navigator = navigator,
                entryProviderBuilders = entryProviderBuilders,
                jarvisSDK = jarvisSDK
            )
        }
    }
}