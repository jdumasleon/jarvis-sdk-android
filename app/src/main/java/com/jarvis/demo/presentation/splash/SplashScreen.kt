package com.jarvis.demo.presentation.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jarvis.core.presentation.components.ResourceStateContent
import com.jarvis.core.presentation.state.ResourceState
import com.jarvis.core.designsystem.component.DSBackground
import com.jarvis.core.designsystem.component.DSLinearProgressIndicator
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.component.DynamicOrbCanvas
import com.jarvis.core.designsystem.component.StateConfig
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.demo.R

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Handle state changes
    LaunchedEffect(uiState) {
        uiState.getDataOrNull()?.let { data ->
            if (!data.showSplash) {
                onSplashFinished()
            }
        }
    }
    
    SplashScreen(
        modifier = modifier,
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
internal fun SplashScreen(
    uiState: SplashUiState,
    onEvent: (SplashEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    ResourceStateContent(
        resourceState = uiState,
        modifier = modifier,
        onRetry = { onEvent(SplashEvent.StartSplash) },
        onDismiss = { onEvent(SplashEvent.ClearError) },
        loadingMessage = null, // We'll show custom loading UI
        emptyMessage = "Welcome to Jarvis",
        emptyActionText = "Start",
        onEmptyAction = { onEvent(SplashEvent.StartSplash) }
    ) { uiData ->
        SplashContent(
            uiData = uiData,
            modifier = modifier
        )
    }
}

@Composable
private fun SplashContent(
    uiData: SplashUiData,
    modifier: Modifier = Modifier
) {
    DSBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DSJarvisTheme.spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Main AI Orb Animation
            Box(
                modifier = Modifier.size(400.dp),
                contentAlignment = Alignment.Center
            ) {
                DynamicSplashOrb(progress = uiData.loadingProgress)
            }
            
            Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.xl))
            
            // App name
            DSText(
                text = uiData.appName,
                style = DSJarvisTheme.typography.heading.heading2,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = DSJarvisTheme.colors.primary.primary60
            )
            
            Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.m))
            
            // Version
            DSText(
                text = uiData.appVersion,
                style = DSJarvisTheme.typography.body.small,
                textAlign = TextAlign.Center,
                color = DSJarvisTheme.colors.neutral.neutral40
            )
            
            Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.xl))
            
            // Loading progress
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Progress bar
                DSLinearProgressIndicator(
                    progress = uiData.loadingProgress,
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(4.dp),
                    color = DSJarvisTheme.colors.primary.primary60,
                    backgroundColor = DSJarvisTheme.colors.neutral.neutral20
                )
                
                Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.m))
                
                // Loading message
                DSText(
                    text = uiData.initializationMessage,
                    style = DSJarvisTheme.typography.body.medium,
                    textAlign = TextAlign.Center,
                    color = DSJarvisTheme.colors.neutral.neutral60
                )
                
                Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.s))
                
                // Progress percentage
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DSText(
                        text = "${(uiData.loadingProgress * 100).toInt()}%",
                        style = DSJarvisTheme.typography.body.small,
                        color = DSJarvisTheme.colors.neutral.neutral40
                    )
                }
            }
        }
    }
}

@Composable
private fun DynamicSplashOrb(progress: Float) {
    Box(
        modifier = Modifier.size(400.dp),
        contentAlignment = Alignment.Center
    ) {
        DynamicOrbCanvas(
            config = StateConfig(
                name = "Initializing",
                colors = listOf(
                    DSJarvisTheme.colors.primary.primary40,
                    DSJarvisTheme.colors.primary.primary60,
                    DSJarvisTheme.colors.primary.primary80
                ),
                speed = 1.2f + (progress * 0.8f), // Speed increases with progress
                morphIntensity = 2.0f + (progress * 1.0f) // More intense morphing as it loads
            ),
            modifier = Modifier.fillMaxSize()
        )
    }
}

// Preview Templates
@Preview(showBackground = true, name = "Splash - Loading")
@Composable
fun SplashScreenLoadingPreview() {
    DSJarvisTheme {
        SplashScreen(
            uiState = ResourceState.Success(SplashUiData.mockSplashUiData),
            onEvent = { }
        )
    }
}

@Preview(showBackground = true, name = "Splash - Starting")
@Composable
fun SplashScreenStartingPreview() {
    DSJarvisTheme {
        val uiData = SplashUiData(
            showSplash = true,
            loadingProgress = 0.1f,
            initializationMessage = "Starting up..."
        )
        SplashScreen(
            uiState = ResourceState.Success(uiData),
            onEvent = { }
        )
    }
}

@Preview(showBackground = true, name = "Splash - Completed")
@Composable
fun SplashScreenCompletedPreview() {
    DSJarvisTheme {
        SplashScreen(
            uiState = ResourceState.Success(SplashUiData.mockSplashCompletedUiData),
            onEvent = { }
        )
    }
}

@Preview(showBackground = true, name = "Splash - Error")
@Composable
fun SplashScreenErrorPreview() {
    DSJarvisTheme {
        SplashScreen(
            uiState = ResourceState.Error(
                RuntimeException("Initialization failed"),
                "Failed to initialize the application"
            ),
            onEvent = { }
        )
    }
}

@Preview(showBackground = true, name = "Splash - Idle")
@Composable
fun SplashScreenIdlePreview() {
    DSJarvisTheme {
        SplashScreen(
            uiState = ResourceState.Idle,
            onEvent = { }
        )
    }
}

// Dark mode previews
@Preview(
    showBackground = true, 
    name = "Splash - Loading Dark", 
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun SplashScreenLoadingDarkPreview() {
    DSJarvisTheme(darkTheme = true) {
        SplashScreen(
            uiState = ResourceState.Success(SplashUiData.mockSplashUiData),
            onEvent = { }
        )
    }
}

@Preview(
    showBackground = true, 
    name = "Splash - Starting Dark", 
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun SplashScreenStartingDarkPreview() {
    DSJarvisTheme(darkTheme = true) {
        val uiData = SplashUiData(
            showSplash = true,
            loadingProgress = 0.1f,
            initializationMessage = "Starting up..."
        )
        SplashScreen(
            uiState = ResourceState.Success(uiData),
            onEvent = { }
        )
    }
}

@Preview(
    showBackground = true, 
    name = "Splash - Error Dark", 
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun SplashScreenErrorDarkPreview() {
    DSJarvisTheme(darkTheme = true) {
        SplashScreen(
            uiState = ResourceState.Error(
                exception = Exception("Initialization failed"),
                message = "Failed to initialize the application"
            ),
            onEvent = { }
        )
    }
}