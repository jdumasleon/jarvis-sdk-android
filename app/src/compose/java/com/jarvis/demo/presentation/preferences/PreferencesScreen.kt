package com.jarvis.demo.presentation.preferences

import androidx.compose.foundation.background
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
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.jarvis.core.designsystem.component.DSCard
import com.jarvis.core.designsystem.component.DSFilterChip
import com.jarvis.core.designsystem.component.DSFlag
import com.jarvis.core.designsystem.component.DSPullToRefresh
import com.jarvis.core.designsystem.component.DSSearchBar
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.component.DSTextField
import com.jarvis.core.designsystem.component.FlagStyle
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.navigation.ActionRegistry
import com.jarvis.core.presentation.components.ResourceStateContent
import com.jarvis.core.presentation.state.ResourceState
import com.jarvis.demo.R
import com.jarvis.demo.presentation.inspector.InspectorEvent
import com.jarvis.demo.presentation.inspector.InspectorGraph

@Composable
fun PreferencesScreen(
    modifier: Modifier = Modifier,
    viewModel: PreferencesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Register the action callback when the screen is composed
    DisposableEffect(viewModel) {
        ActionRegistry.registerAction(PreferencesGraph.Preferences.actionKey) {
            viewModel.onEvent(PreferencesEvent.GenerateRandomPreferences)
        }
        onDispose {
            ActionRegistry.unregisterAction(PreferencesGraph.Preferences.actionKey)
        }
    }

    PreferencesScreen(
        modifier = modifier,
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun PreferencesScreen(
    uiState: PreferencesUiState,
    onEvent: (PreferencesEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
    ) {
        ResourceStateContent(
            resourceState = uiState,
            modifier = Modifier.weight(1f),
            onRetry = { onEvent(PreferencesEvent.RefreshPreferences) },
            onDismiss = { onEvent(PreferencesEvent.ClearError) },
            loadingMessage = "Loading preferences...",
            emptyMessage = "No preferences found",
            emptyActionText = "Reload",
            onEmptyAction = { onEvent(PreferencesEvent.RefreshPreferences) }
        ) { uiData ->
            PreferencesContent(
                data = uiData,
                onEvent = onEvent
            )
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
                .padding(horizontal = DSJarvisTheme.spacing.m)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(DSJarvisTheme.dimensions.m))

        // Preferences storage type
        DSText(
            text = stringResource(R.string.storages),
            style = DSJarvisTheme.typography.body.medium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = DSJarvisTheme.spacing.m)
        )

        Spacer(modifier = Modifier.height(DSJarvisTheme.dimensions.s))

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = DSJarvisTheme.spacing.m),
            horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
        ) {
            PreferenceStorageType.entries.forEach { preferencesType ->
                val selected = data.selectedTab == preferencesType
                val preference = getPreferenceTypeName(preferencesType.ordinal)
                val label = if (selected) "$preference (${data.filteredPreferences.size})" else preference
                DSFilterChip(
                    onClick = {
                        onEvent(PreferencesEvent.SelectTab(preferencesType))
                    },
                    label = label.uppercase(),
                    selected = selected
                )
            }
        }

        Spacer(modifier = Modifier.height(DSJarvisTheme.dimensions.s))
        
        // Preferences List with Pull-to-Refresh
        DSPullToRefresh(
            isRefreshing = data.isRefreshing,
            onRefresh = { onEvent(PreferencesEvent.RefreshPreferences) }
        ) {
            if (data.filteredPreferences.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
                ) {
                    item { Spacer(Modifier.height(DSJarvisTheme.spacing.m)) }

                    item { TabContentHeader(storageType = data.selectedTab) }

                    items(data.filteredPreferences) { preference ->
                        PreferenceItem(
                            modifier = Modifier.padding(horizontal = DSJarvisTheme.spacing.m),
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

                    item { Spacer(Modifier.height(DSJarvisTheme.spacing.m)) }
                }
            }
        }
    }
}

