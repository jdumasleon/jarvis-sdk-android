package com.jarvis.features.preferences.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import com.google.protobuf.MessageLite
import com.jarvis.features.preferences.domain.entity.AppPreference
import com.jarvis.features.preferences.domain.entity.PreferenceStorageType
import com.jarvis.features.preferences.domain.entity.PreferenceType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.reflect.Field
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProtoDataStoreScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    suspend fun scanAllProtoDataStores(): List<AppPreference> = withContext(Dispatchers.IO) {
        val preferences = mutableListOf<AppPreference>()
        
        try {
            // Get all proto datastore files
            val dataStoreDir = File(context.filesDir, "datastore")
            if (dataStoreDir.exists() && dataStoreDir.isDirectory) {
                dataStoreDir.listFiles()?.forEach { file ->
                    if (file.name.endsWith(".pb")) {
                        try {
                            preferences.addAll(extractProtoPreferences(file))
                        } catch (e: Exception) {
                            // Skip files that can't be read as proto
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Return empty list if scanning fails
        }
        
        preferences.sortedBy { it.key }
    }
    
    private suspend fun extractProtoPreferences(file: File): List<AppPreference> {
        val preferences = mutableListOf<AppPreference>()
        
        try {
            val fileName = file.nameWithoutExtension
            
            // For proto datastore files, we can extract basic information
            // but detailed parsing would require the actual proto schema
            preferences.add(
                AppPreference(
                    key = "$fileName.proto_data",
                    value = "Proto Data (${file.length()} bytes)",
                    type = PreferenceType.PROTO_MESSAGE,
                    storageType = PreferenceStorageType.PROTO_DATASTORE,
                    displayName = fileName,
                    description = "Proto DataStore file at ${file.absolutePath}",
                    isSystemPreference = fileName.startsWith("androidx.") || 
                                       fileName.startsWith("android.") ||
                                       fileName.contains("system"),
                    isEditable = false, // Proto editing requires schema knowledge
                    filePath = file.absolutePath
                )
            )
        } catch (e: Exception) {
            // Skip invalid proto files
        }
        
        return preferences
    }
    
    suspend fun updateProtoPreference(preference: AppPreference, newValue: Any): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Proto DataStore editing requires schema knowledge"))
    }
    
    suspend fun deleteProtoPreference(preference: AppPreference): Result<Unit> {
        return try {
            preference.filePath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                    Result.success(Unit)
                } else {
                    Result.failure(IllegalStateException("Proto file not found"))
                }
            } ?: Result.failure(IllegalStateException("No file path provided"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}