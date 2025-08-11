package com.jarvis.features.preferences.presentation.ui

import android.content.ClipData
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.jarvis.core.designsystem.component.DSButton
import com.jarvis.core.designsystem.component.DSButtonSize
import com.jarvis.core.designsystem.component.DSButtonStyle
import com.jarvis.core.designsystem.component.DSCard
import com.jarvis.core.designsystem.component.DSDropdownMenuItem
import com.jarvis.core.designsystem.component.DSFilterChip
import com.jarvis.core.designsystem.component.DSIconButton
import com.jarvis.core.designsystem.component.DSPullToRefresh
import com.jarvis.core.designsystem.component.DSSearchBar
import com.jarvis.core.designsystem.component.DSSwitch
import com.jarvis.core.designsystem.component.DSTabBar
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.component.DSThreeDotsMenu
import com.jarvis.core.designsystem.icons.DSIcons
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.presentation.components.EmptyContent
import com.jarvis.core.presentation.components.ResourceStateContent
import com.jarvis.core.presentation.state.ResourceState
import com.jarvis.features.preferences.domain.entity.AppPreference
import com.jarvis.features.preferences.domain.entity.PreferenceFilter
import com.jarvis.features.preferences.domain.entity.PreferenceStorageType
import com.jarvis.features.preferences.domain.entity.PreferenceType
import kotlinx.coroutines.launch

@Composable
fun PreferencesRoute(
    modifier: Modifier = Modifier,
    viewModel: PreferencesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    PreferencesScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PreferencesScreen(
    uiState: PreferencesUiState,
    onEvent: (PreferencesEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        ResourceStateContent(
            resourceState = uiState,
            onRetry = { onEvent(PreferencesEvent.LoadAllPreferences) },
            onDismiss = { onEvent(PreferencesEvent.ClearError) },
            loadingMessage = "Loading preferences...",
            emptyMessage = "No preferences found",
            emptyActionText = "Load Preferences",
            onEmptyAction = { onEvent(PreferencesEvent.LoadAllPreferences) }
        ) { uiData ->
            PreferencesContent(
                uiData = uiData,
                onEvent = onEvent
            )
        }
    }
}

@Composable
private fun PreferencesContent(
    uiData: PreferencesUiData,
    onEvent: (PreferencesEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    DSPullToRefresh(
        isRefreshing = uiData.isRefreshing,
        onRefresh = { onEvent(PreferencesEvent.RefreshPreferences) }
    ) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.dimensions.s)
        ) {
            Spacer(modifier = Modifier.height(DSJarvisTheme.dimensions.s))

            // Storage Type Tabs
            StorageTypeTabs(
                selectedTab = uiData.selectedTab,
                onTabSelected = { onEvent(PreferencesEvent.SelectTab(it)) }
            )

            // Search and Filters
            SearchAndFilters(
                filter = uiData.filter,
                onSearchQueryChange = { onEvent(PreferencesEvent.UpdateSearchQuery(it)) },
                onTypeFilterChange = { onEvent(PreferencesEvent.UpdateTypeFilter(it)) }
            )

            PreferencesActions(
                uiData = uiData,
                onEvent = onEvent
            )
            
            // Preferences List
            if (uiData.filteredPreferences.isEmpty()) {
                EmptyContent(
                    message = "No ${uiData.selectedTab.name} preferences recorded yet",
                    actionText = "Refresh",
                    onAction = { onEvent(PreferencesEvent.RefreshCurrentTab) }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
                ) {
                    item { Spacer(Modifier.height(DSJarvisTheme.spacing.m)) }

                    items(uiData.filteredPreferences) { preference ->
                        PreferenceItem(
                            modifier = Modifier.padding(horizontal = DSJarvisTheme.spacing.m),
                            preference = preference,
                            onClick = { onEvent(PreferencesEvent.SelectPreference(preference)) },
                            onEditClick = {
                                onEvent(PreferencesEvent.SelectPreference(preference))
                                onEvent(PreferencesEvent.ShowEditDialog(true))
                            },
                            onDeleteClick = {
                                onEvent(PreferencesEvent.SelectPreference(preference))
                                onEvent(PreferencesEvent.ShowDeleteDialog(true))
                            }
                        )
                    }

                    item { Spacer(Modifier.height(DSJarvisTheme.spacing.m)) }
                }
            }
        }
    }
}

