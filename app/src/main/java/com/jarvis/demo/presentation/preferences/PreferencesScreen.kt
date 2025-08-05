package com.jarvis.demo.presentation.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jarvis.core.designsystem.component.DSCard
import com.jarvis.core.designsystem.component.DSCircularProgressIndicator
import com.jarvis.core.designsystem.component.DSSearchBar
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.component.DSTextField
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.presentation.state.ResourceState
import com.jarvis.demo.R

@Composable
fun PreferencesScreen(
    modifier: Modifier = Modifier,
    viewModel: PreferencesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(DSJarvisTheme.spacing.l)
    ) {
        // Header
        DSText(
            text = "Demo App Preferences",
            style = DSJarvisTheme.typography.heading.heading5,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = DSJarvisTheme.spacing.s)
        )
        
        DSText(
            text = "View and manage different types of preferences used in this demo app",
            style = DSJarvisTheme.typography.body.medium,
            color = DSJarvisTheme.colors.neutral.neutral60,
            modifier = Modifier.padding(bottom = DSJarvisTheme.spacing.l)
        )
        
        when (val currentState = uiState) {
            is ResourceState.Loading -> LoadingIndicator()
            is ResourceState.Error -> ErrorMessage(currentState.message ?: "Unknown error", currentState.exception)
            is ResourceState.Success -> PreferencesContent(
                data = currentState.data,
                onEvent = viewModel::onEvent
            )
            else -> LoadingIndicator()
        }
    }
}

@Composable
private fun PreferencesContent(
    data: PreferencesUiData,
    onEvent: (PreferencesEvent) -> Unit
) {
    Column {
        // Search Bar
        DSSearchBar(
            searchText = data.searchQuery,
            onValueChange = { onEvent(PreferencesEvent.SearchQueryChanged(it)) },
            onTextClean = { onEvent(PreferencesEvent.SearchQueryChanged("")) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = DSJarvisTheme.spacing.m)
        )
        
        // Tab Row
        TabRow(
            selectedTabIndex = data.selectedTab.ordinal,
            modifier = Modifier.fillMaxWidth()
        ) {
            PreferenceStorageType.values().forEach { storageType ->
                Tab(
                    selected = data.selectedTab == storageType,
                    onClick = { onEvent(PreferencesEvent.SelectTab(storageType)) },
                    text = {
                        Text(
                            text = when (storageType) {
                                PreferenceStorageType.SHARED_PREFERENCES -> "SharedPrefs"
                                PreferenceStorageType.PREFERENCES_DATASTORE -> "DataStore"
                                PreferenceStorageType.PROTO_DATASTORE -> "Proto"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.m))
        
        // Tab Content Info
        TabContentHeader(storageType = data.selectedTab)
        
        Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.m))
        
        // Preferences List
        if (data.filteredPreferences.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
            ) {
                items(data.filteredPreferences) { preference ->
                    PreferenceItem(
                        preference = preference,
                        onValueChanged = { newValue ->
                            onEvent(PreferencesEvent.UpdatePreference(
                                preference.key,
                                newValue,
                                preference.type
                            ))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TabContentHeader(storageType: PreferenceStorageType) {
    val (title, description) = when (storageType) {
        PreferenceStorageType.SHARED_PREFERENCES -> Pair(
            "SharedPreferences",
            "Legacy XML-based key-value storage. Synchronous operations, stored in shared_prefs/ directory."
        )
        PreferenceStorageType.PREFERENCES_DATASTORE -> Pair(
            "Preferences DataStore",
            "Modern reactive preferences with type safety. Asynchronous operations built on Flow."
        )
        PreferenceStorageType.PROTO_DATASTORE -> Pair(
            "Proto DataStore",
            "Structured data storage using Protocol Buffers. Type-safe schema-based storage."
        )
    }
    
    DSCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.m)
        ) {
            DSText(
                text = title,
                style = DSJarvisTheme.typography.body.large,
                fontWeight = FontWeight.Medium,
                color = DSJarvisTheme.colors.primary.primary40
            )
            
            Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.xs))
            
            DSText(
                text = description,
                style = DSJarvisTheme.typography.body.small,
                color = DSJarvisTheme.colors.neutral.neutral60
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DSText(
            text = "No preferences found",
            style = DSJarvisTheme.typography.body.medium,
            color = DSJarvisTheme.colors.neutral.neutral40
        )
        
        DSText(
            text = "Try adjusting your search query",
            style = DSJarvisTheme.typography.body.small,
            color = DSJarvisTheme.colors.neutral.neutral40
        )
    }
}

@Composable
private fun LoadingIndicator() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DSCircularProgressIndicator(
            modifier = Modifier.size(DSJarvisTheme.dimensions.xxxl),
            color = DSJarvisTheme.colors.primary.primary40
        )
        
        Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.m))
        
        DSText(
            text = stringResource(R.string.loading),
            style = DSJarvisTheme.typography.body.medium,
            color = DSJarvisTheme.colors.neutral.neutral60
        )
    }
}

@Composable
private fun ErrorMessage(
    message: String?,
    throwable: Throwable?
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DSText(
            text = "Error loading preferences",
            style = DSJarvisTheme.typography.body.large,
            color = DSJarvisTheme.colors.error.error40,
            fontWeight = FontWeight.Medium
        )
        
        message?.let {
            Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.s))
            DSText(
                text = it,
                style = DSJarvisTheme.typography.body.medium,
                color = DSJarvisTheme.colors.neutral.neutral60
            )
        }
    }
}

