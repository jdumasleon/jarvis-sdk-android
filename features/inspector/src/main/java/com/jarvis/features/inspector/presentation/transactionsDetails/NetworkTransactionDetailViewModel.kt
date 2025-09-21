package com.jarvis.features.inspector.presentation.transactionsDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.core.common.di.CoroutineDispatcherModule.IoDispatcher
import com.jarvis.core.presentation.state.ResourceState
import com.jarvis.features.inspector.domain.repository.NetworkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NetworkTransactionDetailViewModel @Inject constructor(
    private val networkRepository: NetworkRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow<NetworkTransactionDetailUiState>(ResourceState.Idle)
    val uiState: StateFlow<NetworkTransactionDetailUiState> = _uiState.asStateFlow()

    fun onEvent(event: NetworkTransactionDetailEvent) {
        when (event) {
            is NetworkTransactionDetailEvent.LoadTransaction -> loadTransaction(event.transactionId)
            is NetworkTransactionDetailEvent.SelectTab -> selectTab(event.tabIndex)
            is NetworkTransactionDetailEvent.ShowDeleteConfirmation -> showDeleteConfirmation(event.show)
            is NetworkTransactionDetailEvent.DeleteTransaction -> deleteTransaction()
            is NetworkTransactionDetailEvent.ClearError -> clearError()
        }
    }

    private fun loadTransaction(transactionId: String) {
        _uiState.update { ResourceState.Loading }
        viewModelScope.launch(ioDispatcher) {
            try {
                networkRepository.getTransaction(transactionId).collect { transaction ->
                    if (transaction != null) {
                        val uiData = NetworkTransactionDetailUiData(
                            transaction = transaction,
                            selectedTab = _uiState.value.getDataOrNull()?.selectedTab ?: 0
                        )
                        _uiState.update { ResourceState.Success(uiData) }
                    } else {
                        _uiState.update { 
                            ResourceState.Error(
                                RuntimeException("Transaction not found"),
                                "Transaction with ID $transactionId was not found"
                            ) 
                        }
                    }
                }
            } catch (exception: Exception) {
                _uiState.update { 
                    ResourceState.Error(exception, "Failed to load transaction details") 
                }
            }
        }
    }

    private fun selectTab(tabIndex: Int) {
        val currentData = _uiState.value.getDataOrNull() ?: return
        val updatedData = currentData.copy(selectedTab = tabIndex)
        _uiState.update { ResourceState.Success(updatedData) }
    }

    private fun showDeleteConfirmation(show: Boolean) {
        val currentData = _uiState.value.getDataOrNull() ?: return
        val updatedData = currentData.copy(showDeleteConfirmation = show)
        _uiState.update { ResourceState.Success(updatedData) }
    }

    private fun deleteTransaction() {
        val currentData = _uiState.value.getDataOrNull() ?: return
        viewModelScope.launch(ioDispatcher) {
            try {
                networkRepository.deleteTransaction(currentData.transaction.id)
                // Transaction deleted successfully - the UI should navigate back
            } catch (exception: Exception) {
                _uiState.update { 
                    ResourceState.Error(exception, "Failed to delete transaction")
                }
            }
        }
    }

    private fun clearError() {
        val currentData = _uiState.value.getDataOrNull()
        if (currentData != null) {
            _uiState.update { ResourceState.Success(currentData) }
        } else {
            _uiState.update { ResourceState.Idle }
        }
    }
}