package com.jarvis.features.preferences.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.core.presentation.state.ResourceState
import com.jarvis.features.preferences.domain.entity.AppPreference
import com.jarvis.features.preferences.domain.entity.PreferenceFilter
import com.jarvis.features.preferences.domain.entity.PreferenceGroup
import com.jarvis.features.preferences.domain.entity.PreferenceStorageType
import com.jarvis.features.preferences.domain.entity.PreferenceType
import com.jarvis.features.preferences.domain.usecase.AddPreferenceUseCase
import com.jarvis.features.preferences.domain.usecase.ClearAllPreferencesUseCase
import com.jarvis.features.preferences.domain.usecase.ClearPreferencesByTypeUseCase
import com.jarvis.features.preferences.domain.usecase.DeletePreferenceUseCase
import com.jarvis.features.preferences.domain.usecase.ExportPreferencesUseCase
import com.jarvis.features.preferences.domain.usecase.GetPreferencesByStorageTypeUseCase
import com.jarvis.features.preferences.domain.usecase.ImportPreferencesUseCase
import com.jarvis.features.preferences.domain.usecase.UpdatePreferenceUseCase
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
    private val getPreferencesByStorageTypeUseCase: GetPreferencesByStorageTypeUseCase,
    private val updatePreferenceUseCase: UpdatePreferenceUseCase,
    private val deletePreferenceUseCase: DeletePreferenceUseCase,
    private val addPreferenceUseCase: AddPreferenceUseCase,
    private val clearPreferencesByTypeUseCase: ClearPreferencesByTypeUseCase,
    private val clearAllPreferencesUseCase: ClearAllPreferencesUseCase,
    private val exportPreferencesUseCase: ExportPreferencesUseCase,
    private val importPreferencesUseCase: ImportPreferencesUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<PreferencesInspectorUiState>(ResourceState.Idle)
    val uiState: StateFlow<PreferencesInspectorUiState> = _uiState.asStateFlow()
    
    init {
        onEvent(PreferencesInspectorEvent.LoadAllPreferences)
    }
    
    fun onEvent(event: PreferencesInspectorEvent) {
        when (event) {
            is PreferencesInspectorEvent.LoadAllPreferences -> loadAllPreferences()
            is PreferencesInspectorEvent.RefreshCurrentTab -> refreshCurrentTab()
            is PreferencesInspectorEvent.SelectTab -> selectTab(event.storageType)
            is PreferencesInspectorEvent.UpdateSearchQuery -> updateSearchQuery(event.query)
            is PreferencesInspectorEvent.UpdateTypeFilter -> updateTypeFilter(event.type)
            is PreferencesInspectorEvent.UpdateSystemPreferencesVisibility -> updateSystemPreferencesVisibility(event.show)
            is PreferencesInspectorEvent.SelectPreference -> selectPreference(event.preference)
            is PreferencesInspectorEvent.UpdatePreference -> updatePreference(event.preference, event.newValue)
            is PreferencesInspectorEvent.DeletePreference -> deletePreference(event.preference)
            is PreferencesInspectorEvent.AddPreference -> addPreference(event.key, event.value, event.type, event.storageType)
            is PreferencesInspectorEvent.ClearPreferences -> clearPreferences(event.storageType)
            is PreferencesInspectorEvent.ExportPreferences -> exportPreferences(event.storageType)
            is PreferencesInspectorEvent.ImportPreferences -> importPreferences(event.data, event.targetStorageType)
            is PreferencesInspectorEvent.ShowAddDialog -> showAddDialog(event.show)
            is PreferencesInspectorEvent.ShowEditDialog -> showEditDialog(event.show)
            is PreferencesInspectorEvent.ShowDeleteDialog -> showDeleteDialog(event.show)
            is PreferencesInspectorEvent.ShowDetailDialog -> showDetailDialog(event.show)
            is PreferencesInspectorEvent.ShowExportDialog -> showExportDialog(event.show)
            is PreferencesInspectorEvent.ShowImportDialog -> showImportDialog(event.show)
            is PreferencesInspectorEvent.ClearError -> clearError()
        }
    }
    
    private fun loadAllPreferences() {
        viewModelScope.launch {
            try {
                _uiState.update { ResourceState.Loading }
                
                // Load all preference groups
                val sharedPrefsGroup = loadPreferencesGroupSync(PreferenceStorageType.SHARED_PREFERENCES)
                val dataStoreGroup = loadPreferencesGroupSync(PreferenceStorageType.PREFERENCES_DATASTORE)
                val protoDataStoreGroup = loadPreferencesGroupSync(PreferenceStorageType.PROTO_DATASTORE)
                
                val uiData = PreferencesInspectorUiData(
                    sharedPreferencesGroup = sharedPrefsGroup,
                    dataStorePreferencesGroup = dataStoreGroup,
                    protoDataStoreGroup = protoDataStoreGroup,
                    selectedTab = PreferenceStorageType.SHARED_PREFERENCES,
                    filter = PreferenceFilter()
                )
                
                _uiState.update { ResourceState.Success(uiData) }
            } catch (exception: Exception) {
                _uiState.update { 
                    ResourceState.Error(exception, "Failed to load preferences")
                }
            }
        }
    }
    
    private suspend fun loadPreferencesGroupSync(storageType: PreferenceStorageType): PreferenceGroup {
        return try {
            getPreferencesByStorageTypeUseCase(storageType)
                .catch { throw it }
                .let { flow ->
                    // Get the first emission or empty group
                    var result = PreferenceGroup(storageType)
                    flow.collect { group ->
                        result = group
                    }
                    result
                }
        } catch (exception: Exception) {
            PreferenceGroup(
                storageType = storageType,
                preferences = emptyList(),
                isLoading = false,
                error = exception.message
            )
        }
    }
    
    private fun refreshCurrentTab() {
        loadAllPreferences()
    }
    
    private fun selectTab(storageType: PreferenceStorageType) {
        _uiState.update { currentState ->
            when (currentState) {
                is ResourceState.Success -> {
                    ResourceState.Success(currentState.data.copy(selectedTab = storageType))
                }
                else -> currentState
            }
        }
    }
    
    private fun updateSearchQuery(query: String) {
        _uiState.update { currentState ->
            when (currentState) {
                is ResourceState.Success -> {
                    ResourceState.Success(
                        currentState.data.copy(
                            filter = currentState.data.filter.copy(searchQuery = query)
                        )
                    )
                }
                else -> currentState
            }
        }
    }
    
    private fun updateTypeFilter(type: PreferenceType?) {
        _uiState.update { currentState ->
            when (currentState) {
                is ResourceState.Success -> {
                    ResourceState.Success(
                        currentState.data.copy(
                            filter = currentState.data.filter.copy(typeFilter = type)
                        )
                    )
                }
                else -> currentState
            }
        }
    }
    
    private fun updateSystemPreferencesVisibility(show: Boolean) {
        _uiState.update { currentState ->
            when (currentState) {
                is ResourceState.Success -> {
                    ResourceState.Success(
                        currentState.data.copy(
                            filter = currentState.data.filter.copy(showSystemPreferences = show)
                        )
                    )
                }
                else -> currentState
            }
        }
    }
    
    private fun selectPreference(preference: AppPreference) {
        _uiState.update { currentState ->
            when (currentState) {
                is ResourceState.Success -> {
                    ResourceState.Success(
                        currentState.data.copy(selectedPreference = preference)
                    )
                }
                else -> currentState
            }
        }
    }
    
    private fun updatePreference(preference: AppPreference, newValue: Any) {
        viewModelScope.launch {
            try {
                updatePreferenceUseCase(preference, newValue)
                loadAllPreferences() // Reload all preferences
            } catch (exception: Exception) {
                _uiState.update { 
                    ResourceState.Error(exception, "Failed to update preference")
                }
            }
        }
    }
    
    private fun deletePreference(preference: AppPreference) {
        viewModelScope.launch {
            try {
                deletePreferenceUseCase(preference)
                loadAllPreferences() // Reload all preferences
            } catch (exception: Exception) {
                _uiState.update { 
                    ResourceState.Error(exception, "Failed to delete preference")
                }
            }
        }
    }
    
    private fun addPreference(key: String, value: Any, type: PreferenceType, storageType: PreferenceStorageType) {
        viewModelScope.launch {
            try {
                addPreferenceUseCase(key, value, type, storageType)
                loadAllPreferences() // Reload all preferences
            } catch (exception: Exception) {
                _uiState.update { 
                    ResourceState.Error(exception, "Failed to add preference")
                }
            }
        }
    }
    
    private fun clearPreferences(storageType: PreferenceStorageType) {
        viewModelScope.launch {
            try {
                clearPreferencesByTypeUseCase(storageType)
                loadAllPreferences() // Reload all preferences
            } catch (exception: Exception) {
                _uiState.update { 
                    ResourceState.Error(exception, "Failed to clear preferences")
                }
            }
        }
    }
    
    private fun exportPreferences(storageType: PreferenceStorageType?) {
        viewModelScope.launch {
            try {
                val exportData = exportPreferencesUseCase(storageType)
                _uiState.update { currentState ->
                    when (currentState) {
                        is ResourceState.Success -> {
                            ResourceState.Success(
                                currentState.data.copy(exportData = exportData)
                            )
                        }
                        else -> currentState
                    }
                }
            } catch (exception: Exception) {
                _uiState.update { 
                    ResourceState.Error(exception, "Failed to export preferences")
                }
            }
        }
    }
    
    private fun importPreferences(data: String, targetStorageType: PreferenceStorageType) {
        viewModelScope.launch {
            try {
                val result = importPreferencesUseCase(data, targetStorageType)
                if (result.isSuccess) {
                    _uiState.update { currentState ->
                        when (currentState) {
                            is ResourceState.Success -> {
                                ResourceState.Success(
                                    currentState.data.copy(
                                        importError = null, 
                                        showImportDialog = false
                                    )
                                )
                            }
                            else -> currentState
                        }
                    }
                    loadAllPreferences() // Reload all preferences
                } else {
                    _uiState.update { currentState ->
                        when (currentState) {
                            is ResourceState.Success -> {
                                ResourceState.Success(
                                    currentState.data.copy(
                                        importError = "Failed to import: ${result.exceptionOrNull()?.message}"
                                    )
                                )
                            }
                            else -> currentState
                        }
                    }
                }
            } catch (exception: Exception) {
                _uiState.update { 
                    ResourceState.Error(exception, "Failed to import preferences")
                }
            }
        }
    }
    
    private fun showAddDialog(show: Boolean) {
        _uiState.update { currentState ->
            when (currentState) {
                is ResourceState.Success -> {
                    ResourceState.Success(currentState.data.copy(showAddDialog = show))
                }
                else -> currentState
            }
        }
    }
    
    private fun showEditDialog(show: Boolean) {
        _uiState.update { currentState ->
            when (currentState) {
                is ResourceState.Success -> {
                    ResourceState.Success(currentState.data.copy(showEditDialog = show))
                }
                else -> currentState
            }
        }
    }
    
    private fun showDeleteDialog(show: Boolean) {
        _uiState.update { currentState ->
            when (currentState) {
                is ResourceState.Success -> {
                    ResourceState.Success(currentState.data.copy(showDeleteDialog = show))
                }
                else -> currentState
            }
        }
    }
    
    private fun showDetailDialog(show: Boolean) {
        _uiState.update { currentState ->
            when (currentState) {
                is ResourceState.Success -> {
                    ResourceState.Success(currentState.data.copy(showDetailDialog = show))
                }
                else -> currentState
            }
        }
    }
    
    private fun showExportDialog(show: Boolean) {
        _uiState.update { currentState ->
            when (currentState) {
                is ResourceState.Success -> {
                    ResourceState.Success(currentState.data.copy(showExportDialog = show))
                }
                else -> currentState
            }
        }
    }
    
    private fun showImportDialog(show: Boolean) {
        _uiState.update { currentState ->
            when (currentState) {
                is ResourceState.Success -> {
                    ResourceState.Success(
                        currentState.data.copy(
                            showImportDialog = show, 
                            importError = null
                        )
                    )
                }
                else -> currentState
            }
        }
    }
    
    private fun clearError() {
        _uiState.update { currentState ->
            when (currentState) {
                is ResourceState.Error -> ResourceState.Idle
                is ResourceState.Success -> {
                    ResourceState.Success(
                        currentState.data.copy(
                            globalError = null, 
                            importError = null
                        )
                    )
                }
                else -> currentState
            }
        }
    }
}