@Composable
private fun PreferenceItem(
    preference: PreferenceItem,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var editableValue by remember(preference.value) { mutableStateOf(preference.value) }
    var isEditing by remember { mutableStateOf(false) }
    
    DSCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.m)
        ) {
            // Key and Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    DSText(
                        text = preference.key,
                        style = DSJarvisTheme.typography.body.medium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    DSText(
                        text = preference.type.name.lowercase(),
                        style = DSJarvisTheme.typography.body.small,
                        color = DSJarvisTheme.colors.neutral.neutral40
                    )
                }
                
                // Type indicator
                TypeIndicator(type = preference.type)
            }
            
            Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.s))
            
            // Value editor based on type
            when (preference.type) {
                PreferenceType.BOOLEAN -> {
                    Switch(
                        checked = preference.value.toBoolean(),
                        onCheckedChange = { checked ->
                            onValueChanged(checked.toString())
                        }
                    )
                }
                
                PreferenceType.PROTO -> {
                    // Proto preferences are read-only in this demo
                    DSText(
                        text = preference.value,
                        style = DSJarvisTheme.typography.body.medium,
                        color = DSJarvisTheme.colors.neutral.neutral40,
                        fontWeight = FontWeight.Light
                    )
                    
                    DSText(
                        text = "(Read-only)",
                        style = DSJarvisTheme.typography.body.small,
                        color = DSJarvisTheme.colors.neutral.neutral40
                    )
                }
                
                PreferenceType.STRING, PreferenceType.NUMBER -> {
                    if (isEditing) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DSTextField(
                                text = editableValue,
                                onValueChange = { editableValue = it },
                                modifier = Modifier.weight(1f),
                                placeholder = "Enter value..."
                            )
                            
                            Spacer(modifier = Modifier.width(DSJarvisTheme.spacing.s))
                            
                            // Save button
                            Box(
                                modifier = Modifier
                                    .background(
                                        DSJarvisTheme.colors.primary.primary40,
                                        DSJarvisTheme.shapes.xs
                                    )
                                    .clickable {
                                        onValueChanged(editableValue)
                                        isEditing = false
                                    }
                                    .padding(DSJarvisTheme.spacing.s)
                            ) {
                                DSText(
                                    text = "Save",
                                    style = DSJarvisTheme.typography.body.small,
                                    color = Color.White
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(DSJarvisTheme.spacing.xs))
                            
                            // Cancel button
                            Box(
                                modifier = Modifier
                                    .background(
                                        DSJarvisTheme.colors.neutral.neutral20,
                                        DSJarvisTheme.shapes.xs
                                    )
                                    .clickable {
                                        editableValue = preference.value
                                        isEditing = false
                                    }
                                    .padding(DSJarvisTheme.spacing.s)
                            ) {
                                DSText(
                                    text = "Cancel",
                                    style = DSJarvisTheme.typography.body.small,
                                    color = DSJarvisTheme.colors.neutral.neutral60
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DSText(
                                text = preference.value,
                                style = DSJarvisTheme.typography.body.medium,
                                color = DSJarvisTheme.colors.neutral.neutral60,
                                modifier = Modifier.weight(1f)
                            )
                            
                            // Edit button
                            Box(
                                modifier = Modifier
                                    .background(
                                        DSJarvisTheme.colors.neutral.neutral20,
                                        DSJarvisTheme.shapes.xs
                                    )
                                    .clickable { isEditing = true }
                                    .padding(DSJarvisTheme.spacing.s)
                            ) {
                                DSText(
                                    text = "Edit",
                                    style = DSJarvisTheme.typography.body.small,
                                    color = DSJarvisTheme.colors.neutral.neutral60
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TypeIndicator(
    type: PreferenceType,
    modifier: Modifier = Modifier
) {
    val (color, text) = when (type) {
        PreferenceType.STRING -> Pair(Color(0xFF6366F1), "STR")
        PreferenceType.BOOLEAN -> Pair(Color(0xFF10B981), "BOOL")
        PreferenceType.NUMBER -> Pair(Color(0xFFF59E0B), "NUM")
        PreferenceType.PROTO -> Pair(Color(0xFFEF4444), "PROTO")
    }
    
    Box(
        modifier = modifier
            .background(color, DSJarvisTheme.shapes.xs)
            .padding(horizontal = DSJarvisTheme.dimensions.s, vertical = DSJarvisTheme.dimensions.xs)
    ) {
        DSText(
            text = text,
            style = DSJarvisTheme.typography.body.small,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}