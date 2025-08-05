package com.jarvis.api.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.jarvis.core.designsystem.theme.DSJarvisTheme

/**
 * Floating action button system for Jarvis SDK
 * Shows main Jarvis button that expands to reveal tool-specific buttons
 */
@Composable
fun JarvisFloatingActions(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onNetworkInspectorClick: () -> Unit,
    onPreferencesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        // Semi-transparent overlay when expanded
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable { onToggle() }
                    .semantics { role = Role.Button }
            )
        }
        
        // Tool buttons column
        Column(
            modifier = Modifier.padding(DSJarvisTheme.dimensions.l),
            verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.dimensions.m),
            horizontalAlignment = Alignment.End
        ) {
            // Preferences Inspector Button
            JarvisToolButton(
                visible = isExpanded,
                icon = Icons.Default.Settings,
                contentDescription = "Preferences Inspector",
                onClick = onPreferencesClick,
                animationDelay = 100
            )
            
            // Network Inspector Button
            JarvisToolButton(
                visible = isExpanded,
                icon = Icons.Default.NetworkWifi,
                contentDescription = "Network Inspector",
                onClick = onNetworkInspectorClick,
                animationDelay = 50
            )
            
            // Main Jarvis Button
            JarvisMainButton(
                isExpanded = isExpanded,
                onClick = onToggle
            )
        }
    }
}

@Composable
private fun JarvisToolButton(
    visible: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    animationDelay: Int,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            animationSpec = tween(300, delayMillis = animationDelay)
        ) + fadeIn(
            animationSpec = tween(300, delayMillis = animationDelay)
        ),
        exit = scaleOut(
            animationSpec = tween(200)
        ) + fadeOut(
            animationSpec = tween(200)
        ),
        modifier = modifier
    ) {
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = DSJarvisTheme.colors.primary.primary60,
            contentColor = DSJarvisTheme.colors.neutral.neutral0,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun JarvisMainButton(
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = tween(300),
        label = "main_button_rotation"
    )
    
    FloatingActionButton(
        onClick = onClick,
        containerColor = DSJarvisTheme.colors.primary.primary100,
        contentColor = DSJarvisTheme.colors.neutral.neutral0,
        modifier = modifier.size(56.dp)
    ) {
        Icon(
            imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.BugReport,
            contentDescription = if (isExpanded) "Close Jarvis Tools" else "Open Jarvis Tools",
            modifier = Modifier
                .size(24.dp)
                .rotate(rotation)
        )
    }
}