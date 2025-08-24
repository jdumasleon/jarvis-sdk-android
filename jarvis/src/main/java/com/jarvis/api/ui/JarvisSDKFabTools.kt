package com.jarvis.api.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import com.jarvis.api.ui.components.JarvisFabButton
import com.jarvis.core.designsystem.component.DSJarvisAssistant
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.designsystem.utils.ShakeDetectorEffect

/**
 * Main Jarvis SDK overlay component
 * Provides floating UI and shake detection for development tools
 */
@Composable
fun JarvisSDKFabTools(
    modifier: Modifier = Modifier,
    onShowOverlay: () -> Unit,
    onShowInspector: () -> Unit = {},
    onShowPreferences: () -> Unit = {},
    isJarvisActive: Boolean = false,
) {
    var isJarvisVisible by rememberSaveable { mutableStateOf(isJarvisActive) }

    LaunchedEffect(isJarvisActive) {
        isJarvisVisible = isJarvisActive
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        if (isJarvisVisible) {
            JarvisFabButton(
                onInspectorClick = onShowInspector,
                onPreferencesClick = onShowPreferences,
                onHomeClick = onShowOverlay,
                modifier = Modifier.zIndex(Float.MAX_VALUE)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun JarvisSDKFabToolsPreview() {
    DSJarvisTheme {
        JarvisSDKFabTools(
            onShowOverlay = {},
            isJarvisActive = true
        )
    }
}
