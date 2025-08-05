package com.jarvis.api.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jarvis.core.designsystem.component.*
import com.jarvis.core.designsystem.theme.DSJarvisTheme

/**
 * Main screen for Jarvis SDK showing available tools
 * This is the entry point when users open the Jarvis overlay
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JarvisHomeScreen(
    onNavigateToInspector: () -> Unit,
    onNavigateToPreferences: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { DSText("Jarvis SDK") },
                actions = {
                    IconButton(onClick = onDismiss) {
                        DSIcon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Jarvis"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            DSText(
                text = "Developer Tools",
                style = DSJarvisTheme.typography.heading.heading2,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            DSText(
                text = "Access debugging and inspection tools for your application",
                style = DSJarvisTheme.typography.body.medium,
                color = DSJarvisTheme.colors.neutral.neutral60,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Network Inspector Tool
            JarvisToolCard(
                title = "Network Inspector",
                description = "Monitor HTTP/HTTPS requests, responses, and network performance",
                icon = Icons.Default.NetworkCheck,
                onClick = onNavigateToInspector
            )
            
            // Preferences Inspector Tool
            JarvisToolCard(
                title = "Preferences Inspector",
                description = "View and manage SharedPreferences and DataStore values",
                icon = Icons.Default.Settings,
                onClick = onNavigateToPreferences
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Footer info
            DSText(
                text = "Jarvis SDK v1.0.0",
                style = DSJarvisTheme.typography.body.small,
                color = DSJarvisTheme.colors.neutral.neutral40,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun JarvisToolCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    DSCard(
        modifier = modifier.fillMaxWidth(),
        shape = DSJarvisTheme.shapes.m,
        elevation = DSJarvisTheme.elevations.level2
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DSIcon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DSJarvisTheme.colors.primary.primary100
                )
                
                DSText(
                    text = title,
                    style = DSJarvisTheme.typography.heading.heading4,
                    fontWeight = FontWeight.Bold
                )
            }
            
            DSText(
                text = description,
                style = DSJarvisTheme.typography.body.medium,
                color = DSJarvisTheme.colors.neutral.neutral60
            )
            
            DSButton(
                text = "Open $title",
                style = DSButtonStyle.PRIMARY,
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun JarvisHomeScreenPreview() {
    DSJarvisTheme {
        JarvisHomeScreen(
            onNavigateToInspector = {},
            onNavigateToPreferences = {},
            onDismiss = {}
        )
    }
}