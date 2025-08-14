package com.jarvis.features.preferences.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Velocity
import androidx.hilt.navigation.compose.hiltViewModel
import com.jarvis.core.designsystem.component.DSBottomSheet
import com.jarvis.core.designsystem.component.DSButton
import com.jarvis.core.designsystem.component.DSButtonSize
import com.jarvis.core.designsystem.component.DSButtonStyle
import com.jarvis.core.designsystem.component.DSCard
import com.jarvis.core.designsystem.component.DSDialog
import com.jarvis.core.designsystem.component.DSDropdownMenu
import com.jarvis.core.designsystem.component.DSDropdownMenuItem
import com.jarvis.core.designsystem.component.DSFilterChip
import com.jarvis.core.designsystem.component.DSIconButton
import com.jarvis.core.designsystem.component.DSPullToRefresh
import com.jarvis.core.designsystem.component.DSSearchBar
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.component.DSTextField
import com.jarvis.core.designsystem.component.DSThreeDotsMenu
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.presentation.components.EmptyContent
import com.jarvis.core.presentation.components.ResourceStateContent
import com.jarvis.core.presentation.state.ResourceState
import com.jarvis.features.preferences.domain.entity.AppPreference
import com.jarvis.features.preferences.domain.entity.PreferenceFilter
import com.jarvis.features.preferences.domain.entity.PreferenceStorageType
import com.jarvis.features.preferences.domain.entity.PreferenceType

/**
 * Preferences screen route with state management
 */
@Composable
fun PreferencesRoute(
    modifier: Modifier = Modifier,
    viewModel: PreferencesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    PreferencesScreenWithDialogs(
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
        modifier = modifier
            .padding(top = DSJarvisTheme.spacing.m)
            .fillMaxSize()
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
    val listState = rememberLazyListState()
    var isFiltersVisible by rememberSaveable { mutableStateOf(true) }
    var filtersHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    // Optimized nested scroll connection with debouncing
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            private val threshold = 8f
            private var accumulatedDelta = 0f

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput && filtersHeightPx > 0) {
                    val dy = available.y
                    accumulatedDelta += dy

                    // Only trigger state change when threshold is exceeded
                    when {
                        accumulatedDelta < -threshold && isFiltersVisible -> {
                            isFiltersVisible = false
                            accumulatedDelta = 0f
                        }

                        accumulatedDelta > threshold && !isFiltersVisible -> {
                            isFiltersVisible = true
                            accumulatedDelta = 0f
                        }
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (filtersHeightPx > 0) {
                    when {
                        available.y < -200 && isFiltersVisible -> isFiltersVisible = false
                        available.y > 200 && !isFiltersVisible -> isFiltersVisible = true
                    }
                }
                accumulatedDelta = 0f
                return Velocity.Zero
            }
        }
    }

    // Animated progress with optimized spring animation
    val filtersProgress by animateFloatAsState(
        targetValue = if (isFiltersVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 250,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "filtersProgress"
    )

    // Calculate animated offset
    val filtersOffsetPx = remember(filtersProgress, filtersHeightPx) {
        -filtersHeightPx * (1f - filtersProgress)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        // Animated Filters Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationY = filtersOffsetPx
                    alpha = filtersProgress.coerceIn(0f, 1f)
                }
                .onGloballyPositioned { coordinates ->
                    val newHeight = coordinates.size.height
                    if (newHeight != filtersHeightPx && newHeight > 0) {
                        filtersHeightPx = newHeight
                    }
                }
        ) {
            SearchAndFilters(
                uiData = uiData,
                filter = uiData.filter,
                onSearchQueryChange = { onEvent(PreferencesEvent.UpdateSearchQuery(it)) },
                onTypeFilterChange = { onEvent(PreferencesEvent.UpdateTypeFilter(it)) },
                onStorageTypeChange = { onEvent(PreferencesEvent.ChangeStorageType(it)) }
            )
        }

        // Content below filters with dynamic height adjustment
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = with(density) {
                        (filtersHeightPx * filtersProgress).toInt().toDp()
                    }
                )
        ) {
            PreferencesActions(
                uiData = uiData,
                onEvent = onEvent
            )

            DSPullToRefresh(
                isRefreshing = uiData.isRefreshing,
                onRefresh = { onEvent(PreferencesEvent.RefreshPreferences) }
            ) {
                if (uiData.filteredPreferences.isEmpty()) {
                    EmptyContent(
                        message = "No ${uiData.selectedTab.name} preferences recorded yet",
                        actionText = "Refresh",
                        onAction = { onEvent(PreferencesEvent.RefreshCurrentTab) },
                        modifier = Modifier.padding(horizontal = DSJarvisTheme.spacing.m)
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = DSJarvisTheme.spacing.m),
                        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
                    ) {
                        items(uiData.filteredPreferences) { preference ->
                            PreferenceItem(
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

                        item { Spacer(Modifier.height(DSJarvisTheme.spacing.l)) }
                    }
                }
            }
        }
    }
}

