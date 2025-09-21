package com.jarvis.features.preferences.domain.entity

data class AppPreference(
    val key: String,
    val value: Any,
    val type: PreferenceType,
    val storageType: PreferenceStorageType,
    val displayName: String = key,
    val description: String? = null,
    val isSystemPreference: Boolean = false,
    val isEditable: Boolean = true,
    val filePath: String? = null
)

enum class PreferenceType {
    STRING,
    BOOLEAN,
    INTEGER,
    LONG,
    FLOAT,
    DOUBLE,
    STRING_SET,
    BYTES,
    PROTO_MESSAGE
}

enum class PreferenceStorageType {
    SHARED_PREFERENCES,
    PREFERENCES_DATASTORE,
    PROTO_DATASTORE
}

data class PreferenceFilter(
    val searchQuery: String = "",
    val typeFilter: PreferenceType? = null,
    val storageTypeFilter: PreferenceStorageType? = null,
    val showSystemPreferences: Boolean = true
)

data class PreferenceGroup(
    val storageType: PreferenceStorageType,
    val preferences: List<AppPreference> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)