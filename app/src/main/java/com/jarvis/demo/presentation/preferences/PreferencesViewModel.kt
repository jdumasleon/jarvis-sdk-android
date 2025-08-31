package com.jarvis.demo.presentation.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.core.common.di.CoroutineDispatcherModule.IoDispatcher
import com.jarvis.core.presentation.state.ResourceState
import com.jarvis.demo.data.preferences.DemoPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreferencesViewModel @Inject constructor(
    private val demoPreferencesRepository: DemoPreferencesRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<PreferencesUiState>(ResourceState.Idle)
    val uiState: StateFlow<PreferencesUiState> = _uiState.asStateFlow()
    
    init {
        loadPreferences()
    }
    
    fun onEvent(event: PreferencesEvent) {
        when (event) {
            is PreferencesEvent.LoadPreferences -> loadPreferences()
            is PreferencesEvent.RefreshPreferences -> refreshPreferences()
            is PreferencesEvent.SelectTab -> selectTab(event.storageType)
            is PreferencesEvent.SearchQueryChanged -> updateSearchQuery(event.query)
            is PreferencesEvent.FilterTypeChanged -> updateFilterType(event.filterType)
            is PreferencesEvent.PreferenceSelected -> selectPreference(event.preference)
            is PreferencesEvent.UpdatePreference -> updatePreference(event.key, event.value, event.type)
            is PreferencesEvent.DeletePreference -> deletePreference(event.key)
            is PreferencesEvent.ShowAddDialog -> showAddDialog(event.show)
            is PreferencesEvent.ShowEditDialog -> showEditDialog(event.show)
            is PreferencesEvent.ShowDeleteConfirmation -> showDeleteConfirmation(event.show)
            is PreferencesEvent.ClearAllPreferences -> clearAllPreferences()
            is PreferencesEvent.GenerateRandomPreferences -> generateRandomPreferences()
            is PreferencesEvent.ClearError -> clearError()
        }
    }
    
    private fun loadPreferences() {
        _uiState.update { ResourceState.Loading }
        viewModelScope.launch(ioDispatcher) {
            try {
                // Set up reactive subscription to all three preference stores
                combine(
                    demoPreferencesRepository.getSharedPreferencesFlow(),
                    demoPreferencesRepository.getDataStorePreferencesFlow(),
                    demoPreferencesRepository.getProtoDataStorePreferencesFlow()
                ) { sharedPrefs, dataStorePrefs, protoPrefs ->
                    PreferencesUiData(
                        sharedPreferences = sharedPrefs,
                        dataStorePreferences = dataStorePrefs,
                        protoDataStorePreferences = protoPrefs
                    )
                }.catch { exception ->
                    _uiState.update { 
                        ResourceState.Error(exception, "Failed to load preferences")
                    }
                }.collect { uiData ->
                    _uiState.update { ResourceState.Success(uiData) }
                }
            } catch (exception: Exception) {
                _uiState.update { 
                    ResourceState.Error(exception, "Failed to load preferences")
                }
            }
        }
    }
    
    private fun generateSamplePreferencesForAllTabs() {
        viewModelScope.launch(ioDispatcher) {
            try {
                // Generate sample data for all three storage types
                demoPreferencesRepository.generateAllSampleData()
                // The reactive flow will automatically update the UI state
            } catch (exception: Exception) {
                _uiState.update { 
                    ResourceState.Error(exception, "Failed to generate sample preferences")
                }
            }
        }
    }

    private fun selectTab(storageType: PreferenceStorageType) {
        val currentData = _uiState.value.getDataOrNull() ?: return
        val updatedData = currentData.copy(selectedTab = storageType)
        _uiState.update { ResourceState.Success(updatedData) }
    }
    
    private fun updateSearchQuery(query: String) {
        val currentData = _uiState.value.getDataOrNull() ?: return
        val updatedData = currentData.copy(searchQuery = query)
        _uiState.update { ResourceState.Success(updatedData) }
    }
    
    private fun updateFilterType(filterType: PreferenceTypeFilter) {
        val currentData = _uiState.value.getDataOrNull() ?: return
        val updatedData = currentData.copy(filterType = filterType)
        _uiState.update { ResourceState.Success(updatedData) }
    }
    
    private fun selectPreference(preference: PreferenceItem) {
        val currentData = _uiState.value.getDataOrNull() ?: return
        val updatedData = currentData.copy(selectedPreference = preference)
        _uiState.update { ResourceState.Success(updatedData) }
    }
    
    private fun showAddDialog(show: Boolean) {
        val currentData = _uiState.value.getDataOrNull() ?: return
        val updatedData = currentData.copy(showAddDialog = show)
        _uiState.update { ResourceState.Success(updatedData) }
    }
    
    private fun showEditDialog(show: Boolean) {
        val currentData = _uiState.value.getDataOrNull() ?: return
        val updatedData = currentData.copy(showEditDialog = show)
        _uiState.update { ResourceState.Success(updatedData) }
    }
    
    private fun showDeleteConfirmation(show: Boolean) {
        val currentData = _uiState.value.getDataOrNull() ?: return
        val updatedData = currentData.copy(showDeleteConfirmation = show)
        _uiState.update { ResourceState.Success(updatedData) }
    }
    
    private fun generateRandomPreferences() {
        generateSamplePreferencesForAllTabs()
    }
    
    private fun refreshPreferences() {
        viewModelScope.launch(ioDispatcher) {
            val currentData = _uiState.value.getDataOrNull() ?: PreferencesUiData()
            
            // Set refresh state
            _uiState.update { ResourceState.Success(currentData.copy(isRefreshing = true)) }
            
            try {
                // Collect the combined preferences flow
                combine(
                    demoPreferencesRepository.getSharedPreferencesFlow(),
                    demoPreferencesRepository.getDataStorePreferencesFlow(),
                    demoPreferencesRepository.getProtoDataStorePreferencesFlow()
                ) { sharedPrefs, dataStorePrefs, protoPrefs ->
                    PreferencesUiData(
                        sharedPreferences = sharedPrefs,
                        dataStorePreferences = dataStorePrefs,
                        protoDataStorePreferences = protoPrefs,
                        selectedTab = currentData.selectedTab,
                        searchQuery = currentData.searchQuery,
                        filterType = currentData.filterType,
                        isRefreshing = false // Turn off refresh indicator
                    )
                }.catch { exception ->
                    // Turn off refresh indicator even on error
                    val errorData = currentData.copy(isRefreshing = false)
                    _uiState.update { ResourceState.Success(errorData) }
                }.collect { combinedData ->
                    _uiState.update { ResourceState.Success(combinedData) }
                }
                
            } catch (exception: Exception) {
                // Turn off refresh indicator on error
                val errorData = currentData.copy(isRefreshing = false)
                _uiState.update { ResourceState.Success(errorData) }
            }
        }
    }
    
    private fun clearAllPreferences() {
        viewModelScope.launch(ioDispatcher) {
            try {
                val currentData = _uiState.value.getDataOrNull() ?: return@launch
                
                // Clear based on selected tab
                when (currentData.selectedTab) {
                    PreferenceStorageType.SHARED_PREFERENCES -> {
                        demoPreferencesRepository.clearSharedPreferences()
                    }
                    PreferenceStorageType.PREFERENCES_DATASTORE -> {
                        demoPreferencesRepository.clearDataStorePreferences()
                    }
                    PreferenceStorageType.PROTO_DATASTORE -> {
                        demoPreferencesRepository.clearProtoDataStorePreferences()
                    }
                }
                // The reactive flow will automatically update the UI state
            } catch (exception: Exception) {
                _uiState.update { 
                    ResourceState.Error(exception, "Failed to clear preferences")
                }
            }
        }
    }
    
    private fun deletePreference(key: String) {
        viewModelScope.launch(ioDispatcher) {
            try {
                val currentData = _uiState.value.getDataOrNull() ?: return@launch
                
                // Delete based on selected tab - simplified approach since we can't easily
                // map specific keys back to storage types without more complex implementation
                when (currentData.selectedTab) {
                    PreferenceStorageType.SHARED_PREFERENCES -> {
                        // For SharedPreferences, we'd need to know which file the key belongs to
                        // This is a limitation of the current approach - would need more complex logic
                        _uiState.update { 
                            ResourceState.Error(Exception("Delete operation not supported for SharedPreferences in this demo"), 
                                               "Delete not supported for SharedPreferences")
                        }
                    }
                    PreferenceStorageType.PREFERENCES_DATASTORE -> {
                        // Extract the actual key without file prefix
                        val actualKey = key.substringAfterLast(".")
                        demoPreferencesRepository.getDataStorePreferencesFlow()
                        // DataStore deletion is complex, would need repository enhancement
                        _uiState.update { 
                            ResourceState.Error(Exception("Delete operation requires repository enhancement"), 
                                               "Delete operation not fully implemented")
                        }
                    }
                    PreferenceStorageType.PROTO_DATASTORE -> {
                        _uiState.update { 
                            ResourceState.Error(Exception("Proto preferences cannot be individually deleted"), 
                                               "Proto preferences deletion not supported")
                        }
                    }
                }
            } catch (exception: Exception) {
                _uiState.update { 
                    ResourceState.Error(exception, "Failed to delete preference")
                }
            }
        }
    }
    
    private fun updatePreference(key: String, value: String, type: PreferenceType) {
        viewModelScope.launch(ioDispatcher) {
            try {
                val currentData = _uiState.value.getDataOrNull() ?: return@launch
                
                // Update based on selected tab
                when (currentData.selectedTab) {
                    PreferenceStorageType.SHARED_PREFERENCES -> {
                        // For SharedPreferences updates, we'd need more complex logic to determine
                        // which file and handle different value types properly
                        _uiState.update { 
                            ResourceState.Error(Exception("Update operation not supported for SharedPreferences in this demo"), 
                                               "Update not supported for SharedPreferences")
                        }
                    }
                    PreferenceStorageType.PREFERENCES_DATASTORE -> {
                        // For DataStore updates, delegate to repository methods
                        when (type) {
                            PreferenceType.STRING -> demoPreferencesRepository.updateDataStorePreference(key, value)
                            PreferenceType.BOOLEAN -> demoPreferencesRepository.updateDataStorePreference(key, value.toBoolean())
                            PreferenceType.NUMBER -> {
                                if (value.contains(".")) {
                                    demoPreferencesRepository.updateDataStorePreference(key, value.toFloat())
                                } else {
                                    demoPreferencesRepository.updateDataStorePreference(key, value.toInt())
                                }
                            }
                            PreferenceType.PROTO -> {
                                _uiState.update { 
                                    ResourceState.Error(Exception("Proto type not supported in DataStore"), 
                                                       "Proto type not supported")
                                }
                            }
                        }
                        // The reactive flow will automatically update the UI state
                    }
                    PreferenceStorageType.PROTO_DATASTORE -> {
                        // For Proto updates, use specific proto methods
                        when (key) {
                            "username" -> demoPreferencesRepository.updateProtoUsername(value)
                            "theme_preference" -> demoPreferencesRepository.updateProtoTheme(value)
                            "analytics_enabled" -> demoPreferencesRepository.updateProtoAnalytics(value.toBoolean())
                            else -> {
                                _uiState.update { 
                                    ResourceState.Error(Exception("Update for key '$key' not supported in Proto DataStore"), 
                                                       "Proto update not supported for this key")
                                }
                            }
                        }
                        // The reactive flow will automatically update the UI state
                    }
                }
                
            } catch (exception: Exception) {
                _uiState.update { 
                    ResourceState.Error(exception, "Failed to update preference: ${exception.message}")
                }
            }
        }
    }
    
    private fun clearError() {
        val currentData = _uiState.value.getDataOrNull()
        if (currentData != null) {
            _uiState.update { ResourceState.Success(currentData) }
        } else {
            onEvent(PreferencesEvent.LoadPreferences)
        }
    }
}