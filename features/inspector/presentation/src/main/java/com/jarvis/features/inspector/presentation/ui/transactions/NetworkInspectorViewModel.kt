package com.jarvis.features.inspector.presentation.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.core.presentation.state.ResourceState
import com.jarvis.features.inspector.domain.entity.NetworkTransaction
import com.jarvis.features.inspector.domain.repository.NetworkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NetworkInspectorViewModel @Inject constructor(
    private val networkRepository: NetworkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NetworkInspectorUiState>(ResourceState.Idle)
    val uiState: StateFlow<NetworkInspectorUiState> = _uiState.asStateFlow()

    init {
        onEvent(NetworkInspectorEvent.LoadTransactions)
    }

    fun onEvent(event: NetworkInspectorEvent) {
        when (event) {
            is NetworkInspectorEvent.LoadTransactions -> loadTransactions()
            is NetworkInspectorEvent.SearchQueryChanged -> updateSearchQuery(event.query)
            is NetworkInspectorEvent.MethodFilterChanged -> updateMethodFilter(event.method)
            is NetworkInspectorEvent.StatusFilterChanged -> updateStatusFilter(event.status)
            is NetworkInspectorEvent.TransactionSelected -> selectTransaction(event.transaction)
            is NetworkInspectorEvent.ClearAllTransactions -> clearAllTransactions()
            is NetworkInspectorEvent.DeleteTransaction -> deleteTransaction(event.transactionId)
            is NetworkInspectorEvent.ShowClearConfirmation -> showClearConfirmation(event.show)
            is NetworkInspectorEvent.RefreshTransactions -> refreshTransactions()
            is NetworkInspectorEvent.ClearError -> clearError()
        }
    }

    private fun loadTransactions() {
        _uiState.update { ResourceState.Loading }
        viewModelScope.launch {
            try {
                networkRepository.getAllTransactions().collect { transactions ->
                    val filteredTransactions = applyFilters(transactions)
                    val uiData = NetworkInspectorUiData(
                        transactions = filteredTransactions,
                        searchQuery = _uiState.value.getDataOrNull()?.searchQuery ?: "",
                        selectedMethod = _uiState.value.getDataOrNull()?.selectedMethod,
                        selectedStatus = _uiState.value.getDataOrNull()?.selectedStatus,
                        availableMethods = transactions.map { it.request.method.name }.distinct(),
                        availableStatuses = transactions.map { it.status.name }.distinct()
                    )
                    _uiState.update { ResourceState.Success(uiData) }
                }
            } catch (exception: Exception) {
                _uiState.update { ResourceState.Error(exception, "Failed to load transactions") }
            }
        }
    }

    private fun updateSearchQuery(query: String) {
        val currentData = _uiState.value.getDataOrNull() ?: return
        val updatedData = currentData.copy(searchQuery = query)
        _uiState.update { ResourceState.Success(updatedData) }
        refreshFilters()
    }

    private fun updateMethodFilter(method: String?) {
        val currentData = _uiState.value.getDataOrNull() ?: return
        val updatedData = currentData.copy(selectedMethod = method)
        _uiState.update { ResourceState.Success(updatedData) }
        refreshFilters()
    }

    private fun updateStatusFilter(status: String?) {
        val currentData = _uiState.value.getDataOrNull() ?: return
        val updatedData = currentData.copy(selectedStatus = status)
        _uiState.update { ResourceState.Success(updatedData) }
        refreshFilters()
    }

    private fun selectTransaction(transaction: NetworkTransaction) {
        val currentData = _uiState.value.getDataOrNull() ?: return
        val updatedData = currentData.copy(selectedTransaction = transaction)
        _uiState.update { ResourceState.Success(updatedData) }
    }

    private fun clearAllTransactions() {
        _uiState.update { ResourceState.Loading }
        viewModelScope.launch {
            try {
                networkRepository.deleteAllTransactions()
                loadTransactions()
            } catch (exception: Exception) {
                _uiState.update { ResourceState.Error(exception, "Failed to clear transactions") }
            }
        }
    }

    private fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            try {
                networkRepository.deleteTransaction(transactionId)
                // Data will be updated automatically through the Flow
            } catch (exception: Exception) {
                _uiState.update { ResourceState.Error(exception, "Failed to delete transaction") }
            }
        }
    }

    private fun showClearConfirmation(show: Boolean) {
        val currentData = _uiState.value.getDataOrNull() ?: return
        val updatedData = currentData.copy(showClearConfirmation = show)
        _uiState.update { ResourceState.Success(updatedData) }
    }

    private fun refreshTransactions() {
        loadTransactions()
    }

    private fun clearError() {
        val currentData = _uiState.value.getDataOrNull()
        if (currentData != null) {
            _uiState.update { ResourceState.Success(currentData) }
        } else {
            loadTransactions()
        }
    }

    private fun refreshFilters() {
        viewModelScope.launch {
            try {
                networkRepository.getAllTransactions().collect { allTransactions ->
                    val currentData = _uiState.value.getDataOrNull() ?: return@collect
                    val filteredTransactions = applyFilters(allTransactions)
                    val updatedData = currentData.copy(transactions = filteredTransactions)
                    _uiState.update { ResourceState.Success(updatedData) }
                }
            } catch (exception: Exception) {
                _uiState.update { ResourceState.Error(exception, "Failed to apply filters") }
            }
        }
    }

    private fun applyFilters(transactions: List<NetworkTransaction>): List<NetworkTransaction> {
        val currentData = _uiState.value.getDataOrNull() ?: return transactions
        var filteredTransactions = transactions

        // Apply search filter
        if (currentData.searchQuery.isNotBlank()) {
            filteredTransactions = filteredTransactions.filter { transaction ->
                transaction.request.url.contains(currentData.searchQuery, ignoreCase = true) ||
                transaction.request.method.name.contains(currentData.searchQuery, ignoreCase = true) ||
                transaction.response?.statusCode.toString().contains(currentData.searchQuery, ignoreCase = true)
            }
        }

        // Apply method filter
        if (currentData.selectedMethod != null) {
            filteredTransactions = filteredTransactions.filter { transaction ->
                transaction.request.method.name == currentData.selectedMethod
            }
        }

        // Apply status filter
        if (currentData.selectedStatus != null) {
            filteredTransactions = filteredTransactions.filter { transaction ->
                transaction.status.name == currentData.selectedStatus
            }
        }

        return filteredTransactions
    }
}