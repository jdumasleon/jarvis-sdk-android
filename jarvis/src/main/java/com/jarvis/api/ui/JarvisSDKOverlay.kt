package com.jarvis.api.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import com.jarvis.api.detector.ShakeDetectorEffect
import com.jarvis.api.ui.components.JarvisFabButton

/**
 * Main Jarvis SDK overlay component
 * Provides floating UI and shake detection for development tools
 */
@Composable
fun JarvisSDKOverlay(
    modifier: Modifier = Modifier,
    onShowOverlay: () -> Unit,
    onShowInspector: () -> Unit = {},
    onShowPreferences: () -> Unit = {},
    isJarvisActive: Boolean = false,
    enableShakeDetection: Boolean = false,
    onToggleJarvisActive: () -> Unit = {}
) {
    var isJarvisVisible by remember { mutableStateOf(isJarvisActive) }
    
    // Update visibility when active state changes
    LaunchedEffect(isJarvisActive) {
        isJarvisVisible = isJarvisActive
    }
    
    // Shake detection to show Jarvis (only activate when not active)
    if (enableShakeDetection) {
        ShakeDetectorEffect(
            onShakeDetected = {
                onToggleJarvisActive()
            }
        )
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