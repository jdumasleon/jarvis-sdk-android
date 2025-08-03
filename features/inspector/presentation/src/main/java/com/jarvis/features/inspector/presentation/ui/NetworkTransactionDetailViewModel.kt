package com.jarvis.features.inspector.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.features.inspector.domain.entity.NetworkTransaction
import com.jarvis.features.inspector.domain.repository.NetworkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NetworkTransactionDetailViewModel @Inject constructor(
    private val networkRepository: NetworkRepository
) : ViewModel() {

    private val _transaction = MutableStateFlow<NetworkTransaction?>(null)
    val transaction: StateFlow<NetworkTransaction?> = _transaction.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    fun loadTransaction(transactionId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                networkRepository.getTransaction(transactionId).collect { transaction ->
                    _transaction.value = transaction
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
                // Handle error
            }
        }
    }

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun deleteTransaction() {
        val transactionToDelete = _transaction.value
        if (transactionToDelete != null) {
            viewModelScope.launch {
                try {
                    networkRepository.deleteTransaction(transactionToDelete.id)
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }
}