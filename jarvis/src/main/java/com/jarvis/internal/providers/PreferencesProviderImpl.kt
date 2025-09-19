package com.jarvis.internal.providers

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.jarvis.api.providers.PreferencesProvider
import com.jarvis.config.JarvisConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Internal implementation of PreferencesProvider
 * 
 * This is the actual implementation that provides access to SharedPreferences
 * for the Jarvis SDK. It's internal and should not be used directly by host applications.
 * 
 * This implementation uses a dedicated SharedPreferences file for Jarvis settings
 * to avoid conflicts with host application preferences.
 */
@Singleton
class PreferencesProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val jarvisConfig: JarvisConfig
) : PreferencesProvider {
    
    companion object {
        private const val JARVIS_PREFS_NAME = "jarvis_sdk_preferences"
    }
    
    private val preferences: SharedPreferences = context.getSharedPreferences(
        JARVIS_PREFS_NAME, 
        Context.MODE_PRIVATE
    )
    
    // Flow to notify when preferences change
    private val _preferencesFlow = MutableStateFlow(Unit)
    
    override fun getString(key: String, defaultValue: String): String {
        if (!isEnabled()) return defaultValue
        return preferences.getString(key, defaultValue) ?: defaultValue
    }
    
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        if (!isEnabled()) return defaultValue
        return preferences.getBoolean(key, defaultValue)
    }
    
    override fun getInt(key: String, defaultValue: Int): Int {
        if (!isEnabled()) return defaultValue
        return preferences.getInt(key, defaultValue)
    }
    
    override fun getLong(key: String, defaultValue: Long): Long {
        if (!isEnabled()) return defaultValue
        return preferences.getLong(key, defaultValue)
    }
    
    override fun putString(key: String, value: String) {
        if (!isEnabled()) return
        preferences.edit { putString(key, value) }
        _preferencesFlow.value = Unit
    }
    
    override fun putBoolean(key: String, value: Boolean) {
        if (!isEnabled()) return
        preferences.edit { putBoolean(key, value) }
        _preferencesFlow.value = Unit
    }
    
    override fun putInt(key: String, value: Int) {
        if (!isEnabled()) return
        preferences.edit { putInt(key, value) }
        _preferencesFlow.value = Unit
    }
    
    override fun putLong(key: String, value: Long) {
        if (!isEnabled()) return
        preferences.edit { putLong(key, value) }
        _preferencesFlow.value = Unit
    }
    
    override fun remove(key: String) {
        if (!isEnabled()) return
        preferences.edit { remove(key) }
        _preferencesFlow.value = Unit
    }
    
    override fun clear() {
        if (!isEnabled()) return
        preferences.edit { clear() }
        _preferencesFlow.value = Unit
    }
    
    override fun contains(key: String): Boolean {
        if (!isEnabled()) return false
        return preferences.contains(key)
    }
    
    override fun getAllKeys(): Set<String> {
        if (!isEnabled()) return emptySet()
        return preferences.all.keys
    }
    
    override fun getStringFlow(key: String, defaultValue: String): Flow<String> {
        return _preferencesFlow.asStateFlow().map {
            getString(key, defaultValue)
        }
    }
    
    override fun getBooleanFlow(key: String, defaultValue: Boolean): Flow<Boolean> {
        return _preferencesFlow.asStateFlow().map {
            getBoolean(key, defaultValue)
        }
    }
    
    override fun getIntFlow(key: String, defaultValue: Int): Flow<Int> {
        return _preferencesFlow.asStateFlow().map {
            getInt(key, defaultValue)
        }
    }
    
    override fun getLongFlow(key: String, defaultValue: Long): Flow<Long> {
        return _preferencesFlow.asStateFlow().map {
            getLong(key, defaultValue)
        }
    }
    
    override fun isEnabled(): Boolean {
        return true // Jarvis preferences are always enabled when SDK is initialized
    }
}