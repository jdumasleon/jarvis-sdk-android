package com.jarvis.demo.presentation.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.core.presentation.state.ResourceState
import com.jarvis.demo.data.preferences.PreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreferencesViewModel @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<PreferencesUiState>(ResourceState.Idle)
    val uiState: StateFlow<PreferencesUiState> = _uiState.asStateFlow()
    
    // Backward compatibility
    val preferencesList: StateFlow<List<PreferenceItem>> = MutableStateFlow(
        _uiState.value.getDataOrNull()?.currentPreferences ?: emptyList()
    ).asStateFlow()
    
    init {
        onEvent(PreferencesEvent.LoadPreferences)
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
        viewModelScope.launch {
            try {
                // Generate sample data for all three tabs
                generateSamplePreferencesForAllTabs()
            } catch (exception: Exception) {
                _uiState.update { 
                    ResourceState.Error(exception, "Failed to load preferences")
                }
            }
        }
    }
    
    private fun generateSamplePreferencesForAllTabs() {
        viewModelScope.launch {
            try {
                // Generate DataStore preferences
                preferencesDataStore.generateSamplePreferences()
                
                // Create mock data for all tabs
                val uiData = PreferencesUiData.mockPreferencesUiData
                _uiState.update { ResourceState.Success(uiData) }
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
        generateRandomPreferencesInternal()
    }
    
    private fun generateRandomPreferencesInternal() {
        viewModelScope.launch {
            try {
                preferencesDataStore.generateSamplePreferences()
                
                // Load the generated preferences
                preferencesDataStore.getAllPreferences()
                    .catch { exception ->
                        _uiState.update { 
                            ResourceState.Error(exception, "Failed to generate preferences")
                        }
                    }
                    .collect { prefItems ->
                        val uiData = PreferencesUiData(dataStorePreferences = prefItems)
                        _uiState.update { ResourceState.Success(uiData) }
                    }
            } catch (exception: Exception) {
                _uiState.update { 
                    ResourceState.Error(exception, "Failed to generate preferences")
                }
            }
        }
    }
    
    private fun refreshPreferences() {
        loadPreferences()
    }
    
    private fun clearAllPreferences() {
        viewModelScope.launch {
            try {
                preferencesDataStore.clearAll()
                val uiData = PreferencesUiData(dataStorePreferences = emptyList())
                _uiState.update { ResourceState.Success(uiData) }
            } catch (exception: Exception) {
                _uiState.update { 
                    ResourceState.Error(exception, "Failed to clear preferences")
                }
            }
        }
    }
    
    private fun deletePreference(key: String) {
        viewModelScope.launch {
            try {
                preferencesDataStore.removePreference(key)
                val currentData = _uiState.value.getDataOrNull() ?: return@launch
                val updatedPreferences = currentData.currentPreferences.filter { it.key != key }
                val updatedData = currentData.copy(dataStorePreferences = updatedPreferences)
                _uiState.update { ResourceState.Success(updatedData) }
            } catch (exception: Exception) {
                _uiState.update { 
                    ResourceState.Error(exception, "Failed to delete preference")
                }
            }
        }
    }
    
    private fun updatePreference(key: String, value: String, type: PreferenceType) {
        viewModelScope.launch {
            try {
                preferencesDataStore.updatePreference(key, value, type)
                
                // Update the list
                val currentData = _uiState.value.getDataOrNull() ?: return@launch
                val updatedList = currentData.currentPreferences.map { pref ->
                    if (pref.key == key) {
                        pref.copy(value = value)
                    } else {
                        pref
                    }
                }.let { list ->
                    // Add new preference if it doesn't exist
                    if (list.none { it.key == key }) {
                        list + PreferenceItem(key, value, type)
                    } else {
                        list
                    }
                }.sortedBy { it.key }
                
                val updatedData = currentData.copy(dataStorePreferences = updatedList)
                _uiState.update { ResourceState.Success(updatedData) }
                
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