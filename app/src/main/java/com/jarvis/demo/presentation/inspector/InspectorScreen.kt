package com.jarvis.demo.presentation.inspector

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.jarvis.core.designsystem.component.DSCard
import com.jarvis.core.designsystem.component.DSCircularProgressIndicator
import com.jarvis.core.designsystem.component.DSPullToRefresh
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.presentation.components.ResourceStateContent
import com.jarvis.core.presentation.navigation.ActionRegistry
import com.jarvis.core.presentation.state.ResourceState
import com.jarvis.demo.R
import com.jarvis.demo.data.repository.ApiCallResult
import com.jarvis.demo.presentation.inspector.InspectorUiData.Companion.mockInspectorUiData

@Composable
fun InspectorScreen(
    modifier: Modifier = Modifier,
    viewModel: InspectorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Register the action callback when the screen is composed
    DisposableEffect(viewModel) {
        ActionRegistry.registerAction(InspectorGraph.Inspector.actionKey) {
            viewModel.onEvent(InspectorEvent.AddRandomApiCall)
        }
        onDispose {
            ActionRegistry.unregisterAction(InspectorGraph.Inspector.actionKey)
        }
    }

    InspectorScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}
@Composable
private fun InspectorScreen(
    uiState: InspectorUiState,
    onEvent: (InspectorEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        DSText(
            text = stringResource(R.string.inspector_description),
            style = DSJarvisTheme.typography.body.medium,
            color = DSJarvisTheme.colors.extra.black,
            modifier = Modifier.padding(DSJarvisTheme.spacing.m)
        )

        ResourceStateContent(
            resourceState = uiState,
            modifier = Modifier.weight(1f),
            onRetry = { onEvent(InspectorEvent.RefreshCalls) },
            onDismiss = { onEvent(InspectorEvent.ClearError) },
            loadingMessage = stringResource(R.string.generating_requests),
            emptyMessage = stringResource(R.string.no_api_calls),
            emptyActionText = stringResource(R.string.inspector_reload),
            onEmptyAction = { onEvent(InspectorEvent.PerformInitialApiCalls) }
        ) { uiData ->
            DSPullToRefresh(
                isRefreshing = uiData.isRefreshing,
                onRefresh = { onEvent(InspectorEvent.RefreshCalls) }
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m)
                ) {
                    item { Spacer(Modifier.height(DSJarvisTheme.spacing.xs)) }

                    items(uiData.apiCalls) { apiCall ->
                        ApiCallItem(
                            modifier = Modifier.padding(horizontal = DSJarvisTheme.spacing.m),
                            apiCall = apiCall
                        )
                    }

                    item { Spacer(Modifier.height(DSJarvisTheme.spacing.m)) }
                }
            }
        }
    }
}

