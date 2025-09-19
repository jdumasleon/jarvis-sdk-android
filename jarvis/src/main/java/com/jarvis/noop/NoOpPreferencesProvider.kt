package com.jarvis.noop

import com.jarvis.api.providers.PreferencesProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * No-op implementation of PreferencesProvider
 * 
 * This implementation provides graceful degradation when preferences management
 * is disabled or unavailable. All methods return safe default values
 * and perform no actual operations.
 * 
 * Used in scenarios:
 * - Release builds where preferences inspection should be disabled
 * - When preferences features are explicitly disabled in configuration
 * - Error recovery when the full implementation fails to initialize
 */
@Singleton
class NoOpPreferencesProvider @Inject constructor() : PreferencesProvider {
    
    override fun getString(key: String, defaultValue: String): String {
        return defaultValue
    }
    
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return defaultValue
    }
    
    override fun getInt(key: String, defaultValue: Int): Int {
        return defaultValue
    }
    
    override fun getLong(key: String, defaultValue: Long): Long {
        return defaultValue
    }
    
    override fun putString(key: String, value: String) {
        // No-op: value is not stored
    }
    
    override fun putBoolean(key: String, value: Boolean) {
        // No-op: value is not stored
    }
    
    override fun putInt(key: String, value: Int) {
        // No-op: value is not stored
    }
    
    override fun putLong(key: String, value: Long) {
        // No-op: value is not stored
    }
    
    override fun remove(key: String) {
        // No-op: nothing to remove
    }
    
    override fun clear() {
        // No-op: nothing to clear
    }
    
    override fun contains(key: String): Boolean {
        return false
    }
    
    override fun getAllKeys(): Set<String> {
        return emptySet()
    }
    
    override fun getStringFlow(key: String, defaultValue: String): Flow<String> {
        return flowOf(defaultValue)
    }
    
    override fun getBooleanFlow(key: String, defaultValue: Boolean): Flow<Boolean> {
        return flowOf(defaultValue)
    }
    
    override fun getIntFlow(key: String, defaultValue: Int): Flow<Int> {
        return flowOf(defaultValue)
    }
    
    override fun getLongFlow(key: String, defaultValue: Long): Flow<Long> {
        return flowOf(defaultValue)
    }
    
    override fun isEnabled(): Boolean {
        return false
    }
}