package com.jarvis.demo.koin.data.preferences

import com.jarvis.demo.koin.presentation.preferences.PreferenceItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine


class DemoPreferencesRepository (
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val preferencesDataStoreManager: PreferencesDataStoreManager,
    private val protoDataStoreManager: ProtoDataStoreManager
) {

    fun getAllPreferencesFlow(): Flow<List<PreferenceItem>> {
        return combine(
            sharedPreferencesManager.getAllPreferencesFlow(),
            preferencesDataStoreManager.getAllPreferences(),
            protoDataStoreManager.getAllPreferencesFlow()
        ) { sharedPrefs, dataStorePrefs, protoPrefs ->
            buildList<PreferenceItem> {
                addAll(sharedPrefs)
                addAll(dataStorePrefs)
                addAll(protoPrefs)
            }.sortedBy { it.key }
        }
    }

    fun getSharedPreferencesFlow(): Flow<List<PreferenceItem>> {
        return sharedPreferencesManager.getAllPreferencesFlow()
    }

    fun getDataStorePreferencesFlow(): Flow<List<PreferenceItem>> {
        return preferencesDataStoreManager.getAllPreferences()
    }

    fun getProtoDataStorePreferencesFlow(): Flow<List<PreferenceItem>> {
        return protoDataStoreManager.getAllPreferencesFlow()
    }

    suspend fun generateAllSampleData() {
        // Only generate sample data if all stores are empty
        if (sharedPreferencesManager.isEmpty()) {
            sharedPreferencesManager.generateSampleSharedPreferences()
        }
        
        if (preferencesDataStoreManager.isEmpty()) {
            preferencesDataStoreManager.generateSamplePreferences()
        }
        
        if (protoDataStoreManager.isEmpty()) {
            protoDataStoreManager.generateSampleProtoPreferences()
        }
    }

    suspend fun clearAllPreferences() {
        sharedPreferencesManager.clearAllPreferences()
        preferencesDataStoreManager.clearAll()
        protoDataStoreManager.clearAll()
    }

    suspend fun clearSharedPreferences() {
        sharedPreferencesManager.clearAllPreferences()
    }

    suspend fun clearDataStorePreferences() {
        preferencesDataStoreManager.clearAll()
    }

    suspend fun clearProtoDataStorePreferences() {
        protoDataStoreManager.clearAll()
    }

    suspend fun isAllEmpty(): Boolean {
        return sharedPreferencesManager.isEmpty() && 
               preferencesDataStoreManager.isEmpty() &&
               protoDataStoreManager.isEmpty()
    }

    // Convenience methods for updating specific preferences
    suspend fun updateSharedPreference(file: String, key: String, value: String) {
        sharedPreferencesManager.putString(file, key, value)
    }

    suspend fun updateSharedPreference(file: String, key: String, value: Boolean) {
        sharedPreferencesManager.putBoolean(file, key, value)
    }

    suspend fun updateSharedPreference(file: String, key: String, value: Int) {
        sharedPreferencesManager.putInt(file, key, value)
    }

    suspend fun updateDataStorePreference(key: String, value: String) {
        preferencesDataStoreManager.putString(key, value)
    }

    suspend fun updateDataStorePreference(key: String, value: Boolean) {
        preferencesDataStoreManager.putBoolean(key, value)
    }

    suspend fun updateDataStorePreference(key: String, value: Int) {
        preferencesDataStoreManager.putInt(key, value)
    }

    suspend fun updateDataStorePreference(key: String, value: Float) {
        preferencesDataStoreManager.putFloat(key, value)
    }

    suspend fun updateProtoUsername(username: String) {
        protoDataStoreManager.updateUsername(username)
    }

    suspend fun updateProtoTheme(theme: String) {
        protoDataStoreManager.updateThemePreference(theme)
    }

    suspend fun updateProtoAnalytics(enabled: Boolean) {
        protoDataStoreManager.updateAnalyticsEnabled(enabled)
    }
}