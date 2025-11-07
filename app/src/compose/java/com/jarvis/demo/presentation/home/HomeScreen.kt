package com.jarvis.demo.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.jarvis.core.internal.presentation.components.ResourceStateContent
import com.jarvis.core.internal.presentation.state.ResourceState
import com.jarvis.core.internal.designsystem.component.DSButton
import com.jarvis.core.internal.designsystem.component.DSButtonStyle
import com.jarvis.core.internal.designsystem.component.DSCard
import com.jarvis.core.internal.designsystem.component.DSTag
import com.jarvis.core.internal.designsystem.component.DSTagStyle
import com.jarvis.core.internal.designsystem.component.DSText
import com.jarvis.core.internal.designsystem.theme.DSJarvisTheme
import com.jarvis.core.internal.navigation.ActionRegistry
import com.jarvis.demo.R

@Composable
internal fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Register the refresh action when the screen is composed
    DisposableEffect(viewModel) {
        ActionRegistry.registerAction(HomeGraph.Home.actionKey) {
            viewModel.onEvent(HomeEvent.RefreshData)
        }
        onDispose {
            ActionRegistry.unregisterAction(HomeGraph.Home.actionKey)
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            DSCard(
                modifier = Modifier.fillMaxWidth(),
                shape = DSJarvisTheme.shapes.m,
                elevation = DSJarvisTheme.elevations.none,
            ) {
                // Contenido principal
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DSJarvisTheme.spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // App name and title
                    DSText(
                        text = uiData.welcomeMessage,
                        style = DSJarvisTheme.typography.heading.large,
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
                        color = DSJarvisTheme.colors.neutral.neutral100
                    )

                    Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.m))

                    // Version text
                    DSText(
                        text = uiData.version,
                        style = DSJarvisTheme.typography.body.small,
                        textAlign = TextAlign.Center,
                        color = DSJarvisTheme.colors.neutral.neutral80
                    )

                    Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.m))

                    // Instructions
                    DSText(
                        text = uiData.shakeInstructions,
                        style = DSJarvisTheme.typography.body.medium,
                        textAlign = TextAlign.Center,
                        color = DSJarvisTheme.colors.neutral.neutral100,
                    )

                    Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.l))

                    // Toggle button
                    DSButton(
                        text = if (uiData.isJarvisActive) "Deactivate Jarvis" else "Activate Jarvis",
                        style = if (uiData.isJarvisActive) DSButtonStyle.DESTRUCTIVE else DSButtonStyle.PRIMARY,
                        onClick = { onEvent(HomeEvent.ToggleJarvisMode) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Last refresh time
                    uiData.lastRefreshTime?.let { timestamp ->
                        Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.s))
                        DSText(
                            text = stringResource(
                                R.string.last_updated,
                                java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                                    .format(java.util.Date(timestamp))
                            ),
                            style = DSJarvisTheme.typography.body.small,
                            color = DSJarvisTheme.colors.neutral.neutral80
                        )
                    }
                }
            }

            if (uiData.isJarvisActive) {
                DSTag(
                    tag = "ACTIVE",
                    style = DSTagStyle.Info,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(DSJarvisTheme.spacing.m)
                )
            }
        }
    }
}

// Preview Templates - Comprehensive State Coverage
@Preview(showBackground = true, name = "Loading - Initial State")
@Composable
fun HomeScreenLoadingPreview() {
    DSJarvisTheme {
        HomeScreen(
            uiState = ResourceState.Loading,
            onEvent = { }
        )
    }
}

@Preview(showBackground = true, name = "Idle - Welcome State")
@Composable
fun HomeScreenIdlePreview() {
    DSJarvisTheme {
        HomeScreen(
            uiState = ResourceState.Idle,
            onEvent = { }
        )
    }
}

@Preview(showBackground = true, name = "Success - Jarvis Active")
@Composable
fun HomeScreenActiveJarvisPreview() {
    DSJarvisTheme {
        HomeScreen(
            uiState = ResourceState.Success(HomeUiData.mockHomeUiData),
            onEvent = { }
        )
    }
}

@Preview(showBackground = true, name = "Success - Jarvis Inactive")
@Composable
fun HomeScreenInactiveJarvisPreview() {
    DSJarvisTheme {
        val inactiveData = HomeUiData.mockHomeUiData.copy(isJarvisActive = false)
        HomeScreen(
            uiState = ResourceState.Success(inactiveData),
            onEvent = { }
        )
    }
}

@Preview(showBackground = true, name = "Success - Fresh Start")
@Composable
fun HomeScreenFreshStartPreview() {
    DSJarvisTheme {
        val freshData = HomeUiData(
            welcomeMessage = "Welcome to Jarvis Demo",
            description = "Your network inspection and debugging companion",
            version = "v1.0.0",
            isJarvisActive = false,
            lastRefreshTime = null
        )
        HomeScreen(
            uiState = ResourceState.Success(freshData),
            onEvent = { }
        )
    }
}

@Preview(showBackground = true, name = "Success - Recently Updated")
@Composable
fun HomeScreenRecentlyUpdatedPreview() {
    DSJarvisTheme {
        val recentData = HomeUiData.mockHomeUiData.copy(
            lastRefreshTime = System.currentTimeMillis() - 30000, // 30 seconds ago
            isJarvisActive = true
        )
        HomeScreen(
            uiState = ResourceState.Success(recentData),
            onEvent = { }
        )
    }
}

@Preview(showBackground = true, name = "Error - Network Failure")
@Composable
fun HomeScreenNetworkErrorPreview() {
    DSJarvisTheme {
        HomeScreen(
            uiState = ResourceState.Error(
                RuntimeException("Network connection failed"),
                "Failed to load home data"
            ),
            onEvent = { }
        )
    }
}

@Preview(showBackground = true, name = "Error - Server Error")
@Composable
fun HomeScreenServerErrorPreview() {
    DSJarvisTheme {
        HomeScreen(
            uiState = ResourceState.Error(
                RuntimeException("Server returned 500"),
                "Server is temporarily unavailable"
            ),
            onEvent = { }
        )
    }
}

@Preview(showBackground = true, name = "Dark Theme - Active Jarvis", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenDarkThemePreview() {
    DSJarvisTheme {
        HomeScreen(
            uiState = ResourceState.Success(HomeUiData.mockHomeUiData),
            onEvent = { }
        )
    }
}