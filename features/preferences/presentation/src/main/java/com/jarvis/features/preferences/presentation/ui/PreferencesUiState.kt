package com.jarvis.features.preferences.presentation.ui

import com.jarvis.core.presentation.state.ResourceState
import com.jarvis.features.preferences.domain.entity.AppPreference
import com.jarvis.features.preferences.domain.entity.PreferenceFilter
import com.jarvis.features.preferences.domain.entity.PreferenceGroup
import com.jarvis.features.preferences.domain.entity.PreferenceStorageType
import com.jarvis.features.preferences.domain.entity.PreferenceType

typealias PreferencesUiState = ResourceState<PreferencesUiData>

data class PreferencesUiData(
    val sharedPreferencesGroup: PreferenceGroup = PreferenceGroup(PreferenceStorageType.SHARED_PREFERENCES),
    val dataStorePreferencesGroup: PreferenceGroup = PreferenceGroup(PreferenceStorageType.PREFERENCES_DATASTORE),
    val protoDataStoreGroup: PreferenceGroup = PreferenceGroup(PreferenceStorageType.PROTO_DATASTORE),
    val selectedTab: PreferenceStorageType = PreferenceStorageType.SHARED_PREFERENCES,
    val filter: PreferenceFilter = PreferenceFilter(),
    val selectedPreference: AppPreference? = null,
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showClearAllDialog: Boolean = false,
    val showExportDialog: Boolean = false,
    val showImportDialog: Boolean = false,
    val showDetailDialog: Boolean = false,
    val exportData: String? = null,
    val importError: String? = null,
    val globalError: String? = null,
    val isRefreshing: Boolean = false
) {
    val currentGroup: PreferenceGroup
        get() = when (selectedTab) {
            PreferenceStorageType.SHARED_PREFERENCES -> sharedPreferencesGroup
            PreferenceStorageType.PREFERENCES_DATASTORE -> dataStorePreferencesGroup
            PreferenceStorageType.PROTO_DATASTORE -> protoDataStoreGroup
        }
    
    val filteredPreferences: List<AppPreference>
        get() = currentGroup.preferences.filter { preference ->
            val matchesSearch = if (filter.searchQuery.isBlank()) {
                true
            } else {
                preference.key.contains(filter.searchQuery, ignoreCase = true) ||
                preference.displayName.contains(filter.searchQuery, ignoreCase = true) ||
                preference.value.toString().contains(filter.searchQuery, ignoreCase = true)
            }
            
            val matchesType = filter.typeFilter?.let { it == preference.type } ?: true
            
            val matchesSystemFilter = if (filter.showSystemPreferences) {
                true
            } else {
                !preference.isSystemPreference
            }
            
            matchesSearch && matchesType && matchesSystemFilter
        }

    companion object {
        val mockPreferencesInspectorUiData: PreferencesUiData
            get() = PreferencesUiData(
                sharedPreferencesGroup = PreferenceGroup(
                    storageType = PreferenceStorageType.SHARED_PREFERENCES,
                    preferences = listOf(
                        AppPreference(
                            key = "user_name",
                            value = "John Doe",
                            type = PreferenceType.STRING,
                            storageType = PreferenceStorageType.SHARED_PREFERENCES,
                            displayName = "User Name",
                            description = "The current user's display name",
                            isSystemPreference = false
                        ),
                        AppPreference(
                            key = "is_first_launch",
                            value = false,
                            type = PreferenceType.BOOLEAN,
                            storageType = PreferenceStorageType.SHARED_PREFERENCES,
                            displayName = "Is First Launch",
                            description = "Indicates if this is the first app launch",
                            isSystemPreference = false
                        ),
                        AppPreference(
                            key = "notification_count",
                            value = 42,
                            type = PreferenceType.INTEGER,
                            storageType = PreferenceStorageType.SHARED_PREFERENCES,
                            displayName = "Notification Count",
                            description = "Number of unread notifications",
                            isSystemPreference = false
                        ),
                        AppPreference(
                            key = "app_version",
                            value = "1.0.0",
                            type = PreferenceType.STRING,
                            storageType = PreferenceStorageType.SHARED_PREFERENCES,
                            displayName = "App Version",
                            description = "Current application version",
                            isSystemPreference = true
                        )
                    )
                ),
                dataStorePreferencesGroup = PreferenceGroup(
                    storageType = PreferenceStorageType.PREFERENCES_DATASTORE,
                    preferences = listOf(
                        AppPreference(
                            key = "user_score",
                            value = 98.5f,
                            type = PreferenceType.FLOAT,
                            storageType = PreferenceStorageType.PREFERENCES_DATASTORE,
                            displayName = "User Score",
                            description = "User's current score in the app",
                            isSystemPreference = false
                        ),
                        AppPreference(
                            key = "selected_themes",
                            value = setOf("dark", "blue", "minimal"),
                            type = PreferenceType.STRING_SET,
                            storageType = PreferenceStorageType.PREFERENCES_DATASTORE,
                            displayName = "Selected Themes",
                            description = "User's preferred UI themes",
                            isSystemPreference = false
                        )
                    )
                ),
                filter = PreferenceFilter(
                    searchQuery = "",
                    typeFilter = null,
                    showSystemPreferences = true
                ),
                selectedTab = PreferenceStorageType.SHARED_PREFERENCES
            )
    }
}

sealed interface PreferencesEvent {
    data class ChangeStorageType(val storageType: PreferenceStorageType) : PreferencesEvent
    data class UpdateSearchQuery(val query: String) : PreferencesEvent
    data class UpdateTypeFilter(val type: PreferenceType?) : PreferencesEvent
    data class UpdateSystemPreferencesVisibility(val show: Boolean) : PreferencesEvent
    data class SelectPreference(val preference: AppPreference) : PreferencesEvent
    data class UpdatePreference(val preference: AppPreference, val newValue: Any) : PreferencesEvent
    data class DeletePreference(val preference: AppPreference) : PreferencesEvent
    data class AddPreference(val key: String, val value: Any, val type: PreferenceType, val storageType: PreferenceStorageType) : PreferencesEvent
    data class ClearPreferences(val storageType: PreferenceStorageType) : PreferencesEvent
    data class ExportPreferences(val storageType: PreferenceStorageType?) : PreferencesEvent
    data class ImportPreferences(val data: String, val targetStorageType: PreferenceStorageType) : PreferencesEvent
    data class ShowAddDialog(val show: Boolean) : PreferencesEvent
    data class ShowEditDialog(val show: Boolean) : PreferencesEvent
    data class ShowDeleteDialog(val show: Boolean) : PreferencesEvent
    data class ShowClearAllDialog(val show: Boolean) : PreferencesEvent
    data class ShowDetailDialog(val show: Boolean) : PreferencesEvent
    data class ShowExportDialog(val show: Boolean) : PreferencesEvent
    data class ShowImportDialog(val show: Boolean) : PreferencesEvent
    object LoadAllPreferences : PreferencesEvent
    object RefreshCurrentTab : PreferencesEvent
    object RefreshPreferences : PreferencesEvent
    object ClearError : PreferencesEvent
}