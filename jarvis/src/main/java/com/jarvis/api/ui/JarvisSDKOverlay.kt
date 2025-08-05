package com.jarvis.api.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import com.jarvis.api.detector.ShakeDetectorEffect

/**
 * Main Jarvis SDK overlay component
 * Provides floating UI and shake detection for development tools
 */
@Composable
fun JarvisSDKOverlay(
    onShowOverlay: () -> Unit,
    modifier: Modifier = Modifier,
    enableShakeDetection: Boolean = true
) {
    var isJarvisVisible by remember { mutableStateOf(false) }
    var isFloatingExpanded by remember { mutableStateOf(false) }
    
    // Shake detection to show/hide Jarvis
    if (enableShakeDetection) {
        ShakeDetectorEffect(
            onShakeDetected = {
                isJarvisVisible = !isJarvisVisible
                if (!isJarvisVisible) {
                    isFloatingExpanded = false
                }
            }
        )
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Floating actions overlay
        if (isJarvisVisible) {
            JarvisFloatingActions(
                isExpanded = isFloatingExpanded,
                onToggle = { 
                    isFloatingExpanded = !isFloatingExpanded 
                },
                onNetworkInspectorClick = {
                    onShowOverlay()
                    isFloatingExpanded = false
                },
                onPreferencesClick = {
                    onShowOverlay()
                    isFloatingExpanded = false
                },
                modifier = Modifier.zIndex(Float.MAX_VALUE)
            )
        }
    }
}

/**
 * Simplified version for manual control
 */
@Composable
fun JarvisFloatingOverlay(
    onShowOverlay: () -> Unit,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFloatingExpanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier.fillMaxSize()) {
        if (isVisible) {
            JarvisFloatingActions(
                isExpanded = isFloatingExpanded,
                onToggle = { 
                    isFloatingExpanded = !isFloatingExpanded 
                },
                onNetworkInspectorClick = {
                    onShowOverlay()
                    isFloatingExpanded = false
                },
                onPreferencesClick = {
                    onShowOverlay()
                    isFloatingExpanded = false
                },
                modifier = Modifier.zIndex(Float.MAX_VALUE)
            )
        }
    }
}