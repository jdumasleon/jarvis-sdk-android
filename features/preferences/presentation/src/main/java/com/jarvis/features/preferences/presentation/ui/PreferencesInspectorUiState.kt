package com.jarvis.features.preferences.presentation.ui

import com.jarvis.features.preferences.domain.entity.AppPreference
import com.jarvis.features.preferences.domain.entity.PreferenceFilter
import com.jarvis.features.preferences.domain.entity.PreferenceGroup
import com.jarvis.features.preferences.domain.entity.PreferenceStorageType
import com.jarvis.features.preferences.domain.entity.PreferenceType

data class PreferencesInspectorUiState(
    val sharedPreferencesGroup: PreferenceGroup = PreferenceGroup(PreferenceStorageType.SHARED_PREFERENCES),
    val dataStorePreferencesGroup: PreferenceGroup = PreferenceGroup(PreferenceStorageType.PREFERENCES_DATASTORE),
    val protoDataStoreGroup: PreferenceGroup = PreferenceGroup(PreferenceStorageType.PROTO_DATASTORE),
    val selectedTab: PreferenceStorageType = PreferenceStorageType.SHARED_PREFERENCES,
    val filter: PreferenceFilter = PreferenceFilter(),
    val selectedPreference: AppPreference? = null,
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showExportDialog: Boolean = false,
    val showImportDialog: Boolean = false,
    val showDetailDialog: Boolean = false,
    val exportData: String? = null,
    val importError: String? = null,
    val globalError: String? = null
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
}

sealed interface PreferencesInspectorEvent {
    data class SelectTab(val storageType: PreferenceStorageType) : PreferencesInspectorEvent
    data class UpdateSearchQuery(val query: String) : PreferencesInspectorEvent
    data class UpdateTypeFilter(val type: PreferenceType?) : PreferencesInspectorEvent
    data class UpdateSystemPreferencesVisibility(val show: Boolean) : PreferencesInspectorEvent
    data class SelectPreference(val preference: AppPreference) : PreferencesInspectorEvent
    data class UpdatePreference(val preference: AppPreference, val newValue: Any) : PreferencesInspectorEvent
    data class DeletePreference(val preference: AppPreference) : PreferencesInspectorEvent
    data class AddPreference(val key: String, val value: Any, val type: PreferenceType, val storageType: PreferenceStorageType) : PreferencesInspectorEvent
    data class ClearPreferences(val storageType: PreferenceStorageType) : PreferencesInspectorEvent
    data class ExportPreferences(val storageType: PreferenceStorageType?) : PreferencesInspectorEvent
    data class ImportPreferences(val data: String, val targetStorageType: PreferenceStorageType) : PreferencesInspectorEvent
    data class ShowAddDialog(val show: Boolean) : PreferencesInspectorEvent
    data class ShowEditDialog(val show: Boolean) : PreferencesInspectorEvent
    data class ShowDeleteDialog(val show: Boolean) : PreferencesInspectorEvent
    data class ShowDetailDialog(val show: Boolean) : PreferencesInspectorEvent
    data class ShowExportDialog(val show: Boolean) : PreferencesInspectorEvent
    data class ShowImportDialog(val show: Boolean) : PreferencesInspectorEvent
    object LoadAllPreferences : PreferencesInspectorEvent
    object RefreshCurrentTab : PreferencesInspectorEvent
    object ClearError : PreferencesInspectorEvent
}