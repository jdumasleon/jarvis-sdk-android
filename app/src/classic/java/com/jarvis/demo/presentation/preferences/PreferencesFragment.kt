package com.jarvis.demo.presentation.preferences

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
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.jarvis.core.presentation.state.ResourceState
import com.jarvis.demo.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PreferencesFragment : Fragment() {

    private val viewModel: PreferencesViewModel by viewModels()
    
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var preferencesRecyclerView: RecyclerView
    private lateinit var loadingState: View
    private lateinit var emptyState: View
    private lateinit var errorState: View
    private lateinit var contentContainer: View
    private lateinit var errorMessage: MaterialTextView
    private lateinit var retryButton: MaterialButton
    private lateinit var reloadButton: MaterialButton
    private lateinit var searchEditText: TextInputEditText
    private lateinit var storageChipGroup: ChipGroup
    private lateinit var tabHeaderCard: MaterialCardView
    private lateinit var tabTitle: MaterialTextView
    private lateinit var tabDescription: MaterialTextView
    
    private lateinit var preferencesAdapter: PreferencesAdapter
    
    // Filter state
    private var searchQuery: String = ""
    private var selectedStorageType: PreferenceStorageType = PreferenceStorageType.SHARED_PREFERENCES

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_preferences, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupAdapter()
        setupListeners()
        setupStorageTypeFilters()
        observeViewModel()
        
        // Load preferences without generating random data to avoid conflicts
        viewModel.onEvent(PreferencesEvent.LoadPreferences)
    }

    private fun initializeViews(view: View) {
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        preferencesRecyclerView = view.findViewById(R.id.preferences_recycler_view)
        loadingState = view.findViewById(R.id.loading_state)
        emptyState = view.findViewById(R.id.empty_state)
        errorState = view.findViewById(R.id.error_state)
        contentContainer = view.findViewById(R.id.content_container)
        errorMessage = view.findViewById(R.id.error_message)
        retryButton = view.findViewById(R.id.retry_button)
        reloadButton = view.findViewById(R.id.reload_button)
        searchEditText = view.findViewById(R.id.search_edit_text)
        storageChipGroup = view.findViewById(R.id.storage_chip_group)
        tabHeaderCard = view.findViewById(R.id.tab_header_card)
        tabTitle = view.findViewById(R.id.tab_title)
        tabDescription = view.findViewById(R.id.tab_description)
        
        // Setup RecyclerView
        preferencesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupAdapter() {
        preferencesAdapter = PreferencesAdapter(
            onPreferenceValueChanged = { key, value, type ->
                viewModel.onEvent(PreferencesEvent.UpdatePreference(key, value, type))
            },
            onPreferenceClick = { preference ->
                viewModel.onEvent(PreferencesEvent.PreferenceSelected(preference))
            }
        )
        preferencesRecyclerView.adapter = preferencesAdapter
    }

    private fun setupListeners() {
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.onEvent(PreferencesEvent.RefreshPreferences)
        }
        
        retryButton.setOnClickListener {
            viewModel.onEvent(PreferencesEvent.ClearError)
        }
        
        reloadButton.setOnClickListener {
            viewModel.onEvent(PreferencesEvent.LoadPreferences)
        }
        
        // Search functionality
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s?.toString()?.trim() ?: ""
                viewModel.onEvent(PreferencesEvent.SearchQueryChanged(searchQuery))
            }
        })
    }

    private fun setupStorageTypeFilters() {
        storageChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val newType = when (checkedId) {
                R.id.chip_shared_prefs -> PreferenceStorageType.SHARED_PREFERENCES
                R.id.chip_datastore -> PreferenceStorageType.PREFERENCES_DATASTORE
                R.id.chip_proto -> PreferenceStorageType.PROTO_DATASTORE
                else -> return@setOnCheckedStateChangeListener
            }
            viewModel.onEvent(PreferencesEvent.SelectTab(newType))
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

    private fun updateUI(state: PreferencesUiState) {
        when (state) {
            is ResourceState.Loading -> {
                showLoadingState()
            }
            is ResourceState.Success -> {
                val data = state.data
                swipeRefreshLayout.isRefreshing = data.isRefreshing
                
                // Update tab header first
                updateTabHeader(data.selectedTab)
                
                // Then update the list and visibility
                if (data.filteredPreferences.isEmpty()) {
                    showEmptyState()
                    preferencesAdapter.submitList(emptyList()) // Clear the adapter
                } else {
                    showContentState()
                    // Submit list after showing content state to prevent flickering
                    preferencesAdapter.submitList(data.filteredPreferences.toList())
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

    private fun updateTabHeader(storageType: PreferenceStorageType) {
        val (title, description) = when (storageType) {
            PreferenceStorageType.SHARED_PREFERENCES -> Pair(
                "SharedPreferences",
                "Legacy XML-based key-value storage. Synchronous operations, stored in shared_prefs/ directory."
            )
            PreferenceStorageType.PREFERENCES_DATASTORE -> Pair(
                "Preferences DataStore",
                "Modern reactive preferences with type safety. Asynchronous operations built on Flow."
            )
            PreferenceStorageType.PROTO_DATASTORE -> Pair(
                "Proto DataStore", 
                "Structured data storage using Protocol Buffers. Type-safe schema-based storage."
            )
        }
        
        tabTitle.text = title
        tabDescription.text = description

        selectedStorageType = storageType
        val targetId = when (storageType) {
            PreferenceStorageType.SHARED_PREFERENCES    -> R.id.chip_shared_prefs
            PreferenceStorageType.PREFERENCES_DATASTORE -> R.id.chip_datastore
            PreferenceStorageType.PROTO_DATASTORE       -> R.id.chip_proto
        }

        if (storageChipGroup.checkedChipId != targetId) {
            storageChipGroup.check(targetId)
        }
    }

    private fun showLoadingState() {
        loadingState.visibility = View.VISIBLE
        emptyState.visibility = View.GONE
        errorState.visibility = View.GONE
        contentContainer.visibility = View.GONE
        swipeRefreshLayout.isRefreshing = false
    }

    private fun showEmptyState() {
        loadingState.visibility = View.GONE
        emptyState.visibility = View.VISIBLE
        errorState.visibility = View.GONE
        contentContainer.visibility = View.GONE
        swipeRefreshLayout.isRefreshing = false
    }

    private fun showErrorState(message: String) {
        loadingState.visibility = View.GONE
        emptyState.visibility = View.GONE
        errorState.visibility = View.VISIBLE
        contentContainer.visibility = View.GONE
        errorMessage.text = message
        swipeRefreshLayout.isRefreshing = false
    }

    private fun showContentState() {
        loadingState.visibility = View.GONE
        emptyState.visibility = View.GONE
        errorState.visibility = View.GONE
        contentContainer.visibility = View.VISIBLE
    }
}