@Composable
internal fun PreferencesScreenWithDialogs(
    uiState: PreferencesUiState,
    onEvent: (PreferencesEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    PreferencesScreen(uiState, onEvent, modifier)
    
    // Add Preference Bottom Sheet
    if (uiState is ResourceState.Success && uiState.data.showAddDialog) {
        AddPreferenceBottomSheet(
            selectedStorageType = uiState.data.selectedTab,
            onSave = { key, value, type ->
                onEvent(PreferencesEvent.AddPreference(key, value, type, uiState.data.selectedTab))
                onEvent(PreferencesEvent.ShowAddDialog(false))
            },
            onDismiss = { onEvent(PreferencesEvent.ShowAddDialog(false)) }
        )
    }
    
    // Edit Preference Bottom Sheet
    if (uiState is ResourceState.Success && uiState.data.showEditDialog && uiState.data.selectedPreference != null) {
        EditPreferenceBottomSheet(
            preference = uiState.data.selectedPreference!!,
            onSave = { newValue ->
                onEvent(PreferencesEvent.UpdatePreference(uiState.data.selectedPreference!!, newValue))
                onEvent(PreferencesEvent.ShowEditDialog(false))
            },
            onDismiss = { onEvent(PreferencesEvent.ShowEditDialog(false)) }
        )
    }
    
    // Delete Preference Dialog
    if (uiState is ResourceState.Success && uiState.data.showDeleteDialog && uiState.data.selectedPreference != null) {
        DeletePreferenceDialog(
            preference = uiState.data.selectedPreference!!,
            onConfirm = {
                onEvent(PreferencesEvent.DeletePreference(uiState.data.selectedPreference!!))
                onEvent(PreferencesEvent.ShowDeleteDialog(false))
            },
            onDismiss = { onEvent(PreferencesEvent.ShowDeleteDialog(false)) }
        )
    }
    
    // Clear All Confirmation Dialog
    if (uiState is ResourceState.Success && uiState.data.showClearAllDialog) {
        ClearAllPreferencesDialog(
            storageType = uiState.data.selectedTab,
            onConfirm = {
                onEvent(PreferencesEvent.ClearPreferences(uiState.data.selectedTab))
                onEvent(PreferencesEvent.ShowClearAllDialog(false))
            },
            onDismiss = { onEvent(PreferencesEvent.ShowClearAllDialog(false)) }
        )
    }
}

@Composable
private fun SearchAndFilters(
    uiData: PreferencesUiData,
    filter: PreferenceFilter,
    onSearchQueryChange: (String) -> Unit,
    onTypeFilterChange: (PreferenceType?) -> Unit,
    onStorageTypeChange: (PreferenceStorageType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m)
    ) {
        DSSearchBar(
            searchText = filter.searchQuery,
            onValueChange = onSearchQueryChange,
            onTextClean = { onSearchQueryChange("") },
            modifier = Modifier
                .padding(horizontal = DSJarvisTheme.spacing.m)
                .fillMaxWidth()
        )

        PreferencesTypeChips(
            filter = filter,
            onTypeFilterChange = onTypeFilterChange
        )
        
        StorageTypeChips(
            uiData = uiData,
            onTabSelected = { onStorageTypeChange(it) }
        )
    }
}

