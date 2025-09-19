package com.jarvis.api.providers

import kotlinx.coroutines.flow.Flow

/**
 * PreferencesProvider - Clean interface for preferences management
 * 
 * This interface provides a clean abstraction for preferences functionality,
 * allowing host applications to integrate preferences management without coupling to
 * internal implementation details.
 * 
 * Features:
 * - Type-safe preference management
 * - Reactive preference updates via Flow
 * - Bulk operations support
 * - Conditional availability based on configuration
 * 
 * Example usage in host applications:
 * ```kotlin
 * @Inject
 * lateinit var preferencesProvider: PreferencesProvider
 * 
 * // Get preference value
 * val debugMode = preferencesProvider.getBoolean("debug_mode", false)
 * 
 * // Observe preference changes
 * preferencesProvider.getBooleanFlow("debug_mode", false)
 *     .collect { enabled ->
 *         // Handle debug mode changes
 *     }
 * ```
 */
interface PreferencesProvider {
    
    /**
     * Get a string preference value
     * 
     * @param key The preference key
     * @param defaultValue Default value if preference doesn't exist
     * @return The preference value or default value
     */
    fun getString(key: String, defaultValue: String = ""): String
    
    /**
     * Get a boolean preference value
     * 
     * @param key The preference key
     * @param defaultValue Default value if preference doesn't exist
     * @return The preference value or default value
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
    
    /**
     * Get an integer preference value
     * 
     * @param key The preference key
     * @param defaultValue Default value if preference doesn't exist
     * @return The preference value or default value
     */
    fun getInt(key: String, defaultValue: Int = 0): Int
    
    /**
     * Get a long preference value
     * 
     * @param key The preference key
     * @param defaultValue Default value if preference doesn't exist
     * @return The preference value or default value
     */
    fun getLong(key: String, defaultValue: Long = 0L): Long
    
    /**
     * Set a string preference value
     * 
     * @param key The preference key
     * @param value The value to store
     */
    fun putString(key: String, value: String)
    
    /**
     * Set a boolean preference value
     * 
     * @param key The preference key
     * @param value The value to store
     */
    fun putBoolean(key: String, value: Boolean)
    
    /**
     * Set an integer preference value
     * 
     * @param key The preference key
     * @param value The value to store
     */
    fun putInt(key: String, value: Int)
    
    /**
     * Set a long preference value
     * 
     * @param key The preference key
     * @param value The value to store
     */
    fun putLong(key: String, value: Long)
    
    /**
     * Remove a preference
     * 
     * @param key The preference key to remove
     */
    fun remove(key: String)
    
    /**
     * Clear all preferences
     */
    fun clear()
    
    /**
     * Check if a preference key exists
     * 
     * @param key The preference key to check
     * @return true if the key exists, false otherwise
     */
    fun contains(key: String): Boolean
    
    /**
     * Get all preference keys
     * 
     * @return Set of all preference keys
     */
    fun getAllKeys(): Set<String>
    
    /**
     * Observe string preference changes
     * 
     * @param key The preference key
     * @param defaultValue Default value if preference doesn't exist
     * @return Flow emitting preference value changes
     */
    fun getStringFlow(key: String, defaultValue: String = ""): Flow<String>
    
    /**
     * Observe boolean preference changes
     * 
     * @param key The preference key
     * @param defaultValue Default value if preference doesn't exist
     * @return Flow emitting preference value changes
     */
    fun getBooleanFlow(key: String, defaultValue: Boolean = false): Flow<Boolean>
    
    /**
     * Observe integer preference changes
     * 
     * @param key The preference key
     * @param defaultValue Default value if preference doesn't exist
     * @return Flow emitting preference value changes
     */
    fun getIntFlow(key: String, defaultValue: Int = 0): Flow<Int>
    
    /**
     * Observe long preference changes
     * 
     * @param key The preference key
     * @param defaultValue Default value if preference doesn't exist
     * @return Flow emitting preference value changes
     */
    fun getLongFlow(key: String, defaultValue: Long = 0L): Flow<Long>
    
    /**
     * Check if preferences management is currently enabled
     * 
     * @return true if preferences are available, false otherwise
     */
    fun isEnabled(): Boolean
}