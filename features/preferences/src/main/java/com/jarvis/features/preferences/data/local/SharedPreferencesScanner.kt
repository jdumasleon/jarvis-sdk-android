package com.jarvis.features.preferences.data.local

import android.content.Context
import android.content.SharedPreferences
import com.jarvis.features.preferences.data.config.PreferencesConfigProviderImpl
import com.jarvis.features.preferences.domain.entity.AppPreference
import com.jarvis.features.preferences.domain.entity.PreferenceStorageType
import com.jarvis.features.preferences.domain.entity.PreferenceType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesScanner @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val configProvider: PreferencesConfigProviderImpl
) {
    
    suspend fun scanAllSharedPreferences(): List<AppPreference> = withContext(Dispatchers.IO) {
        val preferences = mutableListOf<AppPreference>()
        
        try {
            val config = configProvider.getDataConfig()
            android.util.Log.d("SharedPrefsScanner", "Starting SharedPreferences scan with config: autoDiscover=${config.autoDiscoverSharedPrefs}, include=${config.includeSharedPrefs}, exclude=${config.excludeSharedPrefs}")
            
            // Step 1: Get SharedPreferences files to scan based on configuration
            val prefsNamesToScan = getSharedPrefsNamesToScan(config)
            android.util.Log.d("SharedPrefsScanner", "SharedPreferences names to scan: $prefsNamesToScan")
            
            // Step 2: Scan configured SharedPreferences
            prefsNamesToScan.forEach { prefsName ->
                try {
                    val sharedPrefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
                    val prefsFile = File(context.applicationInfo.dataDir, "shared_prefs/$prefsName.xml")
                    preferences.addAll(extractPreferencesFromSharedPrefs(sharedPrefs, prefsName, prefsFile))
                    android.util.Log.d("SharedPrefsScanner", "Successfully scanned SharedPreferences: $prefsName")
                } catch (e: Exception) {
                    android.util.Log.w("SharedPrefsScanner", "Failed to read preferences file: $prefsName", e)
                    
                    // If we can't read the SharedPrefs but the file exists, report it as found but inaccessible
                    val prefsFile = File(context.applicationInfo.dataDir, "shared_prefs/$prefsName.xml")
                    if (prefsFile.exists()) {
                        val preference = AppPreference(
                            key = "$prefsName.file_info",
                            value = "SharedPreferences file (${prefsFile.length()} bytes) - content not accessible",
                            type = PreferenceType.STRING,
                            storageType = PreferenceStorageType.SHARED_PREFERENCES,
                            displayName = "Inaccessible SharedPreferences",
                            description = "SharedPreferences file found at ${prefsFile.absolutePath} but content cannot be read",
                            filePath = prefsFile.absolutePath,
                            isSystemPreference = isSystemSharedPrefs(prefsName),
                            isEditable = false
                        )
                        preferences.add(preference)
                    }
                }
            }
            
            // Step 3: If auto-discovery is enabled, scan for unknown SharedPreferences files
            if (config.autoDiscoverSharedPrefs) {
                val discoveredNames = discoverSharedPrefsFiles()
                val unknownNames = discoveredNames - prefsNamesToScan.toSet()
                
                android.util.Log.d("SharedPrefsScanner", "Auto-discovered SharedPreferences: $discoveredNames, Unknown: $unknownNames")
                
                unknownNames.forEach { unknownName ->
                    if (!config.excludeSharedPrefs.contains(unknownName)) {
                        try {
                            val sharedPrefs = context.getSharedPreferences(unknownName, Context.MODE_PRIVATE)
                            val prefsFile = File(context.applicationInfo.dataDir, "shared_prefs/$unknownName.xml")
                            preferences.addAll(extractPreferencesFromSharedPrefs(sharedPrefs, unknownName, prefsFile, isAutoDiscovered = true))
                        } catch (e: Exception) {
                            android.util.Log.w("SharedPrefsScanner", "Could not read auto-discovered SharedPreferences: $unknownName", e)
                        }
                    }
                }
            }

            android.util.Log.d("SharedPrefsScanner", "Scanning completed. Found ${preferences.size} preferences")
        } catch (e: Exception) {
            android.util.Log.e("SharedPrefsScanner", "Failed to scan SharedPreferences", e)
        }
        
        preferences.sortedBy { it.key }
    }
    
    /**
     * Get the list of SharedPreferences names to scan based on configuration
     */
     private fun getSharedPrefsNamesToScan(config: com.jarvis.features.preferences.data.config.PreferencesDataConfig): List<String> {
        return when {
            config.includeSharedPrefs.isNotEmpty() -> {
                // If explicit includes are specified, use only those
                config.includeSharedPrefs.filter { !config.excludeSharedPrefs.contains(it) }
            }
            else -> {
                // If no explicit includes, scan discovered files (if auto-discovery enabled)
                if (config.autoDiscoverSharedPrefs) {
                    discoverSharedPrefsFiles().filter { !config.excludeSharedPrefs.contains(it) }
                } else {
                    emptyList()
                }
            }
        }
    }
    
    /**
     * Discover SharedPreferences files in the filesystem
     */
    private fun discoverSharedPrefsFiles(): List<String> {
        val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
        if (!prefsDir.exists()) {
            return emptyList()
        }
        
        return prefsDir.listFiles { file ->
            file.name.endsWith(".xml")
        }?.mapNotNull { file ->
            // Extract SharedPreferences name from filename (remove .xml)
            file.nameWithoutExtension
        } ?: emptyList()
    }
    
    /**
     * Determine if a SharedPreferences name represents a system preference
     */
    private fun isSystemSharedPrefs(prefsName: String): Boolean {
        val config = configProvider.getDataConfig()
        if (!config.showSystemPreferences) {
            return prefsName.startsWith("androidx.") || 
                   prefsName.startsWith("android.") ||
                   prefsName.contains("system") ||
                   prefsName.contains("internal") ||
                   prefsName.startsWith("com.android.") ||
                   prefsName.startsWith("com.google.")
        }
        return false
    }

    private fun extractPreferencesFromSharedPrefs(
        sharedPrefs: SharedPreferences,
        prefsFileName: String,
        file: File,
        isAutoDiscovered: Boolean = false
    ): List<AppPreference> {
        val config = configProvider.getDataConfig()
        return sharedPrefs.all.map { (key, value) ->
            AppPreference(
                key = "$prefsFileName.$key",
                value = value ?: "",
                type = when (value) {
                    is Boolean -> PreferenceType.BOOLEAN
                    is Int -> PreferenceType.INTEGER
                    is Long -> PreferenceType.LONG
                    is Float -> PreferenceType.FLOAT
                    is Double -> PreferenceType.DOUBLE
                    is Set<*> -> PreferenceType.STRING_SET
                    else -> PreferenceType.STRING
                },
                storageType = PreferenceStorageType.SHARED_PREFERENCES,
                displayName = key.replace("_", " ").replaceFirstChar { it.uppercase() },
                description = if (isAutoDiscovered) "Auto-discovered from: $prefsFileName (${file.absolutePath})" else "From: $prefsFileName (${file.absolutePath})",
                isSystemPreference = isSystemSharedPrefs(prefsFileName),
                isEditable = config.enablePreferenceEditing,
                filePath = file.absolutePath
            )
        }
    }
    
    suspend fun updateSharedPreference(
        prefsFileName: String, 
        key: String, 
        value: Any, 
        type: PreferenceType
    ): Result<Unit> {
        return try {
            val sharedPrefs = context.getSharedPreferences(prefsFileName, Context.MODE_PRIVATE)
            val editor = sharedPrefs.edit()
            
            when (type) {
                PreferenceType.STRING -> editor.putString(key, value.toString())
                PreferenceType.BOOLEAN -> editor.putBoolean(key, value as Boolean)
                PreferenceType.INTEGER -> editor.putInt(key, value as Int)
                PreferenceType.LONG -> editor.putLong(key, value as Long)
                PreferenceType.FLOAT -> editor.putFloat(key, value as Float)
                PreferenceType.DOUBLE -> editor.putFloat(key, (value as Double).toFloat())
                PreferenceType.STRING_SET -> editor.putStringSet(key, value as Set<String>)
                else -> throw IllegalArgumentException("Unsupported type for SharedPreferences: $type")
            }
            
            editor.apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteSharedPreference(prefsFileName: String, key: String): Result<Unit> {
        return try {
            val sharedPrefs = context.getSharedPreferences(prefsFileName, Context.MODE_PRIVATE)
            sharedPrefs.edit().remove(key).apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun clearSharedPreferences(prefsFileName: String): Result<Unit> {
        return try {
            val sharedPrefs = context.getSharedPreferences(prefsFileName, Context.MODE_PRIVATE)
            sharedPrefs.edit().clear().apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun isSystemPreference(prefsFileName: String): Boolean {
        return prefsFileName.startsWith("android.") || 
               prefsFileName.startsWith("system.") ||
               prefsFileName.startsWith("androidx.") ||
               prefsFileName.contains("crashlytics") ||
               prefsFileName.contains("firebase") ||
               prefsFileName.contains("google") ||
               prefsFileName.contains("gms")
    }
}