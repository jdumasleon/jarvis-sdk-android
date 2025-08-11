package com.jarvis.features.preferences.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import com.google.protobuf.MessageLite
import com.jarvis.features.preferences.data.config.PreferencesConfigProviderImpl
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
    @param:ApplicationContext private val context: Context,
    private val configProvider: PreferencesConfigProviderImpl
) {
    
    suspend fun scanAllProtoDataStores(): List<AppPreference> = withContext(Dispatchers.IO) {
        val preferences = mutableListOf<AppPreference>()
        
        try {
            val config = configProvider.getDataConfig()
            android.util.Log.d("ProtoDataStoreScanner", "Starting ProtoDataStore scan with config: autoDiscover=${config.autoDiscoverProtoDataStores}, include=${config.includeProtoDataStores}, exclude=${config.excludeProtoDataStores}")
            
            // Step 1: Get ProtoDataStore names to scan based on configuration
            val protoNamesToScan = getProtoDataStoreNamesToScan(config)
            android.util.Log.d("ProtoDataStoreScanner", "ProtoDataStore names to scan: $protoNamesToScan")
            
            // Step 2: Scan configured ProtoDataStores
            protoNamesToScan.forEach { protoName ->
                try {
                    // First, try to get from registered instances (preferred)
                    val registeredDataStore = config.registeredProtoDataStores[protoName]
                    val registeredExtractor = config.protoExtractors[protoName]
                    
                    if (registeredDataStore != null && registeredExtractor != null) {
                        android.util.Log.d("ProtoDataStoreScanner", "Using registered ProtoDataStore for: $protoName")
                        preferences.addAll(extractFromRegisteredDataStore(protoName, registeredDataStore, registeredExtractor))
                    } else {
                        // Fall back to file-based scanning
                        android.util.Log.d("ProtoDataStoreScanner", "Using file-based scanning for: $protoName (not registered)")
                        val protoFile = File(context.filesDir, "datastore/$protoName.pb")
                        if (protoFile.exists()) {
                            preferences.addAll(extractProtoPreferences(protoFile))
                            android.util.Log.d("ProtoDataStoreScanner", "Successfully scanned ProtoDataStore: $protoName")
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.w("ProtoDataStoreScanner", "Failed to read proto file: $protoName", e)
                    
                    // If we can't read the proto file but it exists, report it as found but inaccessible
                    val protoFile = File(context.filesDir, "datastore/$protoName.pb")
                    if (protoFile.exists()) {
                        val preference = AppPreference(
                            key = "$protoName.file_info",
                            value = "Proto DataStore file (${protoFile.length()} bytes) - content not accessible",
                            type = PreferenceType.STRING,
                            storageType = PreferenceStorageType.PROTO_DATASTORE,
                            displayName = "Inaccessible ProtoDataStore",
                            description = "Proto DataStore file found at ${protoFile.absolutePath} but content cannot be read",
                            filePath = protoFile.absolutePath,
                            isSystemPreference = isSystemProtoDataStore(protoName),
                            isEditable = false
                        )
                        preferences.add(preference)
                    }
                }
            }
            
            // Step 3: If auto-discovery is enabled, scan for unknown proto files
            if (config.autoDiscoverProtoDataStores) {
                val discoveredNames = discoverProtoDataStoreFiles()
                val unknownNames = discoveredNames - protoNamesToScan.toSet()
                
                android.util.Log.d("ProtoDataStoreScanner", "Auto-discovered ProtoDataStores: $discoveredNames, Unknown: $unknownNames")
                
                unknownNames.forEach { unknownName ->
                    if (!config.excludeProtoDataStores.contains(unknownName)) {
                        try {
                            val protoFile = File(context.filesDir, "datastore/$unknownName.pb")
                            preferences.addAll(extractProtoPreferences(protoFile, isAutoDiscovered = true))
                        } catch (e: Exception) {
                            android.util.Log.w("ProtoDataStoreScanner", "Could not read auto-discovered ProtoDataStore: $unknownName", e)
                        }
                    }
                }
            }

            android.util.Log.d("ProtoDataStoreScanner", "Scanning completed. Found ${preferences.size} preferences")
        } catch (e: Exception) {
            android.util.Log.e("ProtoDataStoreScanner", "Failed to scan Proto DataStores", e)
        }
        
        preferences.sortedBy { it.key }
    }
    
    /**
     * Get the list of ProtoDataStore names to scan based on configuration
     */
    private fun getProtoDataStoreNamesToScan(config: com.jarvis.features.preferences.data.config.PreferencesDataConfig): List<String> {
        return when {
            config.includeProtoDataStores.isNotEmpty() -> {
                // If explicit includes are specified, use only those
                config.includeProtoDataStores.filter { !config.excludeProtoDataStores.contains(it) }
            }
            else -> {
                // If no explicit includes, scan discovered files (if auto-discovery enabled)
                if (config.autoDiscoverProtoDataStores) {
                    discoverProtoDataStoreFiles().filter { !config.excludeProtoDataStores.contains(it) }
                } else {
                    emptyList()
                }
            }
        }
    }
    
    /**
     * Discover ProtoDataStore files in the filesystem
     */
    private fun discoverProtoDataStoreFiles(): List<String> {
        val dataStoreDir = File(context.filesDir, "datastore")
        if (!dataStoreDir.exists()) {
            return emptyList()
        }
        
        return dataStoreDir.listFiles { file ->
            file.name.endsWith(".pb")
        }?.mapNotNull { file ->
            // Extract ProtoDataStore name from filename (remove .pb)
            file.nameWithoutExtension
        } ?: emptyList()
    }
    
    /**
     * Determine if a ProtoDataStore name represents a system preference
     */
    private fun isSystemProtoDataStore(protoName: String): Boolean {
        val config = configProvider.getDataConfig()
        if (!config.showSystemPreferences) {
            return protoName.startsWith("androidx.") || 
                   protoName.startsWith("android.") ||
                   protoName.contains("system") ||
                   protoName.contains("internal") ||
                   protoName.startsWith("com.android.") ||
                   protoName.startsWith("com.google.")
        }
        return false
    }
    
    private suspend fun extractProtoPreferences(file: File, isAutoDiscovered: Boolean = false): List<AppPreference> {
        val preferences = mutableListOf<AppPreference>()
        
        try {
            val fileName = file.nameWithoutExtension
            val config = configProvider.getDataConfig()
            
            // For proto datastore files, we can extract basic information
            // but detailed parsing would require the actual proto schema
            preferences.add(
                AppPreference(
                    key = "$fileName.proto_data",
                    value = "Proto Data (${file.length()} bytes)",
                    type = PreferenceType.PROTO_MESSAGE,
                    storageType = PreferenceStorageType.PROTO_DATASTORE,
                    displayName = fileName.replace("_", " ").replaceFirstChar { it.uppercase() },
                    description = if (isAutoDiscovered) "Auto-discovered Proto DataStore file at ${file.absolutePath}" else "Proto DataStore file at ${file.absolutePath}",
                    isSystemPreference = isSystemProtoDataStore(fileName),
                    isEditable = false, // Proto editing requires schema knowledge
                    filePath = file.absolutePath
                )
            )
        } catch (e: Exception) {
            // Skip invalid proto files
        }
        
        return preferences
    }
    
    /**
     * Extract preferences from a registered DataStore using its extractor function
     */
    private suspend fun extractFromRegisteredDataStore(
        name: String,
        dataStore: DataStore<*>,
        extractor: (Any) -> Map<String, Any>
    ): List<AppPreference> {
        val preferences = mutableListOf<AppPreference>()
        val config = configProvider.getDataConfig()
        
        try {
            @Suppress("UNCHECKED_CAST")
            val typedDataStore = dataStore as DataStore<Any>
            val data = typedDataStore.data.first()
            val extractedData = extractor(data)
            
            android.util.Log.d("ProtoDataStoreScanner", "Extracted ${extractedData.size} preferences from registered DataStore: $name")
            
            extractedData.forEach { (key, value) ->
                val preference = AppPreference(
                    key = "$name.$key",
                    value = value,
                    type = determinePreferenceType(value),
                    storageType = PreferenceStorageType.PROTO_DATASTORE,
                    displayName = key.replace("_", " ").replaceFirstChar { it.uppercase() },
                    description = "Proto DataStore preference from registered $name",
                    filePath = "${context.filesDir}/datastore/${name}.pb",
                    isSystemPreference = isSystemProtoDataStore(name),
                    isEditable = config.enablePreferenceEditing // Enable editing for registered DataStores
                )
                preferences.add(preference)
            }
        } catch (e: Exception) {
            android.util.Log.e("ProtoDataStoreScanner", "Failed to extract from registered DataStore: $name", e)
            
            // Add error info as a preference
            val errorPreference = AppPreference(
                key = "$name.extraction_error",
                value = "Failed to extract data: ${e.message}",
                type = PreferenceType.STRING,
                storageType = PreferenceStorageType.PROTO_DATASTORE,
                displayName = "Extraction Error",
                description = "Error occurred while extracting data from registered DataStore $name",
                filePath = "${context.filesDir}/datastore/${name}.pb",
                isSystemPreference = isSystemProtoDataStore(name),
                isEditable = false
            )
            preferences.add(errorPreference)
        }
        
        return preferences
    }
    
    /**
     * Determine the preference type from a value  
     */
    private fun determinePreferenceType(value: Any): PreferenceType {
        return when (value) {
            is Boolean -> PreferenceType.BOOLEAN
            is Int -> PreferenceType.INTEGER
            is Long -> PreferenceType.LONG
            is Float -> PreferenceType.FLOAT
            is Double -> PreferenceType.DOUBLE
            is String -> PreferenceType.STRING
            is Set<*> -> PreferenceType.STRING_SET
            else -> PreferenceType.STRING
        }
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