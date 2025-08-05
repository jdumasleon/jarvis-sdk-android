package com.jarvis.features.preferences.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jarvis.core.designsystem.component.DSButton
import com.jarvis.core.designsystem.component.DSSearchBar
import com.jarvis.core.designsystem.component.DSSwitch
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.features.preferences.domain.entity.AppPreference
import com.jarvis.features.preferences.domain.entity.PreferenceType

@Composable
fun PreferencesInspectorScreen(
    modifier: Modifier = Modifier,
    viewModel: PreferencesInspectorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    PreferencesInspectorContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@Composable
private fun PreferencesInspectorContent(
    uiState: PreferencesInspectorUiState,
    onEvent: (PreferencesInspectorEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        DSText(
            text = "Preferences Inspector",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Search and Filters
        SearchAndFilters(
            filter = uiState.filter,
            onSearchQueryChange = { onEvent(PreferencesInspectorEvent.UpdateSearchQuery(it)) },
            onTypeFilterChange = { onEvent(PreferencesInspectorEvent.UpdateTypeFilter(it)) },
            onSystemPreferencesToggle = { onEvent(PreferencesInspectorEvent.UpdateSystemPreferencesVisibility(it)) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Action Buttons
        ActionButtons(
            onAddClick = { onEvent(PreferencesInspectorEvent.ShowAddDialog(true)) },
            onExportClick = { onEvent(PreferencesInspectorEvent.ExportPreferences(uiState.selectedTab)) },
            onImportClick = { onEvent(PreferencesInspectorEvent.ShowImportDialog(true)) },
            onClearAllClick = { onEvent(PreferencesInspectorEvent.ClearPreferences(uiState.selectedTab)) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Content
        when {
            uiState.currentGroup.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.currentGroup.error != null -> {
                ErrorMessage(
                    error = uiState.currentGroup.error!!,
                    onDismiss = { onEvent(PreferencesInspectorEvent.ClearError) }
                )
            }
            else -> {
                PreferencesList(
                    preferences = uiState.filteredPreferences,
                    onPreferenceClick = { onEvent(PreferencesInspectorEvent.SelectPreference(it)) },
                    onEditClick = { preference ->
                        onEvent(PreferencesInspectorEvent.SelectPreference(preference))
                        onEvent(PreferencesInspectorEvent.ShowEditDialog(true))
                    },
                    onDeleteClick = { preference ->
                        onEvent(PreferencesInspectorEvent.SelectPreference(preference))
                        onEvent(PreferencesInspectorEvent.ShowDeleteDialog(true))
                    }
                )
            }
        }
    }
}

@Composable
private fun SearchAndFilters(
    filter: com.jarvis.features.preferences.domain.entity.PreferenceFilter,
    onSearchQueryChange: (String) -> Unit,
    onTypeFilterChange: (PreferenceType?) -> Unit,
    onSystemPreferencesToggle: (Boolean) -> Unit
) {
    Column {
        // Search Bar
        DSSearchBar(
            searchText = filter.searchQuery,
            onValueChange = onSearchQueryChange,
            onTextClean = { onSearchQueryChange("") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Type Filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filter.typeFilter == null,
                onClick = { onTypeFilterChange(null) },
                label = { Text("All") }
            )
            
            PreferenceType.values().forEach { type ->
                FilterChip(
                    selected = filter.typeFilter == type,
                    onClick = { onTypeFilterChange(if (filter.typeFilter == type) null else type) },
                    label = { Text(type.name) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // System Preferences Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DSText(text = "Show System Preferences")
            Spacer(modifier = Modifier.weight(1f))
            DSSwitch(
                checked = filter.showSystemPreferences,
                onCheckedChange = onSystemPreferencesToggle
            )
        }
    }
}

@Composable
private fun ActionButtons(
    onAddClick: () -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onClearAllClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DSButton(
            onClick = onAddClick,
            text = "Add",
            modifier = Modifier.weight(1f)
        )
        DSButton(
            onClick = onExportClick,
            text = "Export",
            modifier = Modifier.weight(1f)
        )
        DSButton(
            onClick = onImportClick,
            text = "Import",
            modifier = Modifier.weight(1f)
        )
        DSButton(
            onClick = onClearAllClick,
            text = "Clear All",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PreferencesList(
    preferences: List<AppPreference>,
    onPreferenceClick: (AppPreference) -> Unit,
    onEditClick: (AppPreference) -> Unit,
    onDeleteClick: (AppPreference) -> Unit
) {
    if (preferences.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            DSText(
                text = "No preferences found",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(preferences) { preference ->
                PreferenceItem(
                    preference = preference,
                    onClick = { onPreferenceClick(preference) },
                    onEditClick = { onEditClick(preference) },
                    onDeleteClick = { onDeleteClick(preference) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PreferenceItem(
    preference: AppPreference,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (preference.isSystemPreference) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    DSText(
                        text = preference.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    DSText(
                        text = preference.key,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    preference.description?.let { description ->
                        DSText(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    DSText(
                        text = preference.type.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    if (preference.isSystemPreference) {
                        DSText(
                            text = "SYSTEM",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            DSText(
                text = preference.value.toString(),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEditClick,
                    enabled = !preference.isSystemPreference
                ) {
                    Text("Edit")
                }
                
                OutlinedButton(
                    onClick = onDeleteClick,
                    enabled = !preference.isSystemPreference
                ) {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun ErrorMessage(
    error: String,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            DSText(
                text = "Error",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            DSText(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            DSButton(
                onClick = onDismiss,
                text = "Dismiss"
            )
        }
    }
}