@Composable
private fun getPreferenceTypeName(index: Int): String = when (PreferenceStorageType.entries[index]) {
    PreferenceStorageType.SHARED_PREFERENCES -> "Shared"
    PreferenceStorageType.PREFERENCES_DATASTORE -> "Datastore"
    PreferenceStorageType.PROTO_DATASTORE -> "Proto"
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

    DSFlag(
        title = title,
        description = description,
        style = FlagStyle.Info,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DSJarvisTheme.spacing.m)
            .padding(bottom = DSJarvisTheme.spacing.s)
    )
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .padding(DSJarvisTheme.spacing.m)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DSText(
            text = stringResource(R.string.no_preferences_found),
            style = DSJarvisTheme.typography.body.medium,
            color = DSJarvisTheme.colors.neutral.neutral100
        )
        
        DSText(
            text = stringResource(R.string.try_adjusting_search),
            style = DSJarvisTheme.typography.body.small,
            color = DSJarvisTheme.colors.neutral.neutral100
        )
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
        modifier = modifier.fillMaxWidth(),
        shape = DSJarvisTheme.shapes.m,
        elevation = DSJarvisTheme.elevations.level2
    ) {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.s)
        ) {
            // Key and Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    DSText(
                        modifier = Modifier.padding(end = DSJarvisTheme.spacing.xs),
                        text = preference.key,
                        style = DSJarvisTheme.typography.body.medium,
                        fontWeight = FontWeight.Medium
                    )

                    DSText(
                        text = preference.type.name.lowercase(),
                        style = DSJarvisTheme.typography.body.small,
                        color = DSJarvisTheme.colors.neutral.neutral80
                    )
                }
                
                // Type indicator
                TypeIndicator(type = preference.type)
            }
            
            Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.xs))
            
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
                        color = DSJarvisTheme.colors.neutral.neutral100,
                        fontWeight = FontWeight.Light
                    )
                    
                    DSText(
                        text = stringResource(R.string.read_only),
                        style = DSJarvisTheme.typography.body.small,
                        color = DSJarvisTheme.colors.neutral.neutral100
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
                                        DSJarvisTheme.colors.primary.primary80,
                                        DSJarvisTheme.shapes.xs
                                    )
                                    .clickable {
                                        onValueChanged(editableValue)
                                        isEditing = false
                                    }
                                    .padding(DSJarvisTheme.spacing.s)
                            ) {
                                DSText(
                                    text = stringResource(R.string.save),
                                    style = DSJarvisTheme.typography.body.small,
                                    color = Color.White
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(DSJarvisTheme.spacing.xs))
                            
                            // Cancel button
                            Box(
                                modifier = Modifier
                                    .background(
                                        DSJarvisTheme.colors.neutral.neutral40,
                                        DSJarvisTheme.shapes.xs
                                    )
                                    .clickable {
                                        editableValue = preference.value
                                        isEditing = false
                                    }
                                    .padding(DSJarvisTheme.spacing.s)
                            ) {
                                DSText(
                                    text = stringResource(R.string.cancel),
                                    style = DSJarvisTheme.typography.body.small,
                                    color = DSJarvisTheme.colors.neutral.neutral100
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
                                    text = stringResource(R.string.edit),
                                    style = DSJarvisTheme.typography.body.small,
                                    color = DSJarvisTheme.colors.neutral.neutral100
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

@Preview(showBackground = true, name = "Loading - Initial State")
@Composable
fun PreferencesScreenLoadingPreview() {
    DSJarvisTheme {
        PreferencesScreen(
            uiState = ResourceState.Loading,
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Idle State")
@Composable
fun PreferencesScreenIdlePreview() {
    DSJarvisTheme {
        PreferencesScreen(
            uiState = ResourceState.Idle,
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Error State")
@Composable
fun PreferencesScreenErrorPreview() {
    DSJarvisTheme {
        PreferencesScreen(
            uiState = ResourceState.Error(
                RuntimeException("Server error"),
                "Unable to load preferences"
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Empty Preferences")
@Composable
fun PreferencesScreenEmptyPreview() {
    DSJarvisTheme {
        PreferencesScreen(
            uiState = ResourceState.Success(
                PreferencesUiData.mockPreferencesUiData.copy(
                    sharedPreferences = emptyList(),
                    dataStorePreferences = emptyList(),
                    protoDataStorePreferences = emptyList()
                )
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "With Data")
@Composable
fun PreferencesScreenWithDataPreview() {
    DSJarvisTheme {
        PreferencesScreen(
            uiState = ResourceState.Success(PreferencesUiData.mockPreferencesUiData),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Dark Theme", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreferencesScreenDarkThemePreview() {
    DSJarvisTheme {
        PreferencesScreen(
            uiState = ResourceState.Success(PreferencesUiData.mockPreferencesUiData),
            onEvent = {}
        )
    }
}