@Composable
private fun StorageTypeChips(
    uiData: PreferencesUiData,
    onTabSelected: (PreferenceStorageType) -> Unit
) {
    val storageTypes = listOf(
        PreferenceStorageType.SHARED_PREFERENCES,
        PreferenceStorageType.PREFERENCES_DATASTORE,
        PreferenceStorageType.PROTO_DATASTORE
    )

    Column (
        modifier = Modifier.padding(start = DSJarvisTheme.spacing.m)
    ) {
        DSText(
            text = "Storages",
            style = DSJarvisTheme.typography.body.medium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = DSJarvisTheme.spacing.s)
        )

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
        ) {
            storageTypes.forEach { preferencesType ->
                DSFilterChip(
                    onClick = {
                        onTabSelected(preferencesType)
                    },
                    label = getPreferenceTypeName(preferencesType),
                    selected = uiData.selectedTab == preferencesType
                )
            }
        }
    }
}

@Composable
private fun PreferencesTypeChips(
    filter: PreferenceFilter,
    onTypeFilterChange: (PreferenceType?) -> Unit
) {
    Column(
        modifier = Modifier.padding(start = DSJarvisTheme.spacing.m)
    ) {
        DSText(
            text = "Types",
            style = DSJarvisTheme.typography.body.medium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = DSJarvisTheme.spacing.s)
        )

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
        ) {
            DSFilterChip(
                selected = filter.typeFilter == null,
                onClick = { onTypeFilterChange(null) },
                label = "All"
            )

            PreferenceType.entries.forEach { type ->
                val isSelected = filter.typeFilter == type
                DSFilterChip(
                    selected = isSelected,
                    onClick = {
                        val newType = if (isSelected) null else type
                        onTypeFilterChange(newType)
                    },
                    label = type.name
                )
            }
        }
    }
}

@Composable
private fun PreferencesActions(
    uiData: PreferencesUiData,
    onEvent: (PreferencesEvent) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DSJarvisTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DSText(
            text = "${getPreferenceTypeName(uiData.selectedTab)} (${uiData.filteredPreferences.size})",
            style = DSJarvisTheme.typography.body.medium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = DSJarvisTheme.spacing.s)
        )

        DSThreeDotsMenu(
            iconTint = DSJarvisTheme.colors.primary.primary60,
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
                        text = if (uiData.filter.showSystemPreferences) "Hide System" else "Show System",
                        icon = if (uiData.filter.showSystemPreferences) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        onClick = { onEvent(PreferencesEvent.UpdateSystemPreferencesVisibility(!uiData.filter.showSystemPreferences)) }
                    )
                )
                add(
                    DSDropdownMenuItem(
                        text = "Clear All",
                        textColor = DSJarvisTheme.colors.error.error100,
                        icon = Icons.Default.DeleteForever,
                        iconTint = DSJarvisTheme.colors.error.error100,
                        onClick = { onEvent(PreferencesEvent.ShowClearAllDialog(true)) }
                    )
                )
            }
        )
    }
}

