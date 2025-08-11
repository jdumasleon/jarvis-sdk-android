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
    private val clearAllPreferencesUseCase: ClearAllPreferencesUseCase,
    private val exportPreferencesUseCase: ExportPreferencesUseCase,
    private val importPreferencesUseCase: ImportPreferencesUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<PreferencesUiState>(ResourceState.Idle)
    val uiState: StateFlow<PreferencesUiState> = _uiState.asStateFlow()
    
    init {
        onEvent(PreferencesEvent.LoadAllPreferences)
    }
    
    fun onEvent(event: PreferencesEvent) {
        when (event) {
            is PreferencesEvent.LoadAllPreferences -> loadAllPreferences()
            is PreferencesEvent.RefreshCurrentTab -> refreshCurrentTab()
            is PreferencesEvent.SelectTab -> selectTab(event.storageType)
            is PreferencesEvent.UpdateSearchQuery -> updateSearchQuery(event.query)
            is PreferencesEvent.UpdateTypeFilter -> updateTypeFilter(event.type)
            is PreferencesEvent.UpdateSystemPreferencesVisibility -> updateSystemPreferencesVisibility(event.show)
            is PreferencesEvent.SelectPreference -> selectPreference(event.preference)
            is PreferencesEvent.UpdatePreference -> updatePreference(event.preference, event.newValue)
            is PreferencesEvent.DeletePreference -> deletePreference(event.preference)
            is PreferencesEvent.AddPreference -> addPreference(event.key, event.value, event.type, event.storageType)
            is PreferencesEvent.ClearPreferences -> clearPreferences(event.storageType)
            is PreferencesEvent.ExportPreferences -> exportPreferences(event.storageType)
            is PreferencesEvent.ImportPreferences -> importPreferences(event.data, event.targetStorageType)
            is PreferencesEvent.ShowAddDialog -> showAddDialog(event.show)
            is PreferencesEvent.ShowEditDialog -> showEditDialog(event.show)
            is PreferencesEvent.ShowDeleteDialog -> showDeleteDialog(event.show)
            is PreferencesEvent.ShowDetailDialog -> showDetailDialog(event.show)
            is PreferencesEvent.ShowExportDialog -> showExportDialog(event.show)
            is PreferencesEvent.ShowImportDialog -> showImportDialog(event.show)
            is PreferencesEvent.RefreshPreferences -> loadAllPreferences()
            is PreferencesEvent.ClearError -> clearError()
        }
    }

    private fun loadAllPreferences() {
        viewModelScope.launch {
            _uiState.update { currentState ->
                val isFirstLoad = currentState !is ResourceState.Success
                if (isFirstLoad) {
                    // First load - show initial loading state
                    ResourceState.Success(
                        PreferencesUiData(
                            sharedPreferencesGroup = PreferenceGroup(PreferenceStorageType.SHARED_PREFERENCES, isLoading = true),
                            dataStorePreferencesGroup = PreferenceGroup(PreferenceStorageType.PREFERENCES_DATASTORE, isLoading = true),
                            protoDataStoreGroup = PreferenceGroup(PreferenceStorageType.PROTO_DATASTORE, isLoading = true),
                            selectedTab = PreferenceStorageType.SHARED_PREFERENCES,
                            filter = PreferenceFilter(),
                            isRefreshing = false
                        )
                    )
                } else {
                    // Refresh - show pull-to-refresh state
                    ResourceState.Success(
                        (currentState as ResourceState.Success).data.copy(isRefreshing = true)
                    )
                }
            }

            // Kick off the three loads in parallel
            loadSharedPrefs()
            loadPrefsDataStore()
            loadProtoDataStore()
        }
    }

    private suspend fun fetchGroup(storageType: PreferenceStorageType): PreferenceGroup {
        return try {
            getPreferencesByStorageTypeUseCase(storageType)
                .catch { throw it }
                .let { flow ->
                    var result = PreferenceGroup(storageType)
                    flow.collect { group -> result = group }
                    result
                }
        } catch (e: Exception) {
            PreferenceGroup(
                storageType = storageType,
                preferences = emptyList(),
                isLoading = false,
                error = e.message
            )
        }
    }

    /** Update just one group inside the current Success state */
    private fun updateGroupInState(
        storageType: PreferenceStorageType,
        group: PreferenceGroup
    ) {
        _uiState.update { current ->
            when (current) {
                is ResourceState.Success -> {
                    val updatedData = when (storageType) {
                        PreferenceStorageType.SHARED_PREFERENCES ->
                            current.data.copy(sharedPreferencesGroup = group)
                        PreferenceStorageType.PREFERENCES_DATASTORE ->
                            current.data.copy(dataStorePreferencesGroup = group)
                        PreferenceStorageType.PROTO_DATASTORE ->
                            current.data.copy(protoDataStoreGroup = group)
                    }
                    
                    // Check if all groups are done loading and turn off refresh indicator
                    val allGroupsLoaded = !updatedData.sharedPreferencesGroup.isLoading &&
                                        !updatedData.dataStorePreferencesGroup.isLoading &&
                                        !updatedData.protoDataStoreGroup.isLoading
                    
                    val finalData = if (allGroupsLoaded) {
                        updatedData.copy(isRefreshing = false)
                    } else {
                        updatedData
                    }
                    
                    ResourceState.Success(finalData)
                }
                else -> current
            }
        }
    }

    fun loadSharedPrefs() {
        viewModelScope.launch {
            // optional: set loading flag for this group while keeping others intact
            _uiState.update { current ->
                if (current is ResourceState.Success) {
                    ResourceState.Success(
                        current.data.copy(
                            sharedPreferencesGroup = current.data.sharedPreferencesGroup.copy(isLoading = true, error = null)
                        )
                    )
                } else current
            }

            val group = fetchGroup(PreferenceStorageType.SHARED_PREFERENCES)
            updateGroupInState(PreferenceStorageType.SHARED_PREFERENCES, group)
        }
    }

    fun loadPrefsDataStore() {
        viewModelScope.launch {
            _uiState.update { current ->
                if (current is ResourceState.Success) {
                    ResourceState.Success(
                        current.data.copy(
                            dataStorePreferencesGroup = current.data.dataStorePreferencesGroup.copy(isLoading = true, error = null)
                        )
                    )
                } else current
            }

            val group = fetchGroup(PreferenceStorageType.PREFERENCES_DATASTORE)
            updateGroupInState(PreferenceStorageType.PREFERENCES_DATASTORE, group)
        }
    }

    fun loadProtoDataStore() {
        viewModelScope.launch {
            _uiState.update { current ->
                if (current is ResourceState.Success) {
                    ResourceState.Success(
                        current.data.copy(
                            protoDataStoreGroup = current.data.protoDataStoreGroup.copy(isLoading = true, error = null)
                        )
                    )
                } else current
            }

            val group = fetchGroup(PreferenceStorageType.PROTO_DATASTORE)
            updateGroupInState(PreferenceStorageType.PROTO_DATASTORE, group)
        }
    }

    private fun refreshCurrentTab() {
        val state = _uiState.value
        if (state is ResourceState.Success) {
            when (state.data.selectedTab) {
                PreferenceStorageType.SHARED_PREFERENCES -> loadSharedPrefs()
                PreferenceStorageType.PREFERENCES_DATASTORE -> loadPrefsDataStore()
                PreferenceStorageType.PROTO_DATASTORE -> loadProtoDataStore()
            }
        } else {
            // If we aren't in Success yet, just load all
            loadAllPreferences()
        }
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
                clearAllPreferencesUseCase()
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