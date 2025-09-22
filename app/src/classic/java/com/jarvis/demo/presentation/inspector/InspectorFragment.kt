package com.jarvis.demo.presentation.inspector

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.jarvis.core.internal.presentation.state.ResourceState
import com.jarvis.demo.R
import com.jarvis.demo.data.repository.ApiCallResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InspectorFragment : Fragment() {

    private val viewModel: InspectorViewModel by viewModels()
    
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var apiCallsRecyclerView: RecyclerView
    private lateinit var loadingState: View
    private lateinit var emptyState: View
    private lateinit var errorState: View
    private lateinit var errorMessage: MaterialTextView
    private lateinit var retryButton: MaterialButton
    private lateinit var reloadButton: MaterialButton
    private lateinit var searchEditText: TextInputEditText
    private lateinit var statusChipGroup: ChipGroup
    private lateinit var methodChipGroup: ChipGroup
    
    private lateinit var apiCallsAdapter: ApiCallsAdapter
    
    // Filter state
    private var searchQuery: String = ""
    private var statusFilter: StatusFilter = StatusFilter.ALL
    private var selectedMethod: String? = null
    
    enum class StatusFilter {
        ALL, SUCCESS, ERROR
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_inspector, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupAdapter()
        setupListeners()
        setupFilters()
        observeViewModel()
        
    }

    private fun initializeViews(view: View) {
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        apiCallsRecyclerView = view.findViewById(R.id.api_calls_recycler_view)
        loadingState = view.findViewById(R.id.loading_state)
        emptyState = view.findViewById(R.id.empty_state)
        errorState = view.findViewById(R.id.error_state)
        errorMessage = view.findViewById(R.id.error_message)
        retryButton = view.findViewById(R.id.retry_button)
        reloadButton = view.findViewById(R.id.reload_button)
        searchEditText = view.findViewById(R.id.search_edit_text)
        statusChipGroup = view.findViewById(R.id.status_chip_group)
        methodChipGroup = view.findViewById(R.id.method_chip_group)
        
        // Setup RecyclerView
        apiCallsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupAdapter() {
        apiCallsAdapter = ApiCallsAdapter { apiCall ->
            viewModel.onEvent(InspectorEvent.CallSelected(apiCall))
        }
        apiCallsRecyclerView.adapter = apiCallsAdapter
    }

    private fun setupListeners() {
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.onEvent(InspectorEvent.RefreshCalls)
        }
        
        retryButton.setOnClickListener {
            viewModel.onEvent(InspectorEvent.ClearError)
        }
        
        reloadButton.setOnClickListener {
            viewModel.onEvent(InspectorEvent.PerformInitialApiCalls)
        }
        
        // Search functionality
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s?.toString()?.trim() ?: ""
                applyFilters()
            }
        })
    }

    private fun setupFilters() {
        // Status filter - allow multiple selection or none for ALL
        statusChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            statusFilter = when {
                checkedIds.contains(R.id.chip_success) && !checkedIds.contains(R.id.chip_error) -> StatusFilter.SUCCESS
                checkedIds.contains(R.id.chip_error) && !checkedIds.contains(R.id.chip_success) -> StatusFilter.ERROR
                checkedIds.contains(R.id.chip_status_all) -> StatusFilter.ALL
                else -> StatusFilter.ALL
            }
            applyFilters()
        }
        
        // Method filter - single selection
        methodChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            selectedMethod = when {
                checkedIds.contains(R.id.chip_get) -> "GET"
                checkedIds.contains(R.id.chip_post) -> "POST" 
                checkedIds.contains(R.id.chip_put) -> "PUT"
                checkedIds.contains(R.id.chip_delete) -> "DELETE"
                else -> null // All methods
            }
            applyFilters()
        }
    }


    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUI(state)
                }
            }
        }
    }

    private fun updateUI(state: InspectorUiState) {
        when (state) {
            is ResourceState.Loading -> {
                showLoadingState()
            }
            is ResourceState.Success -> {
                val data = state.data
                swipeRefreshLayout.isRefreshing = data.isRefreshing
                
                if (data.apiCalls.isEmpty() && !data.isPerformingCalls) {
                    showEmptyState()
                } else {
                    showContentState()
                    applyFilters(data.apiCalls)
                }
            }
            is ResourceState.Error -> {
                showErrorState(state.message ?: "Unknown error occurred")
            }
            is ResourceState.Idle -> {
                showLoadingState()
            }
        }
    }

    private fun showLoadingState() {
        loadingState.visibility = View.VISIBLE
        emptyState.visibility = View.GONE
        errorState.visibility = View.GONE
        apiCallsRecyclerView.visibility = View.GONE
        swipeRefreshLayout.isRefreshing = false
    }

    private fun showEmptyState() {
        loadingState.visibility = View.GONE
        emptyState.visibility = View.VISIBLE
        errorState.visibility = View.GONE
        apiCallsRecyclerView.visibility = View.GONE
        swipeRefreshLayout.isRefreshing = false
    }

    private fun showErrorState(message: String) {
        loadingState.visibility = View.GONE
        emptyState.visibility = View.GONE
        errorState.visibility = View.VISIBLE
        apiCallsRecyclerView.visibility = View.GONE
        errorMessage.text = message
        swipeRefreshLayout.isRefreshing = false
    }

    private fun showContentState() {
        loadingState.visibility = View.GONE
        emptyState.visibility = View.GONE
        errorState.visibility = View.GONE
        apiCallsRecyclerView.visibility = View.VISIBLE
    }

    private fun applyFilters(apiCalls: List<ApiCallResult>? = null) {
        val currentState = viewModel.uiState.value
        val calls = apiCalls ?: (currentState as? ResourceState.Success)?.data?.apiCalls ?: return
        
        val filteredCalls = calls.filter { apiCall ->
            // Apply search filter
            val matchesSearch = if (searchQuery.isEmpty()) {
                true
            } else {
                apiCall.url.contains(searchQuery, ignoreCase = true) ||
                apiCall.host.contains(searchQuery, ignoreCase = true) ||
                apiCall.method.contains(searchQuery, ignoreCase = true) ||
                apiCall.error?.contains(searchQuery, ignoreCase = true) == true
            }
            
            // Apply status filter
            val matchesStatus = when (statusFilter) {
                StatusFilter.ALL -> true
                StatusFilter.SUCCESS -> apiCall.isSuccess
                StatusFilter.ERROR -> !apiCall.isSuccess
            }
            
            // Apply method filter
            val matchesMethod = selectedMethod?.let { method ->
                apiCall.method.uppercase() == method
            } ?: true
            
            matchesSearch && matchesStatus && matchesMethod
        }
        
        apiCallsAdapter.submitList(filteredCalls)
        
        // Show empty state if no results after filtering
        if (filteredCalls.isEmpty() && calls.isNotEmpty()) {
            showEmptyState()
        } else if (filteredCalls.isNotEmpty()) {
            showContentState()
        }
    }
}