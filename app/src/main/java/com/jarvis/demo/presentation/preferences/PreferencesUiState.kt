package com.jarvis.demo.presentation.preferences

import com.jarvis.core.internal.presentation.state.ResourceState

typealias PreferencesUiState = ResourceState<PreferencesUiData>

/**
 * UiData containing all screen state for PreferencesScreen with 3-tab support
 */
data class PreferencesUiData(
    val sharedPreferences: List<PreferenceItem> = emptyList(),
    val dataStorePreferences: List<PreferenceItem> = emptyList(),
    val protoDataStorePreferences: List<PreferenceItem> = emptyList(),
    val selectedTab: PreferenceStorageType = PreferenceStorageType.SHARED_PREFERENCES,
    val searchQuery: String = "",
    val selectedPreference: PreferenceItem? = null,
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val filterType: PreferenceTypeFilter = PreferenceTypeFilter.ALL,
    val isRefreshing: Boolean = false
) {
    val currentPreferences: List<PreferenceItem>
        get() = when (selectedTab) {
            PreferenceStorageType.SHARED_PREFERENCES -> sharedPreferences
            PreferenceStorageType.PREFERENCES_DATASTORE -> dataStorePreferences
            PreferenceStorageType.PROTO_DATASTORE -> protoDataStorePreferences
        }
    
    val filteredPreferences: List<PreferenceItem>
        get() = currentPreferences.filter { preference ->
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                preference.key.contains(searchQuery, ignoreCase = true) ||
                preference.value.contains(searchQuery, ignoreCase = true)
            }
            
            val matchesType = when (filterType) {
                PreferenceTypeFilter.ALL -> true
                PreferenceTypeFilter.STRING -> preference.type == PreferenceType.STRING
                PreferenceTypeFilter.BOOLEAN -> preference.type == PreferenceType.BOOLEAN
                PreferenceTypeFilter.NUMBER -> preference.type == PreferenceType.NUMBER
                PreferenceTypeFilter.PROTO -> preference.type == PreferenceType.PROTO
            }
            
            matchesSearch && matchesType
        }
    
    companion object {
        val mockPreferencesUiData = PreferencesUiData(
            sharedPreferences = listOf(
                PreferenceItem("user_name", "JohnDoe123", PreferenceType.STRING),
                PreferenceItem("is_notifications_enabled_large_text", "true", PreferenceType.BOOLEAN),
                PreferenceItem("theme_mode", "dark", PreferenceType.STRING),
                PreferenceItem("max_cache_size", "256", PreferenceType.NUMBER),
                PreferenceItem("api_timeout", "15000", PreferenceType.NUMBER),
                PreferenceItem("is_analytics_enabled", "false", PreferenceType.BOOLEAN),
                PreferenceItem("language", "en", PreferenceType.STRING),
                PreferenceItem("auto_sync", "true", PreferenceType.BOOLEAN)
            ).sortedBy { it.key },
            dataStorePreferences = listOf(
                PreferenceItem("app_version", "1.0.0", PreferenceType.STRING),
                PreferenceItem("first_launch", "false", PreferenceType.BOOLEAN),
                PreferenceItem("session_count", "42", PreferenceType.NUMBER),
                PreferenceItem("last_backup_date", "2024-12-07", PreferenceType.STRING),
                PreferenceItem("enable_crash_reports", "true", PreferenceType.BOOLEAN),
                PreferenceItem("data_usage_limit", "1024", PreferenceType.NUMBER),
                PreferenceItem("preferred_quality", "hd", PreferenceType.STRING),
                PreferenceItem("auto_download", "false", PreferenceType.BOOLEAN)
            ).sortedBy { it.key },
            protoDataStorePreferences = listOf(
                PreferenceItem("user_profile.proto", "Binary Data (524 bytes)", PreferenceType.PROTO),
                PreferenceItem("app_settings.proto", "Binary Data (1.2 KB)", PreferenceType.PROTO),
                PreferenceItem("cache_metadata.proto", "Binary Data (256 bytes)", PreferenceType.PROTO),
                PreferenceItem("sync_state.proto", "Binary Data (892 bytes)", PreferenceType.PROTO)
            ).sortedBy { it.key }
        )
    }
}

data class PreferenceItem(
    val key: String,
    val value: String,
    val type: PreferenceType
)

enum class PreferenceType {
    STRING, BOOLEAN, NUMBER, PROTO
}

enum class PreferenceStorageType {
    SHARED_PREFERENCES, PREFERENCES_DATASTORE, PROTO_DATASTORE
}

enum class PreferenceTypeFilter {
    ALL, STRING, BOOLEAN, NUMBER, PROTO
}

/**
 * Events for PreferencesScreen user interactions
 */
sealed interface PreferencesEvent {
    data class SelectTab(val storageType: PreferenceStorageType) : PreferencesEvent
    data class SearchQueryChanged(val query: String) : PreferencesEvent
    data class FilterTypeChanged(val filterType: PreferenceTypeFilter) : PreferencesEvent
    data class PreferenceSelected(val preference: PreferenceItem) : PreferencesEvent
    data class UpdatePreference(val key: String, val value: String, val type: PreferenceType) : PreferencesEvent
    data class DeletePreference(val key: String) : PreferencesEvent
    data class ShowAddDialog(val show: Boolean) : PreferencesEvent
    data class ShowEditDialog(val show: Boolean) : PreferencesEvent
    data class ShowDeleteConfirmation(val show: Boolean) : PreferencesEvent
    
    object LoadPreferences : PreferencesEvent
    object RefreshPreferences : PreferencesEvent
    object ClearAllPreferences : PreferencesEvent
    object GenerateRandomPreferences : PreferencesEvent
    object ClearError : PreferencesEvent
}