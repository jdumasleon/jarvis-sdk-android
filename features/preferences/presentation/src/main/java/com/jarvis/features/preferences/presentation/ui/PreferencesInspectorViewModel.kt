package com.jarvis.features.preferences.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class PreferencesInspectorViewModel @Inject constructor(
    private val getPreferencesByStorageTypeUseCase: GetPreferencesByStorageTypeUseCase,
    private val updatePreferenceUseCase: UpdatePreferenceUseCase,
    private val deletePreferenceUseCase: DeletePreferenceUseCase,
    private val addPreferenceUseCase: AddPreferenceUseCase,
    private val clearPreferencesByTypeUseCase: ClearPreferencesByTypeUseCase,
    private val clearAllPreferencesUseCase: ClearAllPreferencesUseCase,
    private val exportPreferencesUseCase: ExportPreferencesUseCase,
    private val importPreferencesUseCase: ImportPreferencesUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PreferencesInspectorUiState())
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
        PreferenceStorageType.values().forEach { storageType ->
            loadPreferencesForType(storageType)
        }
    }
    
    private fun loadPreferencesForType(storageType: PreferenceStorageType) {
        viewModelScope.launch {
            getPreferencesByStorageTypeUseCase(storageType)
                .catch { exception ->
                    when (storageType) {
                        PreferenceStorageType.SHARED_PREFERENCES -> {
                            _uiState.update { 
                                it.copy(sharedPreferencesGroup = it.sharedPreferencesGroup.copy(
                                    isLoading = false,
                                    error = exception.message
                                ))
                            }
                        }
                        PreferenceStorageType.PREFERENCES_DATASTORE -> {
                            _uiState.update { 
                                it.copy(dataStorePreferencesGroup = it.dataStorePreferencesGroup.copy(
                                    isLoading = false,
                                    error = exception.message
                                ))
                            }
                        }
                        PreferenceStorageType.PROTO_DATASTORE -> {
                            _uiState.update { 
                                it.copy(protoDataStoreGroup = it.protoDataStoreGroup.copy(
                                    isLoading = false,
                                    error = exception.message
                                ))
                            }
                        }
                    }
                }
                .collect { group ->
                    when (storageType) {
                        PreferenceStorageType.SHARED_PREFERENCES -> {
                            _uiState.update { it.copy(sharedPreferencesGroup = group) }
                        }
                        PreferenceStorageType.PREFERENCES_DATASTORE -> {
                            _uiState.update { it.copy(dataStorePreferencesGroup = group) }
                        }
                        PreferenceStorageType.PROTO_DATASTORE -> {
                            _uiState.update { it.copy(protoDataStoreGroup = group) }
                        }
                    }
                }
        }
    }
    
    private fun refreshCurrentTab() {
        loadPreferencesForType(_uiState.value.selectedTab)
    }
    
    private fun selectTab(storageType: PreferenceStorageType) {
        _uiState.update { it.copy(selectedTab = storageType) }
    }
    
    private fun updateSearchQuery(query: String) {
        _uiState.update { 
            it.copy(filter = it.filter.copy(searchQuery = query))
        }
    }
    
    private fun updateTypeFilter(type: PreferenceType?) {
        _uiState.update { 
            it.copy(filter = it.filter.copy(typeFilter = type))
        }
    }
    
    private fun updateSystemPreferencesVisibility(show: Boolean) {
        _uiState.update { 
            it.copy(filter = it.filter.copy(showSystemPreferences = show))
        }
    }
    
    private fun selectPreference(preference: com.jarvis.features.preferences.domain.entity.AppPreference) {
        _uiState.update { it.copy(selectedPreference = preference) }
    }
    
    private fun updatePreference(preference: com.jarvis.features.preferences.domain.entity.AppPreference, newValue: Any) {
        viewModelScope.launch {
            try {
                updatePreferenceUseCase(preference, newValue)
                loadPreferencesForType(preference.storageType) // Reload specific type
            } catch (exception: Exception) {
                _uiState.update { 
                    it.copy(globalError = "Failed to update preference: ${exception.message}")
                }
            }
        }
    }
    
    private fun deletePreference(preference: com.jarvis.features.preferences.domain.entity.AppPreference) {
        viewModelScope.launch {
            try {
                deletePreferenceUseCase(preference)
                loadPreferencesForType(preference.storageType) // Reload specific type
            } catch (exception: Exception) {
                _uiState.update { 
                    it.copy(globalError = "Failed to delete preference: ${exception.message}")
                }
            }
        }
    }
    
    private fun addPreference(key: String, value: Any, type: com.jarvis.features.preferences.domain.entity.PreferenceType, storageType: PreferenceStorageType) {
        viewModelScope.launch {
            try {
                addPreferenceUseCase(key, value, type, storageType)
                loadPreferencesForType(storageType) // Reload specific type
            } catch (exception: Exception) {
                _uiState.update { 
                    it.copy(globalError = "Failed to add preference: ${exception.message}")
                }
            }
        }
    }
    
    private fun clearPreferences(storageType: PreferenceStorageType) {
        viewModelScope.launch {
            try {
                clearPreferencesByTypeUseCase(storageType)
                loadPreferencesForType(storageType) // Reload specific type
            } catch (exception: Exception) {
                _uiState.update { 
                    it.copy(globalError = "Failed to clear preferences: ${exception.message}")
                }
            }
        }
    }
    
    private fun exportPreferences(storageType: PreferenceStorageType?) {
        viewModelScope.launch {
            try {
                val exportData = exportPreferencesUseCase(storageType)
                _uiState.update { it.copy(exportData = exportData) }
            } catch (exception: Exception) {
                _uiState.update { 
                    it.copy(globalError = "Failed to export preferences: ${exception.message}")
                }
            }
        }
    }
    
    private fun importPreferences(data: String, targetStorageType: PreferenceStorageType) {
        viewModelScope.launch {
            try {
                val result = importPreferencesUseCase(data, targetStorageType)
                if (result.isSuccess) {
                    _uiState.update { it.copy(importError = null, showImportDialog = false) }
                    loadPreferencesForType(targetStorageType) // Reload specific type
                } else {
                    _uiState.update { 
                        it.copy(importError = "Failed to import: ${result.exceptionOrNull()?.message}")
                    }
                }
            } catch (exception: Exception) {
                _uiState.update { 
                    it.copy(importError = "Failed to import preferences: ${exception.message}")
                }
            }
        }
    }
    
    private fun showAddDialog(show: Boolean) {
        _uiState.update { it.copy(showAddDialog = show) }
    }
    
    private fun showEditDialog(show: Boolean) {
        _uiState.update { it.copy(showEditDialog = show) }
    }
    
    private fun showDeleteDialog(show: Boolean) {
        _uiState.update { it.copy(showDeleteDialog = show) }
    }
    
    private fun showDetailDialog(show: Boolean) {
        _uiState.update { it.copy(showDetailDialog = show) }
    }
    
    private fun showExportDialog(show: Boolean) {
        _uiState.update { it.copy(showExportDialog = show) }
    }
    
    private fun showImportDialog(show: Boolean) {
        _uiState.update { it.copy(showImportDialog = show, importError = null) }
    }
    
    private fun clearError() {
        _uiState.update { it.copy(globalError = null, importError = null) }
    }
}