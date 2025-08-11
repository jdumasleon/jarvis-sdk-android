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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jarvis.core.designsystem.component.DSButton
import com.jarvis.core.designsystem.component.DSButtonStyle
import com.jarvis.core.designsystem.component.DSCard
import com.jarvis.core.designsystem.component.DSFilterChip
import com.jarvis.core.designsystem.component.DSIconButton
import com.jarvis.core.designsystem.component.DSSearchBar
import com.jarvis.core.designsystem.component.DSSwitch
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.presentation.components.ResourceStateContent
import com.jarvis.core.presentation.state.ResourceState
import com.jarvis.features.preferences.domain.entity.AppPreference
import com.jarvis.features.preferences.domain.entity.PreferenceFilter
import com.jarvis.features.preferences.domain.entity.PreferenceStorageType
import com.jarvis.features.preferences.domain.entity.PreferenceType

@Composable
fun PreferencesInspectorRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PreferencesInspectorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    PreferencesInspectorScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PreferencesInspectorScreen(
    uiState: PreferencesInspectorUiState,
    onEvent: (PreferencesInspectorEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { DSText("Preferences Inspector") },
                navigationIcon = {
                    DSIconButton(
                        onClick = onNavigateBack,
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                },
                actions = {
                    DSIconButton(
                        onClick = { onEvent(PreferencesInspectorEvent.ClearPreferences(PreferenceStorageType.SHARED_PREFERENCES)) },
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear All"
                    )
                    DSIconButton(
                        onClick = { /* TODO: Toggle search visibility */ },
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ResourceStateContent(
                resourceState = uiState,
                onRetry = { onEvent(PreferencesInspectorEvent.LoadAllPreferences) },
                onDismiss = { onEvent(PreferencesInspectorEvent.ClearError) },
                loadingMessage = "Loading preferences...",
                emptyMessage = "No preferences found",
                emptyActionText = "Load Preferences",
                onEmptyAction = { onEvent(PreferencesInspectorEvent.LoadAllPreferences) }
            ) { uiData ->
                PreferencesInspectorContent(
                    uiData = uiData,
                    onEvent = onEvent
                )
            }
        }
    }
}

@Composable
private fun PreferencesInspectorContent(
    uiData: PreferencesInspectorUiData,
    onEvent: (PreferencesInspectorEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search and Filters
        SearchAndFilters(
            filter = uiData.filter,
            onSearchQueryChange = { onEvent(PreferencesInspectorEvent.UpdateSearchQuery(it)) },
            onTypeFilterChange = { onEvent(PreferencesInspectorEvent.UpdateTypeFilter(it)) },
            onSystemPreferencesToggle = { onEvent(PreferencesInspectorEvent.UpdateSystemPreferencesVisibility(it)) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Action Buttons
        ActionButtons(
            onAddClick = { onEvent(PreferencesInspectorEvent.ShowAddDialog(true)) },
            onExportClick = { onEvent(PreferencesInspectorEvent.ExportPreferences(uiData.selectedTab)) },
            onImportClick = { onEvent(PreferencesInspectorEvent.ShowImportDialog(true)) },
            onClearAllClick = { onEvent(PreferencesInspectorEvent.ClearPreferences(uiData.selectedTab)) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Preferences List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiData.filteredPreferences) { preference ->
                PreferenceItem(
                    preference = preference,
                    onClick = { onEvent(PreferencesInspectorEvent.SelectPreference(preference)) },
                    onEditClick = { 
                        onEvent(PreferencesInspectorEvent.SelectPreference(preference))
                        onEvent(PreferencesInspectorEvent.ShowEditDialog(true))
                    },
                    onDeleteClick = { 
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
    filter: PreferenceFilter,
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
            DSFilterChip(
                selected = filter.typeFilter == null,
                onClick = { onTypeFilterChange(null) },
                label = "All"
            )
            
            PreferenceType.entries.forEach { type ->
                DSFilterChip(
                    selected = filter.typeFilter == type,
                    onClick = { onTypeFilterChange(if (filter.typeFilter == type) null else type) },
                    label = type.name
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
            style = DSButtonStyle.OUTLINE,
            modifier = Modifier.weight(1f)
        )
        DSButton(
            onClick = onExportClick,
            text = "Export",
            style = DSButtonStyle.OUTLINE,
            modifier = Modifier.weight(1f)
        )
        DSButton(
            onClick = onImportClick,
            text = "Import",
            style = DSButtonStyle.OUTLINE,
            modifier = Modifier.weight(1f)
        )
        DSButton(
            onClick = onClearAllClick,
            text = "Clear All",
            style = DSButtonStyle.SECONDARY,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PreferenceItem(
    preference: AppPreference,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    DSCard(
        modifier = modifier.fillMaxWidth()
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
                        style = DSJarvisTheme.typography.heading.heading5,
                        fontWeight = FontWeight.Medium
                    )
                    
                    DSText(
                        text = preference.key,
                        style = DSJarvisTheme.typography.body.small,
                        color = DSJarvisTheme.colors.neutral.neutral60
                    )
                    
                    preference.description?.let { description ->
                        DSText(
                            text = description,
                            style = DSJarvisTheme.typography.body.small,
                            color = DSJarvisTheme.colors.neutral.neutral60
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    DSText(
                        text = preference.type.name,
                        style = DSJarvisTheme.typography.body.extraSmall,
                        color = DSJarvisTheme.colors.primary.primary100
                    )
                    
                    if (preference.isSystemPreference) {
                        DSText(
                            text = "SYSTEM",
                            style = DSJarvisTheme.typography.body.extraSmall,
                            color = DSJarvisTheme.colors.neutral.neutral60
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            DSText(
                text = preference.value.toString(),
                style = DSJarvisTheme.typography.body.medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = DSJarvisTheme.colors.neutral.neutral80
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DSButton(
                    onClick = onEditClick,
                    text = "Edit",
                    style = DSButtonStyle.OUTLINE,
                    disabled = preference.isSystemPreference
                )
                
                DSButton(
                    onClick = onDeleteClick,
                    text = "Delete",
                    style = DSButtonStyle.SECONDARY,
                    disabled = preference.isSystemPreference
                )
            }
        }
    }
}

// Comprehensive Preview Templates
@Preview(showBackground = true, name = "Loading State")
@Composable
private fun PreferencesInspectorScreenLoadingPreview() {
    DSJarvisTheme {
        PreferencesInspectorScreen(
            uiState = ResourceState.Loading,
            onEvent = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Idle State")
@Composable
private fun PreferencesInspectorScreenIdlePreview() {
    DSJarvisTheme {
        PreferencesInspectorScreen(
            uiState = ResourceState.Idle,
            onEvent = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Success - With Preferences")
@Composable
private fun PreferencesInspectorScreenSuccessPreview() {
    DSJarvisTheme {
        PreferencesInspectorScreen(
            uiState = ResourceState.Success(PreferencesInspectorUiData.mockPreferencesInspectorUiData),
            onEvent = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Success - Empty State")
@Composable
private fun PreferencesInspectorScreenEmptyPreview() {
    DSJarvisTheme {
        PreferencesInspectorScreen(
            uiState = ResourceState.Success(PreferencesInspectorUiData()),
            onEvent = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Error State")
@Composable
private fun PreferencesInspectorScreenErrorPreview() {
    DSJarvisTheme {
        PreferencesInspectorScreen(
            uiState = ResourceState.Error(
                exception = Exception("Database connection failed"),
                message = "Failed to load preferences data"
            ),
            onEvent = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Dark Theme - Success", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreferencesInspectorScreenDarkThemePreview() {
    DSJarvisTheme {
        PreferencesInspectorScreen(
            uiState = ResourceState.Success(PreferencesInspectorUiData.mockPreferencesInspectorUiData),
            onEvent = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Single Preference Item")
@Composable
private fun PreferenceItemPreview() {
    DSJarvisTheme {
        PreferenceItem(
            preference = AppPreference(
                key = "user_name",
                value = "John William Doe",
                type = PreferenceType.STRING,
                storageType = PreferenceStorageType.SHARED_PREFERENCES,
                displayName = "User Display Name",
                description = "The current user's full display name for the application",
                isSystemPreference = false
            ),
            onClick = {},
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true, name = "System Preference Item")
@Composable
private fun SystemPreferenceItemPreview() {
    DSJarvisTheme {
        PreferenceItem(
            preference = AppPreference(
                key = "app_version",
                value = "1.0.0-beta",
                type = PreferenceType.STRING,
                storageType = PreferenceStorageType.SHARED_PREFERENCES,
                displayName = "Application Version",
                description = "Current version of the application - managed by system",
                isSystemPreference = true
            ),
            onClick = {},
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}