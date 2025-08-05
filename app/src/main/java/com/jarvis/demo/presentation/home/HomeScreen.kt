package com.jarvis.demo.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jarvis.core.presentation.components.ResourceStateContent
import com.jarvis.core.presentation.state.ResourceState
import com.jarvis.core.designsystem.component.DSButton
import com.jarvis.core.designsystem.component.DSButtonStyle
import com.jarvis.core.designsystem.component.DSCard
import com.jarvis.core.designsystem.component.DSTag
import com.jarvis.core.designsystem.component.DSTagStyle
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.presentation.navigation.ActionRegistry
import com.jarvis.demo.R

@Composable
internal fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    
    // Register the refresh action when the screen is composed
    DisposableEffect(viewModel) {
        ActionRegistry.registerAction(HomeDestinations.Home.actionKey) {
            viewModel.onEvent(HomeEvent.RefreshData)
        }
        onDispose {
            ActionRegistry.unregisterAction(HomeDestinations.Home.actionKey)
        }
    }
    
    HomeScreen(
        modifier = modifier,
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
internal fun HomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    ResourceStateContent(
        resourceState = uiState,
        modifier = modifier,
        onRetry = { onEvent(HomeEvent.RefreshData) },
        onDismiss = { onEvent(HomeEvent.ClearError) },
        loadingMessage = "Loading home data...",
        emptyMessage = "Welcome to Jarvis Demo",
        emptyActionText = "Get Started",
        onEmptyAction = { onEvent(HomeEvent.RefreshData) }
    ) { uiData ->
        HomeContent(
            uiData = uiData,
            onEvent = onEvent,
            modifier = modifier
        )
    }
}

@Composable
private fun HomeContent(
    uiData: HomeUiData,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(DSJarvisTheme.spacing.l),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Welcome card
        DSCard(
            modifier = Modifier.fillMaxWidth(),
            shape = DSJarvisTheme.shapes.m,
            elevation = DSJarvisTheme.elevations.level3,
        ) {
            Column(
                modifier = Modifier
                    .padding(DSJarvisTheme.spacing.xl)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App name and title
                DSText(
                    text = uiData.welcomeMessage,
                    style = DSJarvisTheme.typography.heading.heading3,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = DSJarvisTheme.colors.primary.primary60
                )
                
                Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.s))
                
                // Description
                DSText(
                    text = uiData.description,
                    style = DSJarvisTheme.typography.body.large,
                    textAlign = TextAlign.Center,
                    color = DSJarvisTheme.colors.neutral.neutral60
                )
                
                Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.xs))
                
                // Version and Status
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DSText(
                        text = uiData.version,
                        style = DSJarvisTheme.typography.body.small,
                        textAlign = TextAlign.Center,
                        color = DSJarvisTheme.colors.neutral.neutral40
                    )
                    
                    if (uiData.isJarvisActive) {
                        Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.xs))
                        DSTag(
                            tag = "ACTIVE",
                            style = DSTagStyle.Primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.l))
                
                // Instructions
                DSText(
                    text = uiData.shakeInstructions,
                    style = DSJarvisTheme.typography.body.medium,
                    textAlign = TextAlign.Center,
                    color = DSJarvisTheme.colors.neutral.neutral80,
                    lineHeight = DSJarvisTheme.typography.body.medium.lineHeight * 1.3f
                )
                
                Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.l))
                
                // Toggle button
                DSButton(
                    text = if (uiData.isJarvisActive) "Deactivate Jarvis" else "Activate Jarvis",
                    style = DSButtonStyle.PRIMARY,
                    onClick = { onEvent(HomeEvent.ToggleJarvisMode) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Last refresh time
                uiData.lastRefreshTime?.let { timestamp ->
                    Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.s))
                    DSText(
                        text = "Last updated: ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(timestamp))}",
                        style = DSJarvisTheme.typography.body.small,
                        color = DSJarvisTheme.colors.neutral.neutral40
                    )
                }
            }
        }
    }
}

// Preview Templates
@Preview(showBackground = true, name = "Home - Success")
@Composable
fun HomeScreenSuccessPreview() {
    DSJarvisTheme {
        HomeScreen(
            uiState = ResourceState.Success(HomeUiData.mockHomeUiData),
            onEvent = { }
        )
    }
}

@Preview(showBackground = true, name = "Home - Loading")
@Composable
fun HomeScreenLoadingPreview() {
    DSJarvisTheme {
        HomeScreen(
            uiState = ResourceState.Loading,
            onEvent = { }
        )
    }
}

@Preview(showBackground = true, name = "Home - Error")
@Composable
fun HomeScreenErrorPreview() {
    DSJarvisTheme {
        HomeScreen(
            uiState = ResourceState.Error(
                RuntimeException("Network error"),
                "Failed to load home data"
            ),
            onEvent = { }
        )
    }
}

@Preview(showBackground = true, name = "Home - Idle")
@Composable
fun HomeScreenIdlePreview() {
    DSJarvisTheme {
        HomeScreen(
            uiState = ResourceState.Idle,
            onEvent = { }
        )
    }
}

@Preview(showBackground = true, name = "Home - Inactive Jarvis")
@Composable
fun HomeScreenInactivePreview() {
    DSJarvisTheme {
        val uiData = HomeUiData.mockHomeUiData.copy(isJarvisActive = false)
        HomeScreen(
            uiState = ResourceState.Success(uiData),
            onEvent = { }
        )
    }
}