@Composable
private fun ApiCallItem(
    apiCall: ApiCallResult,
    modifier: Modifier = Modifier
) {
    DSCard(
        modifier = modifier.fillMaxWidth(),
        shape = DSJarvisTheme.shapes.m,
        elevation = DSJarvisTheme.elevations.level2
    ) {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.s)
        ) {
            // URL and Method
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    DSText(
                        text = apiCall.url,
                        style = DSJarvisTheme.typography.body.medium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2
                    )
                    
                    DSText(
                        text = apiCall.host,
                        style = DSJarvisTheme.typography.body.small,
                        color = DSJarvisTheme.colors.neutral.neutral100
                    )
                }
                
                // Status indicator
                StatusIndicator(
                    isSuccess = apiCall.isSuccess,
                    statusCode = apiCall.statusCode
                )
            }
            
            Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.s))
            
            // Method, Time, and Duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // HTTP Method Badge
                Box(
                    modifier = Modifier
                        .background(
                            color = getMethodColor(apiCall.method),
                            shape = RoundedCornerShape(DSJarvisTheme.spacing.xs)
                        )
                        .padding(horizontal = DSJarvisTheme.spacing.s, vertical = DSJarvisTheme.spacing.xs)
                ) {
                    DSText(
                        text = apiCall.method,
                        style = DSJarvisTheme.typography.body.small,
                        color = DSJarvisTheme.colors.extra.white,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.width(DSJarvisTheme.spacing.s))
                
                // Time
                DSText(
                    text = apiCall.timestamp,
                    style = DSJarvisTheme.typography.body.small,
                    color = DSJarvisTheme.colors.neutral.neutral80
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Duration
                DSText(
                    text = "${apiCall.duration}ms",
                    style = DSJarvisTheme.typography.body.small,
                    color = DSJarvisTheme.colors.neutral.neutral80,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Progress bar for duration visualization
            if (apiCall.duration > 0) {
                Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.xs))
                
                val maxDuration = 2000f // 2 seconds max for visualization
                val progress = (apiCall.duration / maxDuration).coerceAtMost(1f)
                
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(DSJarvisTheme.dimensions.xs)
                        .clip(DSJarvisTheme.shapes.xs),
                    color = if (apiCall.isSuccess) {
                        DSJarvisTheme.colors.success.success60
                    } else {
                        DSJarvisTheme.colors.error.error60
                    }
                )
            }
            
            // Error message if any
            apiCall.error?.let { error ->
                Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.xs))
                DSText(
                    text = error,
                    style = DSJarvisTheme.typography.body.small,
                    color = DSJarvisTheme.colors.error.error60
                )
            }
        }
    }
}

@Composable
private fun StatusIndicator(
    isSuccess: Boolean,
    statusCode: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.xs)
    ) {
        Box(
            modifier = Modifier
                .size(DSJarvisTheme.dimensions.s)
                .background(
                    color = if (isSuccess) {
                        DSJarvisTheme.colors.success.success60
                    } else {
                        DSJarvisTheme.colors.error.error60
                    },
                    shape = CircleShape
                )
        )
        
        if (statusCode > 0) {
            DSText(
                text = statusCode.toString(),
                style = DSJarvisTheme.typography.body.small,
                color = if (isSuccess) {
                    DSJarvisTheme.colors.success.success60
                } else {
                    DSJarvisTheme.colors.error.error60
                },
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun getMethodColor(method: String): Color {
    return when (method.uppercase()) {
        "GET" -> Color(0xFF2563EB) // Blue
        "POST" -> Color(0xFF16A34A) // Green
        "PUT" -> Color(0xFFF59E0B) // Orange
        "PATCH" -> Color(0xFF8B5CF6) // Purple
        "DELETE" -> Color(0xFFDC2626) // Red
        else -> Color(0xFF6B7280) // Gray
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
fun InspectorScreenLoadingPreview() {
    DSJarvisTheme {
        InspectorScreen(
            uiState = ResourceState.Loading,
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Empty State")
@Composable
fun InspectorScreenEmptyPreview() {
    DSJarvisTheme {
        InspectorScreen(
            uiState = ResourceState.Success(InspectorUiData(apiCalls = emptyList())),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "With Data")
@Composable
fun InspectorScreenWithDataPreview() {
    DSJarvisTheme {
        InspectorScreen(
            uiState = ResourceState.Success(mockInspectorUiData),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Error State")
@Composable
fun InspectorScreenErrorPreview() {
    DSJarvisTheme {
        InspectorScreen(
            uiState = ResourceState.Error(
                RuntimeException("Network error"),
                "Failed to load API calls"
            ),
            onEvent = {}
        )
    }
}

// Dark mode previews
@Preview(
    showBackground = true, 
    name = "Loading Dark", 
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun InspectorScreenLoadingDarkPreview() {
    DSJarvisTheme(darkTheme = true) {
        InspectorScreen(
            uiState = ResourceState.Loading,
            onEvent = {}
        )
    }
}

@Preview(
    showBackground = true, 
    name = "With Data Dark", 
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun InspectorScreenWithDataDarkPreview() {
    DSJarvisTheme(darkTheme = true) {
        InspectorScreen(
            uiState = ResourceState.Success(InspectorUiData.mockInspectorUiData),
            onEvent = {}
        )
    }
}