@Composable
private fun PreferencesActions(
    onEvent: (PreferencesEvent) -> Unit,
    uiData: PreferencesUiData
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DSJarvisTheme.spacing.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DSText(
            text = "Preferences",
            style = DSJarvisTheme.typography.heading.heading5,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        DSThreeDotsMenu(
            items = buildList {
                add(
                    DSDropdownMenuItem(
                        text = "Add",
                        icon = Icons.Default.Add,
                        onClick = { onEvent(PreferencesEvent.ShowAddDialog(true)) }
                    )
                )
                add(
                    DSDropdownMenuItem(
                        text = "Export",
                        icon = Icons.Default.ArrowCircleDown,
                        onClick = { onEvent(PreferencesEvent.ExportPreferences(uiData.selectedTab)) }
                    )
                )
                add(
                    DSDropdownMenuItem(
                        text = "Import",
                        icon = Icons.Default.ArrowCircleUp,
                        onClick = { onEvent(PreferencesEvent.ShowImportDialog(true)) }
                    )
                )
                add(
                    DSDropdownMenuItem(
                        text = "Clear All",
                        icon = Icons.Default.CleaningServices,
                        onClick = { onEvent(PreferencesEvent.ClearPreferences(uiData.selectedTab)) }
                    )
                )
                add(
                    DSDropdownMenuItem(
                        text = "Show System Preferences",
                        icon = if (uiData.filter.showSystemPreferences) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        onClick = { onEvent(PreferencesEvent.UpdateSystemPreferencesVisibility(uiData.filter.showSystemPreferences)) }
                    )
                )
            }
        )
    }
}

@Composable
private fun StorageTypeTabs(
    selectedTab: PreferenceStorageType,
    onTabSelected: (PreferenceStorageType) -> Unit
) {
    val storageTypes = listOf(
        PreferenceStorageType.SHARED_PREFERENCES,
        PreferenceStorageType.PREFERENCES_DATASTORE,
        PreferenceStorageType.PROTO_DATASTORE
    )
    
    val tabLabels = listOf("SharedPrefs", "DataStore", "Proto")

    DSTabBar(
        selectedTabIndex = storageTypes.indexOf(selectedTab),
        tabCount = tabLabels.size,
        onTabSelected = { index ->
            onTabSelected(storageTypes[index])
        },
        backgroundColor = DSJarvisTheme.colors.extra.surface
    ) { index, selected ->
        Box (
            modifier = Modifier.padding(DSJarvisTheme.spacing.m)
        ) {
            DSText(
                text = tabLabels[index],
                style = DSJarvisTheme.typography.body.medium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) {
                    DSJarvisTheme.colors.primary.primary100
                } else {
                    DSJarvisTheme.colors.neutral.neutral100
                }
            )
        }
    }
}

@Composable
private fun SearchAndFilters(
    filter: PreferenceFilter,
    onSearchQueryChange: (String) -> Unit,
    onTypeFilterChange: (PreferenceType?) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m)
    ) {
        // Search Bar
        DSSearchBar(
            searchText = filter.searchQuery,
            onValueChange = onSearchQueryChange,
            onTextClean = { onSearchQueryChange("") },
            modifier = Modifier
                .padding(horizontal = DSJarvisTheme.spacing.m)
                .fillMaxWidth()
        )
        
        // Type Filters
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = DSJarvisTheme.spacing.m),
            horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
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
        modifier = modifier
            .clickable(onClick = onClick)
            .fillMaxWidth(),
        shape = DSJarvisTheme.shapes.m,
        elevation = DSJarvisTheme.elevations.level2,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m),
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DSText(
                    text = preference.value.toString(),
                    style = DSJarvisTheme.typography.body.medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = DSJarvisTheme.colors.neutral.neutral80
                )

                Spacer(modifier = Modifier.weight(1f))

                DSIconButton(
                    onClick = onEditClick,
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = DSJarvisTheme.colors.primary.primary100
                )

                DSIconButton(
                    onClick = onDeleteClick,
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = DSJarvisTheme.colors.error.error100
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Success - With Preferences")
@Composable
private fun PreferencesInspectorScreenSuccessPreview() {
    DSJarvisTheme {
        PreferencesScreen(
            uiState = ResourceState.Success(PreferencesUiData.mockPreferencesInspectorUiData),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Success - Empty State")
@Composable
private fun PreferencesInspectorScreenEmptyPreview() {
    DSJarvisTheme {
        PreferencesScreen(
            uiState = ResourceState.Success(PreferencesUiData()),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
private fun PreferencesInspectorScreenLoadingPreview() {
    DSJarvisTheme {
        PreferencesScreen(
            uiState = ResourceState.Loading,
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Idle State")
@Composable
private fun PreferencesInspectorScreenIdlePreview() {
    DSJarvisTheme {
        PreferencesScreen(
            uiState = ResourceState.Idle,
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Error State")
@Composable
private fun PreferencesInspectorScreenErrorPreview() {
    DSJarvisTheme {
        PreferencesScreen(
            uiState = ResourceState.Error(
                exception = Exception("Database connection failed"),
                message = "Failed to load preferences data"
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Dark Theme - Success", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreferencesInspectorScreenDarkThemePreview() {
    DSJarvisTheme {
        PreferencesScreen(
            uiState = ResourceState.Success(PreferencesUiData.mockPreferencesInspectorUiData),
            onEvent = {}
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