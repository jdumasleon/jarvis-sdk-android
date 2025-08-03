package com.jarvis.demo.presentation.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class PreferencesViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val preferences: SharedPreferences = context.getSharedPreferences("demo_preferences", Context.MODE_PRIVATE)
    
    private val _uiState = MutableStateFlow(PreferencesUiState())
    val uiState: StateFlow<PreferencesUiState> = _uiState.asStateFlow()
    
    private val _preferences = MutableStateFlow<List<PreferenceItem>>(emptyList())
    val preferencesList: StateFlow<List<PreferenceItem>> = _preferences.asStateFlow()
    
    init {
        loadPreferences()
    }
    
    private fun loadPreferences() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val existingPrefs = preferences.all
            
            if (existingPrefs.isEmpty()) {
                // Generate random preferences for demo
                generateRandomPreferences()
            } else {
                // Load existing preferences
                val prefItems = existingPrefs.map { (key, value) ->
                    PreferenceItem(
                        key = key,
                        value = value.toString(),
                        type = when (value) {
                            is Boolean -> PreferenceType.BOOLEAN
                            is Int, is Long, is Float, is Double -> PreferenceType.NUMBER
                            else -> PreferenceType.STRING
                        }
                    )
                }.sortedBy { it.key }
                
                _preferences.value = prefItems
            }
            
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
    
    private fun generateRandomPreferences() {
        val samplePreferences = mapOf(
            "user_name" to "JohnDoe${Random.nextInt(100, 999)}",
            "is_notifications_enabled" to Random.nextBoolean(),
            "theme_mode" to listOf("light", "dark", "auto").random(),
            "max_cache_size" to Random.nextInt(50, 500),
            "api_timeout" to Random.nextInt(5000, 30000),
            "is_analytics_enabled" to Random.nextBoolean(),
            "language" to listOf("en", "es", "fr", "de").random(),
            "auto_sync" to Random.nextBoolean(),
            "cache_expiry_hours" to Random.nextInt(1, 48),
            "is_debug_mode" to Random.nextBoolean(),
            "server_url" to "https://api.example${Random.nextInt(1, 5)}.com",
            "retry_attempts" to Random.nextInt(1, 5),
            "is_wifi_only" to Random.nextBoolean(),
            "version_code" to Random.nextInt(100, 200),
            "last_sync_timestamp" to System.currentTimeMillis(),
            "refresh_rate" to Random.nextDouble(0.5, 5.0),
            "is_first_launch" to false,
            "user_rating" to Random.nextDouble(1.0, 5.0),
            "device_id" to "device_${Random.nextInt(10000, 99999)}",
            "is_premium_user" to Random.nextBoolean()
        )
        
        val editor = preferences.edit()
        samplePreferences.forEach { (key, value) ->
            when (value) {
                is String -> editor.putString(key, value)
                is Boolean -> editor.putBoolean(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is Float -> editor.putFloat(key, value)
                is Double -> editor.putFloat(key, value.toFloat())
            }
        }
        editor.apply()
        
        // Convert to preference items
        val prefItems = samplePreferences.map { (key, value) ->
            PreferenceItem(
                key = key,
                value = value.toString(),
                type = when (value) {
                    is Boolean -> PreferenceType.BOOLEAN
                    is Int, is Long, is Float, is Double -> PreferenceType.NUMBER
                    else -> PreferenceType.STRING
                }
            )
        }.sortedBy { it.key }
        
        _preferences.value = prefItems
    }
    
    fun refreshPreferences() {
        loadPreferences()
    }
    
    fun clearAllPreferences() {
        viewModelScope.launch {
            preferences.edit().clear().apply()
            _preferences.value = emptyList()
        }
    }
    
    fun updatePreference(key: String, value: String, type: PreferenceType) {
        viewModelScope.launch {
            val editor = preferences.edit()
            
            try {
                when (type) {
                    PreferenceType.STRING -> editor.putString(key, value)
                    PreferenceType.BOOLEAN -> editor.putBoolean(key, value.toBoolean())
                    PreferenceType.NUMBER -> {
                        when {
                            value.contains(".") -> editor.putFloat(key, value.toFloat())
                            else -> editor.putInt(key, value.toInt())
                        }
                    }
                }
                editor.apply()
                
                // Update the list
                val updatedList = _preferences.value.map { pref ->
                    if (pref.key == key) {
                        pref.copy(value = value)
                    } else {
                        pref
                    }
                }
                _preferences.value = updatedList
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update preference: ${e.message}"
                )
            }
        }
    }
    
    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class PreferencesUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class PreferenceItem(
    val key: String,
    val value: String,
    val type: PreferenceType
)

enum class PreferenceType {
    STRING, BOOLEAN, NUMBER
}