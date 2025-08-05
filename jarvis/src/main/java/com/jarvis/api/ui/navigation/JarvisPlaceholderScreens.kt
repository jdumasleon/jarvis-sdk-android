package com.jarvis.api.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.component.DSIcon
import com.jarvis.core.designsystem.theme.DSJarvisTheme

/**
 * Placeholder screens for Jarvis navigation
 * These will be replaced with actual feature screens when they're properly integrated
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderInspectorScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { DSText("Network Inspector") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        DSIcon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DSText(
                    text = "🔍 Network Inspector",
                    style = DSJarvisTheme.typography.heading.heading3,
                    textAlign = TextAlign.Center
                )
                DSText(
                    text = "Feature will be integrated from\nfeatures:inspector:lib module",
                    style = DSJarvisTheme.typography.body.medium,
                    color = DSJarvisTheme.colors.neutral.neutral60,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderPreferencesScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { DSText("Preferences Inspector") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        DSIcon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DSText(
                    text = "⚙️ Preferences Inspector",
                    style = DSJarvisTheme.typography.heading.heading3,
                    textAlign = TextAlign.Center
                )
                DSText(
                    text = "Feature will be integrated from\nfeatures:preferences:lib module",
                    style = DSJarvisTheme.typography.body.medium,
                    color = DSJarvisTheme.colors.neutral.neutral60,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderTransactionDetailScreen(
    transactionId: String,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { DSText("Transaction Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        DSIcon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DSText(
                    text = "📊 Transaction Detail",
                    style = DSJarvisTheme.typography.heading.heading3,
                    textAlign = TextAlign.Center
                )
                DSText(
                    text = "Transaction ID: $transactionId",
                    style = DSJarvisTheme.typography.body.medium,
                    color = DSJarvisTheme.colors.neutral.neutral60,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}