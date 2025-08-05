package com.jarvis.features.preferences.data.local

import android.content.Context
import android.content.SharedPreferences
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
    @ApplicationContext private val context: Context
) {
    
    suspend fun scanAllSharedPreferences(): List<AppPreference> = withContext(Dispatchers.IO) {
        val preferences = mutableListOf<AppPreference>()
        
        try {
            // Get all SharedPreferences files
            val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
            if (prefsDir.exists() && prefsDir.isDirectory) {
                prefsDir.listFiles()?.forEach { file ->
                    if (file.name.endsWith(".xml")) {
                        val prefsName = file.nameWithoutExtension
                        try {
                            val sharedPrefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
                            preferences.addAll(extractPreferencesFromSharedPrefs(sharedPrefs, prefsName, file))
                        } catch (e: Exception) {
                            // Skip this preferences file if it can't be read
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Return empty list if scanning fails
        }
        
        preferences.sortedBy { it.key }
    }
    
    private fun extractPreferencesFromSharedPrefs(
        sharedPrefs: SharedPreferences,
        prefsFileName: String,
        file: File
    ): List<AppPreference> {
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
                displayName = key,
                description = "From: $prefsFileName (${file.absolutePath})",
                isSystemPreference = isSystemPreference(prefsFileName),
                isEditable = true,
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