package com.jarvis.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ActivityShakingJarvisModeViewModel @Inject constructor(
    private val preferencesDataStoreManager: PreferencesDataStoreService
) : ViewModel() {

    fun initializeEnvironmentsList(environmentsList: List<Pair<String, String>>) {
        viewModelScope.launch {
            val currentEnvironmentList = preferencesDataStoreManager.environmentsList.first()
            if (currentEnvironmentList.isEmpty() || currentEnvironmentList != environmentsList) {
                preferencesDataStoreManager.setEnvironmentsList(environmentsList)
            }
            val currentFirstEnvironment: Pair<String, String> = preferencesDataStoreManager.firstEnvironment.first()
            if (currentFirstEnvironment.first.isEmpty()) {
                preferencesDataStoreManager.setFirstEnvironment(environmentsList.first())
            }
        }
    }
}