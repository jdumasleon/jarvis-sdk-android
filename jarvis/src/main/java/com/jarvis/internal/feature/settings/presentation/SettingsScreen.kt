package com.jarvis.internal.feature.settings.presentation

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.presentation.components.ResourceStateContent
import com.jarvis.core.presentation.state.ResourceState
import com.jarvis.internal.feature.settings.domain.entity.SettingsAction
import com.jarvis.internal.feature.settings.presentation.SettingsUiData.Companion.mockSettingsUiData
import com.jarvis.internal.feature.settings.presentation.components.SettingsGroup
import com.jarvis.internal.feature.settings.presentation.components.RatingBottomSheet
import com.jarvis.internal.feature.settings.presentation.components.RatingData
import androidx.core.net.toUri
import com.jarvis.core.presentation.utils.openUrl
import com.jarvis.core.presentation.utils.shareUrl
import com.jarvis.internal.feature.settings.presentation.components.AppDetailsBottomSheet

/**
 * Settings screen route with state management
 */
@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    onNavigateToInspector: () -> Unit,
    onNavigateToPreferences: () -> Unit,
    onNavigateToLogging: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    SettingsScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier,
        onAction = { action ->
            when (action) {
                is SettingsAction.OpenUrl -> openUrl(action.url, context)
                is SettingsAction.ShareApp -> shareUrl(action.url, context)
                is SettingsAction.OpenEmail -> {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = "mailto:".toUri()
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(action.email))
                        putExtra(Intent.EXTRA_SUBJECT, action.subject)
                    }
                    context.startActivity(intent)
                }
                is SettingsAction.NavigateToInspector -> onNavigateToInspector()
                is SettingsAction.NavigateToPreferences -> onNavigateToPreferences()
                is SettingsAction.NavigateToLogging -> onNavigateToLogging()
                is SettingsAction.RateApp -> viewModel.onEvent(SettingsEvent.ShowRatingDialog)
                is SettingsAction.ShowCallingAppDetails -> viewModel.onEvent(SettingsEvent.ShowCallingAppDetailsDialog)
                is SettingsAction.Version -> { }
            }
        }

    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(
    uiState: SettingsUiState,
    onEvent: (SettingsEvent) -> Unit,
    onAction: (SettingsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(top = DSJarvisTheme.spacing.m)
            .fillMaxSize()
    ) {
        ResourceStateContent(
            resourceState = uiState,
            onRetry = { onEvent(SettingsEvent.LoadSettings) },
            loadingMessage = "Loading settings...",
            emptyMessage = "No settings found",
            emptyActionText = "Load Settings",
            onEmptyAction = { onEvent(SettingsEvent.LoadSettings) }
        ) { uiData ->
            SettingsContent(
                uiData = uiData,
                onEvent = onEvent,
                onAction = onAction
            )
        }
    }
}

@Composable
private fun SettingsContent(
    uiData: SettingsUiData,
    onEvent: (SettingsEvent) -> Unit,
    onAction: (SettingsAction) -> Unit,
    modifier: Modifier = Modifier,

    ) {
    val listState = rememberLazyListState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DSJarvisTheme.colors.extra.background)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = DSJarvisTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.l),
            reverseLayout = false,
            userScrollEnabled = true
        ) {
            item(key = "spacer_top", contentType = "spacer") {
                Spacer(modifier = Modifier.height(DSJarvisTheme.dimensions.xs))
            }

            items(
                items = uiData.settingsItems,
                key = { settings -> "setting_${settings.title}" },
                contentType = { "setting" }
            ) { settingGroup ->
                SettingsGroup(
                    group = settingGroup,
                    onAction = onAction
                )
            }

            item(key = "spacer_bottom", contentType = "spacer") {
                Spacer(modifier = Modifier.height(DSJarvisTheme.dimensions.xs))
            }
        }
        
        // Rating Bottom Sheet
        if (uiData.showRatingDialog) {
            RatingBottomSheet(
                ratingData = RatingData(
                    stars = uiData.ratingStars,
                    description = uiData.ratingDescription,
                    isSubmitting = uiData.isSubmittingRating
                ),
                onRatingChange = { stars ->
                    onEvent(SettingsEvent.UpdateRatingStars(stars))
                },
                onDescriptionChange = { description ->
                    onEvent(SettingsEvent.UpdateRatingDescription(description))
                },
                onSubmit = {
                    onEvent(SettingsEvent.SubmitRating)
                },
                onCancel = {
                    onEvent(SettingsEvent.HideRatingDialog)
                }
            )
        }

        // Calling App Details Bottom Sheet
        if (uiData.showCallingAppDetailsDialog && uiData.settingsAppInfo != null) {
            AppDetailsBottomSheet(
                hostAppInfo = uiData.settingsAppInfo.hostAppInfo,
                onCancel = {
                    onEvent(SettingsEvent.HideCallingAppDetailsDialog)
                }
            )
        }
    }
}

@Preview(showBackground = true, name = "Settings Screen - Loading")
@Composable
private fun SettingsScreenLoadingPreview() {
    DSJarvisTheme {
        SettingsScreen(
            uiState = ResourceState.Loading,
            onEvent = {},
            onAction = {}
        )
    }
}

@Preview(showBackground = true, name = "Settings Screen - Success")
@Composable
private fun SettingsScreenSuccessPreview() {
    DSJarvisTheme {
        SettingsScreen(
            uiState = ResourceState.Success(mockSettingsUiData),
            onEvent = {},
            onAction = {}
        )
    }
}

@Preview(showBackground = true, name = "Settings Screen - Error")  
@Composable
private fun SettingsScreenErrorPreview() {
    DSJarvisTheme {
        SettingsScreen(
            uiState = ResourceState.Error(
                exception = RuntimeException("Network error"),
                message = "Failed to load settings"
            ),
            onEvent = {},
            onAction = {}
        )
    }
}