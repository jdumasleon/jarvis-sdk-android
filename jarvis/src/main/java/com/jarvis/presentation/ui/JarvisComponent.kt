package com.jarvis.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jarvis.core.designsystem.component.DSCard
import com.jarvis.core.designsystem.theme.DSJarvisTheme

@Composable
fun JarvisComponent() {
    var showInspector by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    
    // Main God Mode Dialog
    Dialog(
        onDismissRequest = { /* Can't dismiss by tapping outside */ },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        DSCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DSJarvisTheme.dimensions.m),
            shape = DSJarvisTheme.shapes.l,
            elevation = DSJarvisTheme.elevations.level2
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Jarvis Debug Tools",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { /* Close handled by shake detector */ }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
                
                // Network Inspector Button
                OutlinedButton(
                    onClick = { showInspector = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.NetworkWifi,
                        contentDescription = "Network Inspector",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Network Inspector")
                }
                
                // Settings Button (for future features)
                OutlinedButton(
                    onClick = { showSettings = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Debug Settings")
                }
            }
        }
    }
    
    // Network Inspector Modal
    if (showInspector) {
        NetworkInspectorModal(
            onDismiss = { showInspector = false }
        )
    }
    
    // Settings Modal (placeholder for future features)
    if (showSettings) {
        Dialog(
            onDismissRequest = { showSettings = false }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Debug Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Settings will be available in future versions",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    OutlinedButton(
                        onClick = { showSettings = false },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 16.dp)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
private fun NetworkInspectorModal(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        // TODO: Update to use Navigation3 pattern when needed
        // For now, showing placeholder content
        DSCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = "Network Inspector",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )
            Text(
                text = "Inspector integration available in demo app",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}