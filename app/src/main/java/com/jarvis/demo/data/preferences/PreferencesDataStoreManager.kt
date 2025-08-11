package com.jarvis.demo.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.jarvis.demo.presentation.preferences.PreferenceItem
import com.jarvis.demo.presentation.preferences.PreferenceType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "demo_preferences")

@Singleton
class PreferencesDataStore @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    
    fun getAllPreferences(): Flow<List<PreferenceItem>> {
        return context.dataStore.data.map { preferences ->
            preferences.asMap().map { (key, value) ->
                PreferenceItem(
                    key = key.name,
                    value = value.toString(),
                    type = when (value) {
                        is Boolean -> PreferenceType.BOOLEAN
                        is Int, is Long, is Float, is Double -> PreferenceType.NUMBER
                        else -> PreferenceType.STRING
                    }
                )
            }.sortedBy { it.key }
        }
    }
    
    suspend fun putString(key: String, value: String) {
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey(key)] = value
        }
    }
    
    suspend fun putBoolean(key: String, value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey(key)] = value
        }
    }
    
    suspend fun putInt(key: String, value: Int) {
        context.dataStore.edit { preferences ->
            preferences[intPreferencesKey(key)] = value
        }
    }
    
    suspend fun putLong(key: String, value: Long) {
        context.dataStore.edit { preferences ->
            preferences[longPreferencesKey(key)] = value
        }
    }
    
    suspend fun putFloat(key: String, value: Float) {
        context.dataStore.edit { preferences ->
            preferences[floatPreferencesKey(key)] = value
        }
    }
    
    suspend fun putDouble(key: String, value: Double) {
        context.dataStore.edit { preferences ->
            preferences[doublePreferencesKey(key)] = value
        }
    }
    
    suspend fun updatePreference(key: String, value: String, type: PreferenceType) {
        context.dataStore.edit { preferences ->
            when (type) {
                PreferenceType.STRING -> preferences[stringPreferencesKey(key)] = value
                PreferenceType.BOOLEAN -> preferences[booleanPreferencesKey(key)] = value.toBoolean()
                PreferenceType.NUMBER -> {
                    when {
                        value.contains(".") -> preferences[floatPreferencesKey(key)] = value.toFloat()
                        else -> preferences[intPreferencesKey(key)] = value.toInt()
                    }
                }
                PreferenceType.PROTO -> {
                    // Proto preferences are not supported in Preferences DataStore
                    // This is a placeholder for consistency
                }
            }
        }
    }
    
    suspend fun removePreference(key: String) {
        context.dataStore.edit { preferences ->
            val keysToRemove = preferences.asMap().keys.filter { it.name == key }
            keysToRemove.forEach { keyToRemove ->
                preferences.remove(keyToRemove)
            }
        }
    }
    
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    suspend fun isEmpty(): Boolean {
        return context.dataStore.data.first().asMap().isEmpty()
    }
    
    suspend fun generateSamplePreferences() {
        val samplePreferences = mapOf(
            "user_name" to "JohnDoe${(100..999).random()}",
            "is_notifications_enabled" to listOf(true, false).random(),
            "theme_mode" to listOf("light", "dark", "auto").random(),
            "max_cache_size" to (50..500).random(),
            "api_timeout" to (5000..30000).random(),
            "is_analytics_enabled" to listOf(true, false).random(),
            "language" to listOf("en", "es", "fr", "de").random(),
            "auto_sync" to listOf(true, false).random(),
            "cache_expiry_hours" to (1..48).random(),
            "is_debug_mode" to listOf(true, false).random(),
            "server_url" to "https://api.example${(1..5).random()}.com",
            "retry_attempts" to (1..5).random(),
            "is_wifi_only" to listOf(true, false).random(),
            "version_code" to (100..200).random(),
            "last_sync_timestamp" to System.currentTimeMillis(),
            "refresh_rate" to kotlin.random.Random.nextDouble(0.5, 5.0).toFloat(),
            "is_first_launch" to false,
            "user_rating" to kotlin.random.Random.nextDouble(1.0, 5.0).toFloat(),
            "device_id" to "device_${(10000..99999).random()}",
            "is_premium_user" to listOf(true, false).random()
        )
        
        context.dataStore.edit { preferences ->
            samplePreferences.forEach { (key, value) ->
                when (value) {
                    is String -> preferences[stringPreferencesKey(key)] = value
                    is Boolean -> preferences[booleanPreferencesKey(key)] = value
                    is Int -> preferences[intPreferencesKey(key)] = value
                    is Long -> preferences[longPreferencesKey(key)] = value
                    is Float -> preferences[floatPreferencesKey(key)] = value
                    is Double -> preferences[doublePreferencesKey(key)] = value
                }
            }
        }
    }
}