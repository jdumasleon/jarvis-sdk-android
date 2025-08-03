package com.jarvis.features.inspector.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.features.inspector.domain.entity.NetworkTransaction
import com.jarvis.features.inspector.domain.repository.NetworkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NetworkInspectorViewModel @Inject constructor(
    private val networkRepository: NetworkRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedMethod = MutableStateFlow<String?>(null)
    val selectedMethod: StateFlow<String?> = _selectedMethod.asStateFlow()

    private val _selectedStatus = MutableStateFlow<String?>(null)
    val selectedStatus: StateFlow<String?> = _selectedStatus.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val transactions: StateFlow<List<NetworkTransaction>> = combine(
        _searchQuery,
        _selectedMethod,
        _selectedStatus
    ) { query, method, status ->
        Triple(query, method, status)
    }.let { filtersFlow ->
        combine(
            filtersFlow,
            networkRepository.getAllTransactions()
        ) { (query, method, status), allTransactions ->
            var filteredTransactions = allTransactions

            // Apply search filter
            if (query.isNotBlank()) {
                filteredTransactions = filteredTransactions.filter { transaction ->
                    transaction.request.url.contains(query, ignoreCase = true) ||
                    transaction.request.method.name.contains(query, ignoreCase = true) ||
                    transaction.response?.statusCode.toString().contains(query, ignoreCase = true)
                }
            }

            // Apply method filter
            if (method != null) {
                filteredTransactions = filteredTransactions.filter { transaction ->
                    transaction.request.method.name == method
                }
            }

            // Apply status filter
            if (status != null) {
                filteredTransactions = filteredTransactions.filter { transaction ->
                    transaction.status.name == status
                }
            }

            filteredTransactions
        }
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateMethodFilter(method: String?) {
        _selectedMethod.value = method
    }

    fun updateStatusFilter(status: String?) {
        _selectedStatus.value = status
    }

    fun clearAllTransactions() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                networkRepository.deleteAllTransactions()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            try {
                networkRepository.deleteTransaction(transactionId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}