@Composable
private fun getPreferenceTypeName(preferenceStorageType: PreferenceStorageType): String = when (preferenceStorageType) {
    PreferenceStorageType.SHARED_PREFERENCES -> "SHARED"
    PreferenceStorageType.PREFERENCES_DATASTORE -> "DATASTORE"
    PreferenceStorageType.PROTO_DATASTORE -> "PROTO"
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
        elevation = DSJarvisTheme.elevations.level1,
    ) {
        Column (
            modifier = Modifier.padding(DSJarvisTheme.spacing.s),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    DSText(
                        text = preference.displayName,
                        style = DSJarvisTheme.typography.body.large,
                        fontWeight = FontWeight.Medium
                    )

                    DSText(
                        text = preference.key,
                        style = DSJarvisTheme.typography.body.medium,
                        color = DSJarvisTheme.colors.neutral.neutral80
                    )

                    preference.description?.let { description ->
                        DSText(
                            text = description,
                            style = DSJarvisTheme.typography.body.small,
                            color = DSJarvisTheme.colors.neutral.neutral60,
                            overflow= TextOverflow.Ellipsis,
                            maxLines = 1
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
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DSText(
                    text = preference.value.toString(),
                    style = DSJarvisTheme.typography.body.medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = DSJarvisTheme.colors.neutral.neutral100
                )

                Spacer(modifier = Modifier.weight(1f))

                DSIconButton(
                    onClick = onEditClick,
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = DSJarvisTheme.colors.primary.primary100
                )

                DSIconButton(
                    modifier = Modifier.padding(DSJarvisTheme.spacing.none),
                    onClick = onDeleteClick,
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = DSJarvisTheme.colors.error.error100
                )
            }
        }
    }
}

