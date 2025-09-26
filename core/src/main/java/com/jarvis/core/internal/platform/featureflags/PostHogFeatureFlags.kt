@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.core.internal.platform.featureflags

import androidx.annotation.RestrictTo

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.jarvis.core.internal.common.di.CoroutineDispatcherModule
import com.posthog.PostHog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PostHog implementation of FeatureFlags
 * Uses PostHog's feature flag capabilities with local DataStore for overrides
 */
@Singleton
class PostHogFeatureFlags @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @param:CoroutineDispatcherModule.IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FeatureFlags {

    private val overridePrefix = "override_"

    override suspend fun initialize() = withContext(ioDispatcher) {
        // PostHog feature flags are initialized when PostHog is set up
        // Additional initialization can be done here if needed
    }

    override suspend fun isFeatureEnabled(key: String): Boolean = withContext(ioDispatcher) {
        // Check for local override first
        val overrideKey = booleanPreferencesKey("${overridePrefix}${key}")
        val preferences = dataStore.data.first()
        val override = preferences[overrideKey]
        
        if (override != null) {
            return@withContext override
        }
        
        // Fallback to PostHog feature flags
        return@withContext try {
            PostHog.isFeatureEnabled(key)
        } catch (e: Exception) {
            false // Default to false if error
        }
    }

    override suspend fun getStringFlag(key: String, defaultValue: String): String = withContext(ioDispatcher) {
        // Check for local override first
        val overrideKey = stringPreferencesKey("${overridePrefix}${key}")
        val preferences = dataStore.data.first()
        val override = preferences[overrideKey]
        
        if (override != null) {
            return@withContext override
        }
        
        // Fallback to PostHog feature flags
        return@withContext try {
            PostHog.getFeatureFlag(key) as? String ?: defaultValue
        } catch (e: Exception) {
            defaultValue
        }
    }

    override suspend fun getBooleanFlag(key: String, defaultValue: Boolean): Boolean = withContext(ioDispatcher) {
        // Check for local override first
        val overrideKey = booleanPreferencesKey("${overridePrefix}${key}")
        val preferences = dataStore.data.first()
        val override = preferences[overrideKey]
        
        if (override != null) {
            return@withContext override
        }
        
        // Fallback to PostHog feature flags
        return@withContext try {
            when (val flag = PostHog.getFeatureFlag(key)) {
                is Boolean -> flag
                is String -> flag.toBoolean()
                null -> defaultValue
                else -> defaultValue
            }
        } catch (e: Exception) {
            defaultValue
        }
    }

    override suspend fun getIntFlag(key: String, defaultValue: Int): Int = withContext(ioDispatcher) {
        // Check for local override first
        val overrideKey = intPreferencesKey("${overridePrefix}${key}")
        val preferences = dataStore.data.first()
        val override = preferences[overrideKey]
        
        if (override != null) {
            return@withContext override
        }
        
        // Fallback to PostHog feature flags
        return@withContext try {
            when (val flag = PostHog.getFeatureFlag(key)) {
                is Int -> flag
                is String -> flag.toIntOrNull() ?: defaultValue
                is Number -> flag.toInt()
                null -> defaultValue
                else -> defaultValue
            }
        } catch (e: Exception) {
            defaultValue
        }
    }

    override suspend fun getDoubleFlag(key: String, defaultValue: Double): Double = withContext(ioDispatcher) {
        // Check for local override first
        val overrideKey = doublePreferencesKey("${overridePrefix}${key}")
        val preferences = dataStore.data.first()
        val override = preferences[overrideKey]
        
        if (override != null) {
            return@withContext override
        }
        
        // Fallback to PostHog feature flags
        return@withContext try {
            when (val flag = PostHog.getFeatureFlag(key)) {
                is Double -> flag
                is String -> flag.toDoubleOrNull() ?: defaultValue
                is Number -> flag.toDouble()
                null -> defaultValue
                else -> defaultValue
            }
        } catch (e: Exception) {
            defaultValue
        }
    }

    override suspend fun getJsonFlag(key: String, defaultValue: Map<String, Any>): Map<String, Any> = withContext(ioDispatcher) {
        // PostHog doesn't directly support JSON flags, so we'll return default
        // In a real implementation, you might parse JSON strings
        return@withContext defaultValue
    }

    override suspend fun reload() = withContext(ioDispatcher) {
        try {
            PostHog.reloadFeatureFlags()
        } catch (e: Exception) {
            // Handle reload failure
        }
    }

    override fun getAllFlags(): Flow<Map<String, Any>> {
        // PostHog doesn't provide a way to get all flags
        // Return empty flow for now
        return flowOf(emptyMap())
    }

    override suspend fun overrideFlag(key: String, value: Any) {
        withContext(ioDispatcher) {
            // Store override in DataStore
            when (value) {
                is String -> {
                    val overrideKey = stringPreferencesKey("${overridePrefix}${key}")
                    dataStore.updateData { prefs ->
                        prefs.toMutablePreferences().apply {
                            set(overrideKey, value)
                        }
                    }
                }
                is Boolean -> {
                    val overrideKey = booleanPreferencesKey("${overridePrefix}${key}")
                    dataStore.updateData { prefs ->
                        prefs.toMutablePreferences().apply {
                            set(overrideKey, value)
                        }
                    }
                }
                is Int -> {
                    val overrideKey = intPreferencesKey("${overridePrefix}${key}")
                    dataStore.updateData { prefs ->
                        prefs.toMutablePreferences().apply {
                            set(overrideKey, value)
                        }
                    }
                }
                is Double -> {
                    val overrideKey = doublePreferencesKey("${overridePrefix}${key}")
                    dataStore.updateData { prefs ->
                        prefs.toMutablePreferences().apply {
                            set(overrideKey, value)
                        }
                    }
                }
                else -> {
                    // Store as string for other types
                    val overrideKey = stringPreferencesKey("${overridePrefix}${key}")
                    dataStore.updateData { prefs ->
                        prefs.toMutablePreferences().apply {
                            set(overrideKey, value.toString())
                        }
                    }
                }
            }
        }
    }

    override suspend fun clearOverrides() {
        withContext(ioDispatcher) {
            dataStore.updateData { prefs ->
                val mutablePrefs = prefs.toMutablePreferences()
                prefs.asMap().keys.filter { it.name.startsWith(overridePrefix) }
                    .forEach { key -> mutablePrefs.remove(key) }
                mutablePrefs
            }
        }
    }
}