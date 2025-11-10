package com.jarvis.internal.feature.settings.presentation

import androidx.annotation.RestrictTo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.core.internal.common.di.CoroutineDispatcherModule.IoDispatcher
import com.jarvis.core.internal.presentation.state.ResourceState
import com.jarvis.internal.feature.settings.domain.entity.AppInfo
import com.jarvis.internal.feature.settings.domain.entity.SettingsAppInfo
import com.jarvis.internal.feature.settings.domain.entity.Rating
import com.jarvis.internal.feature.settings.domain.entity.SettingsGroup
import com.jarvis.internal.feature.settings.domain.usecase.GetAppInfoUseCase
import com.jarvis.internal.feature.settings.domain.usecase.GetSettingsItemsUseCase
import com.jarvis.internal.feature.settings.domain.usecase.GetSettingsAppInfoUseCase
import com.jarvis.internal.feature.settings.domain.usecase.SubmitRatingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.getOrThrow

/**
 * ViewModel for Settings screen following clean architecture pattern
 */
@HiltViewModel
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class SettingsViewModel @Inject constructor(
    private val getSettingsItemsUseCase: GetSettingsItemsUseCase,
    private val getAppInfoUseCase: GetAppInfoUseCase,
    private val getSettingsAppInfoUseCase: GetSettingsAppInfoUseCase,
    private val submitRatingUseCase: SubmitRatingUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(ResourceState.Idle)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        onEvent(SettingsEvent.LoadSettings)
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.LoadSettings -> loadSettings()
            is SettingsEvent.ShowRatingDialog -> showRatingDialog()
            is SettingsEvent.HideRatingDialog -> hideRatingDialog()
            is SettingsEvent.UpdateRatingStars -> updateRatingStars(event.stars)
            is SettingsEvent.UpdateRatingDescription -> updateRatingDescription(event.description)
            is SettingsEvent.SubmitRating -> submitRating()
            is SettingsEvent.ShowCallingAppDetailsDialog -> showCallingAppDetailsDialog()
            is SettingsEvent.HideCallingAppDetailsDialog -> hideCallingAppDetailsDialog()
        }
    }

    private fun showRatingDialog() {
        updateCurrentData { it.copy(showRatingDialog = true) }
    }

    private fun hideRatingDialog() {
        updateCurrentData {
            it.copy(
                showRatingDialog = false,
                ratingStars = 0,
                ratingDescription = "",
            )
        }
    }

    private fun showCallingAppDetailsDialog() {
        updateCurrentData { it.copy(showCallingAppDetailsDialog = true) }
    }

    private fun hideCallingAppDetailsDialog() {
        updateCurrentData { it.copy(showCallingAppDetailsDialog = false) }
    }

    private fun updateRatingStars(stars: Int) {
        updateCurrentData { it.copy(ratingStars = stars) }
    }

    private fun updateRatingDescription(description: String) {
        updateCurrentData { it.copy(ratingDescription = description) }
    }

    private fun submitRating() {
        val currentData = _uiState.value.getDataOrNull() ?: return
        viewModelScope.launch(ioDispatcher) {
            updateCurrentData { it.copy(isSubmittingRating = true) }

            val rating = Rating(
                stars = currentData.ratingStars,
                description = currentData.ratingDescription,
                version = currentData.settingsAppInfo?.sdkInfo?.version
            )

            submitRatingUseCase(rating).collectLatest { result ->
                result.onSuccess { submissionResult ->
                    updateCurrentData {
                        it.copy(
                            isSubmittingRating = false,
                            showRatingDialog = false,
                            ratingStars = 0,
                            ratingDescription = ""
                        )
                    }
                }.onFailure { exception ->
                    _uiState.update {
                        ResourceState.Error(exception, "Failed to load settings")
                    }
                }

            }
        }
    }

    private fun updateCurrentData(update: (SettingsUiData) -> SettingsUiData) {
        val currentData = _uiState.value.getDataOrNull() ?: return
        _uiState.update { ResourceState.Success(update(currentData)) }
    }

    private fun loadSettings() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { ResourceState.Loading }

            combine(
                getSettingsItemsUseCase()
                    .catch { emit(Result.failure(it)) },
                getAppInfoUseCase()
                    .catch { emit(Result.failure(it)) },
                getSettingsAppInfoUseCase()
                    .catch { emit(Result.failure(it)) }
            ) { settingsRes: Result<List<SettingsGroup>>, appInfoRes: Result<AppInfo>, settingsAppInfoRes: Result<SettingsAppInfo> ->
                when {
                    settingsRes.isFailure -> {
                        val e = settingsRes.exceptionOrNull() ?: Exception("Unknown error")
                        ResourceState.Error(e, "Failed to load settings items")
                    }
                    appInfoRes.isFailure -> {
                        val e = appInfoRes.exceptionOrNull() ?: Exception("Unknown error")
                        ResourceState.Error(e, "Failed to load app info")
                    }
                    settingsAppInfoRes.isFailure -> {
                        val e = settingsAppInfoRes.exceptionOrNull() ?: Exception("Unknown error")
                        ResourceState.Error(e, "Failed to load settings app info")
                    }
                    else -> {
                        val settings = settingsRes.getOrThrow()
                        val appInfo = appInfoRes.getOrThrow()
                        val settingsAppInfo = settingsAppInfoRes.getOrThrow()
                        ResourceState.Success(
                            SettingsUiData(
                                settingsItems = settings,
                                appInfo = appInfo,
                                settingsAppInfo = settingsAppInfo
                            )
                        )
                    }
                }
            }
                .onStart { emit(ResourceState.Loading) }
                .distinctUntilChanged()
                .collect { state ->
                    _uiState.update { state }
                }

        }
    }
}