@Composable
private fun AddPreferenceBottomSheet(
    selectedStorageType: PreferenceStorageType,
    onSave: (String, Any, PreferenceType) -> Unit,
    onDismiss: () -> Unit
) {
    var key by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(PreferenceType.STRING) }
    var showTypeDropdown by remember { mutableStateOf(false) }
    
    DSBottomSheet(
        onDismissRequest = onDismiss,
        title = { DSText("Add Preference") },
        content = {
            DSText(
                text = "Add new preference to ${getPreferenceTypeName(selectedStorageType)} storage",
                style = DSJarvisTheme.typography.body.small,
                color = DSJarvisTheme.colors.neutral.neutral60
            )
            
            DSTextField(
                text = key,
                onValueChange = { key = it },
                title = "Key",
                placeholder = "preference_key",
                modifier = Modifier.fillMaxWidth()
            )
            
            Box {
                DSButton(
                    text = "Type: ${selectedType.name}",
                    onClick = { showTypeDropdown = true },
                    style = DSButtonStyle.OUTLINE,
                    size = DSButtonSize.SMALL,
                    modifier = Modifier.fillMaxWidth()
                )
                
                DSDropdownMenu(
                    expanded = showTypeDropdown,
                    onDismissRequest = { showTypeDropdown = false },
                    items = PreferenceType.entries.map { type ->
                        DSDropdownMenuItem(
                            text = type.name,
                            onClick = {
                                selectedType = type
                                showTypeDropdown = false
                            }
                        )
                    }
                )
            }
            
            DSTextField(
                text = value,
                onValueChange = { value = it },
                title = "Value",
                placeholder = when (selectedType) {
                    PreferenceType.STRING -> "string value"
                    PreferenceType.INTEGER -> "123"
                    PreferenceType.FLOAT -> "123.45"
                    PreferenceType.BOOLEAN -> "true/false"
                    PreferenceType.LONG -> "123456789"
                    PreferenceType.STRING_SET -> "item1,item2,item3"
                    PreferenceType.DOUBLE -> "123.456789"
                    PreferenceType.BYTES -> "base64encoded"
                    PreferenceType.PROTO_MESSAGE -> "proto message"
                },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            DSButton(
                text = "Add",
                onClick = {
                    if (key.isNotEmpty() && value.isNotEmpty()) {
                        val parsedValue = parseValueForType(value, selectedType)
                        if (parsedValue != null) {
                            onSave(key, parsedValue, selectedType)
                        }
                    }
                },
                style = DSButtonStyle.PRIMARY,
                size = DSButtonSize.MEDIUM,
                modifier = Modifier.fillMaxWidth()
            )
        },
        dismissButton = {
            DSButton(
                text = "Cancel",
                onClick = onDismiss,
                style = DSButtonStyle.SECONDARY,
                size = DSButtonSize.MEDIUM,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

@Composable
private fun EditPreferenceBottomSheet(
    preference: AppPreference,
    onSave: (Any) -> Unit,
    onDismiss: () -> Unit
) {
    var value by remember { mutableStateOf(preference.value.toString()) }
    
    DSBottomSheet(
        onDismissRequest = onDismiss,
        title = { DSText("Edit Preference") },
        content = {
            DSText(
                text = "Key: ${preference.key}",
                style = DSJarvisTheme.typography.body.medium,
                fontWeight = FontWeight.Medium
            )
            
            DSText(
                text = "Type: ${preference.type.name}",
                style = DSJarvisTheme.typography.body.small,
                color = DSJarvisTheme.colors.neutral.neutral60
            )
            
            DSTextField(
                text = value,
                onValueChange = { value = it },
                title = "Value",
                placeholder = "Enter new value",
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            DSButton(
                text = "Save",
                onClick = {
                    if (value.isNotEmpty()) {
                        val parsedValue = parseValueForType(value, preference.type)
                        if (parsedValue != null) {
                            onSave(parsedValue)
                        }
                    }
                },
                style = DSButtonStyle.PRIMARY,
                size = DSButtonSize.MEDIUM,
                modifier = Modifier.fillMaxWidth()
            )
        },
        dismissButton = {
            DSButton(
                text = "Cancel",
                onClick = onDismiss,
                style = DSButtonStyle.SECONDARY,
                size = DSButtonSize.MEDIUM,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

@Composable
private fun DeletePreferenceDialog(
    preference: AppPreference,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    DSDialog(
        onDismissRequest = onDismiss,
        title = { DSText("Delete Preference") },
        text = {
            DSText("Are you sure you want to delete the preference \"${preference.key}\"? This action cannot be undone.")
        },
        confirmButton = {
            DSButton(
                text = "Delete",
                onClick = onConfirm,
                style = DSButtonStyle.PRIMARY,
                size = DSButtonSize.SMALL,
                modifier = Modifier.fillMaxWidth()
            )
        },
        dismissButton = {
            DSButton(
                text = "Cancel",
                onClick = onDismiss,
                style = DSButtonStyle.SECONDARY,
                size = DSButtonSize.SMALL,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

@Composable
private fun ClearAllPreferencesDialog(
    storageType: PreferenceStorageType,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    DSDialog(
        onDismissRequest = onDismiss,
        title = { DSText("Clear All Preferences") },
        text = {
            DSText("Are you sure you want to clear all preferences in ${getPreferenceTypeName(storageType)} storage? This action cannot be undone and will delete all preference data.")
        },
        confirmButton = {
            DSButton(
                text = "Clear All",
                onClick = onConfirm,
                style = DSButtonStyle.PRIMARY,
                size = DSButtonSize.MEDIUM,
                modifier = Modifier.fillMaxWidth()
            )
        },
        dismissButton = {
            DSButton(
                text = "Cancel",
                onClick = onDismiss,
                style = DSButtonStyle.SECONDARY,
                size = DSButtonSize.MEDIUM,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

private fun parseValueForType(value: String, type: PreferenceType): Any? {
    return try {
        when (type) {
            PreferenceType.STRING -> value
            PreferenceType.INTEGER -> value.toInt()
            PreferenceType.FLOAT -> value.toFloat()
            PreferenceType.BOOLEAN -> value.toBooleanStrict()
            PreferenceType.LONG -> value.toLong()
            PreferenceType.STRING_SET -> value.split(",").map { it.trim() }.toSet()
            PreferenceType.DOUBLE -> value.toDouble()
            PreferenceType.BYTES -> value.encodeToByteArray()
            PreferenceType.PROTO_MESSAGE -> value // For proto messages, we'll accept string representation
        }
    } catch (e: Exception) {
        null
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