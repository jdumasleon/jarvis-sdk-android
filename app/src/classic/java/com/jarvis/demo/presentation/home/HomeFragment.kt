package com.jarvis.demo.presentation.home

import android.os.Bundle
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
import com.google.android.material.textview.MaterialTextView
import android.widget.Toast
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import com.jarvis.core.presentation.state.ResourceState
import com.jarvis.demo.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()
    
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var welcomeCard: MaterialCardView
    private lateinit var welcomeTitle: MaterialTextView
    private lateinit var welcomeDescription: MaterialTextView
    private lateinit var versionText: MaterialTextView
    private lateinit var instructionsText: MaterialTextView
    private lateinit var toggleJarvisFabButton: MaterialButton
    private lateinit var lastUpdatedText: MaterialTextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupListeners()
        observeViewModel()
        
        // Initial data load
        viewModel.onEvent(HomeEvent.RefreshData)
    }

    private fun initializeViews(view: View) {
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        welcomeCard = view.findViewById(R.id.welcome_card)
        welcomeTitle = view.findViewById(R.id.welcome_title)
        welcomeDescription = view.findViewById(R.id.welcome_description)
        versionText = view.findViewById(R.id.version_text)
        instructionsText = view.findViewById(R.id.instructions_text)
        toggleJarvisFabButton = view.findViewById(R.id.toggle_jarvis_fab_button)
        lastUpdatedText = view.findViewById(R.id.last_updated_text)
    }

    private fun setupListeners() {
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.onEvent(HomeEvent.RefreshData)
        }
        
        toggleJarvisFabButton.setOnClickListener {
            viewModel.onEvent(HomeEvent.ToggleJarvisMode)
        }
    }
    
    private fun updateJarvisFabButton(isActive: Boolean) {
        if (isActive) {
            // Jarvis FAB is active
            toggleJarvisFabButton.text = getString(R.string.deactivate_jarvis_fab)
            toggleJarvisFabButton.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.error_color)
            )
        } else {
            // Jarvis FAB is inactive
            toggleJarvisFabButton.text = getString(R.string.activate_jarvis_fab)
            toggleJarvisFabButton.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.jarvis_primary_60)
            )
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

    private fun updateUI(state: HomeUiState) {
        val isLoading = state is ResourceState.Loading
        swipeRefreshLayout.isRefreshing = isLoading
        
        when (state) {
            is ResourceState.Success -> {
                val data = state.data
                
                // Update welcome card with data
                welcomeTitle.text = data.welcomeMessage
                welcomeDescription.text = data.description
                versionText.text = data.version
                instructionsText.text = data.shakeInstructions
                
                // Update Jarvis FAB button state from ViewModel
                updateJarvisFabButton(data.isJarvisActive)
                
                // Update last updated time
                data.lastRefreshTime?.let { timestamp ->
                    val timeString = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                        .format(java.util.Date(timestamp))
                    lastUpdatedText.text = getString(R.string.last_updated, timeString)
                    lastUpdatedText.visibility = View.VISIBLE
                } ?: run {
                    lastUpdatedText.visibility = View.GONE
                }
            }
            is ResourceState.Loading -> {
                // Show loading state
                welcomeTitle.text = getString(R.string.welcome_message)
                welcomeDescription.text = getString(R.string.jarvis_description)
                versionText.text = getString(R.string.version)
                instructionsText.text = getString(R.string.shake_instructions)
            }
            is ResourceState.Error -> {
                // Show error state
                welcomeTitle.text = getString(R.string.welcome_message)
                welcomeDescription.text = "Error: ${state.message}"
                versionText.text = getString(R.string.version)
                instructionsText.text = getString(R.string.shake_instructions)
            }
            is ResourceState.Idle -> {
                // Show idle state
                welcomeTitle.text = getString(R.string.welcome_message)
                welcomeDescription.text = getString(R.string.jarvis_description)
                versionText.text = getString(R.string.version)
                instructionsText.text = getString(R.string.shake_instructions)
            }
        }

        toggleJarvisFabButton.isEnabled = !isLoading
    }
}