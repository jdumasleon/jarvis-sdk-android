package com.jarvis.demo.domain.usecase.preferences

import com.jarvis.demo.data.preferences.DemoPreferencesRepository
import com.jarvis.demo.presentation.preferences.PreferenceStorageType
import com.jarvis.demo.presentation.preferences.PreferenceType
import javax.inject.Inject

class ManagePreferencesUseCase @Inject constructor(
    private val repository: DemoPreferencesRepository
) {

    suspend fun generateAllSampleData() {
        repository.generateAllSampleData()
    }

    suspend fun clearAllPreferences() {
        repository.clearAllPreferences()
    }

    suspend fun clearSharedPreferences() {
        repository.clearSharedPreferences()
    }

    suspend fun clearDataStorePreferences() {
        repository.clearDataStorePreferences()
    }

    suspend fun clearProtoDataStorePreferences() {
        repository.clearProtoDataStorePreferences()
    }

    suspend fun updateDataStorePreference(key: String, value: String) {
        repository.updateDataStorePreference(key, value)
    }

    suspend fun updateDataStorePreference(key: String, value: Boolean) {
        repository.updateDataStorePreference(key, value)
    }

    suspend fun updateDataStorePreference(key: String, value: Int) {
        repository.updateDataStorePreference(key, value)
    }

    suspend fun updateDataStorePreference(key: String, value: Float) {
        repository.updateDataStorePreference(key, value)
    }

    suspend fun updateProtoUsername(username: String) {
        repository.updateProtoUsername(username)
    }

    suspend fun updateProtoTheme(theme: String) {
        repository.updateProtoTheme(theme)
    }

    suspend fun updateProtoAnalytics(enabled: Boolean) {
        repository.updateProtoAnalytics(enabled)
    }
}