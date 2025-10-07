package com.jarvis.demo.koin.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.jarvis.demo.koin.presentation.preferences.PreferenceItem
import com.jarvis.demo.koin.presentation.preferences.PreferenceType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import androidx.core.content.edit


class SharedPreferencesManager (
    context: Context
) {
    companion object {
        private const val SHARED_PREFS_NAME = "demo_shared_prefs"
        private const val USER_PREFS_NAME = "demo_user_prefs" 
        private const val APP_SETTINGS_NAME = "demo_app_settings"
        private const val CACHE_SETTINGS_NAME = "demo_cache_settings"
    }

    private val demoPrefs: SharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    private val userPrefs: SharedPreferences = context.getSharedPreferences(USER_PREFS_NAME, Context.MODE_PRIVATE)
    private val appSettings: SharedPreferences = context.getSharedPreferences(APP_SETTINGS_NAME, Context.MODE_PRIVATE)
    private val cacheSettings: SharedPreferences = context.getSharedPreferences(CACHE_SETTINGS_NAME, Context.MODE_PRIVATE)

    // Flow to notify when preferences change
    private val _preferencesFlow = MutableStateFlow(Unit)
    
    fun getAllPreferencesFlow(): Flow<List<PreferenceItem>> {
        return _preferencesFlow.asStateFlow().map {
            getAllPreferencesInternal()
        }
    }

    private fun getAllPreferencesInternal(): List<PreferenceItem> {
        val preferences = mutableListOf<PreferenceItem>()
        
        // Get preferences from all SharedPreference files
        listOf(
            SHARED_PREFS_NAME to demoPrefs,
            USER_PREFS_NAME to userPrefs,
            APP_SETTINGS_NAME to appSettings,
            CACHE_SETTINGS_NAME to cacheSettings
        ).forEach { (fileName, prefs) ->
            prefs.all.forEach { (key, value) ->
                preferences.add(
                    PreferenceItem(
                        key = "$fileName.$key",
                        value = value?.toString() ?: "null",
                        type = when (value) {
                            is Boolean -> PreferenceType.BOOLEAN
                            is Int, is Long, is Float -> PreferenceType.NUMBER
                            else -> PreferenceType.STRING
                        }
                    )
                )
            }
        }
        
        return preferences.sortedBy { it.key }
    }

    fun putString(file: String, key: String, value: String) {
        val prefs = getPreferencesByFile(file)
        prefs.edit { putString(key, value) }
        _preferencesFlow.value = Unit
    }

    fun putBoolean(file: String, key: String, value: Boolean) {
        val prefs = getPreferencesByFile(file)
        prefs.edit { putBoolean(key, value) }
        _preferencesFlow.value = Unit
    }

    fun putInt(file: String, key: String, value: Int) {
        val prefs = getPreferencesByFile(file)
        prefs.edit { putInt(key, value) }
        _preferencesFlow.value = Unit
    }

    fun putFloat(file: String, key: String, value: Float) {
        val prefs = getPreferencesByFile(file)
        prefs.edit { putFloat(key, value) }
        _preferencesFlow.value = Unit
    }

    fun putLong(file: String, key: String, value: Long) {
        val prefs = getPreferencesByFile(file)
        prefs.edit { putLong(key, value) }
        _preferencesFlow.value = Unit
    }

    fun removePreference(file: String, key: String) {
        val prefs = getPreferencesByFile(file)
        prefs.edit { remove(key) }
        _preferencesFlow.value = Unit
    }

    fun clearAllPreferences(file: String? = null) {
        if (file != null) {
            getPreferencesByFile(file).edit { clear() }
        } else {
            // Clear all files
            demoPrefs.edit { clear() }
            userPrefs.edit { clear() }
            appSettings.edit { clear() }
            cacheSettings.edit { clear() }
        }
        _preferencesFlow.value = Unit
    }

    private fun getPreferencesByFile(file: String): SharedPreferences {
        return when (file) {
            SHARED_PREFS_NAME -> demoPrefs
            USER_PREFS_NAME -> userPrefs
            APP_SETTINGS_NAME -> appSettings
            CACHE_SETTINGS_NAME -> cacheSettings
            else -> demoPrefs // Default fallback
        }
    }

    fun generateSampleSharedPreferences() {
        // Demo shared preferences
        demoPrefs.edit {
            putString("app_version", "1.0.0")
            putBoolean("debug_mode", true)
            putString("environment", "development")
            putInt("build_number", 42)
            putString("api_base_url", "https://api.demo.com")
        }

        // User preferences
        userPrefs.edit {
            putString("username", "demo_user_${(100..999).random()}")
            putBoolean("is_premium", false)
            putString("preferred_language", "en")
            putInt("login_count", (1..50).random())
            putBoolean("email_notifications", true)
            putBoolean("push_notifications", true)
            putString("theme_preference", listOf("light", "dark", "auto").random())
        }

        // App settings
        appSettings.edit {
            putBoolean("auto_sync", true)
            putInt("sync_interval_minutes", 30)
            putFloat("font_size_multiplier", 1.2f)
            putBoolean("analytics_enabled", false)
            putString("default_screen", "home")
            putInt("session_timeout_minutes", 15)
        }

        // Cache settings  
        cacheSettings.edit {
            putInt("max_cache_size_mb", 128)
            putLong("cache_expiry_hours", 24)
            putBoolean("cache_wifi_only", false)
            putInt("image_cache_size_mb", 50)
            putBoolean("preload_images", true)
        }

        _preferencesFlow.value = Unit
    }

    fun isEmpty(): Boolean {
        return demoPrefs.all.isEmpty() && 
               userPrefs.all.isEmpty() && 
               appSettings.all.isEmpty() && 
               cacheSettings.all.isEmpty()
    }
}