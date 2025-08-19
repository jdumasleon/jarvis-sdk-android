package com.jarvis.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jarvis.core.designsystem.R
import com.jarvis.core.designsystem.component.DSButton
import com.jarvis.core.designsystem.component.DSButtonStyle
import com.jarvis.core.designsystem.component.DSCircularProgressIndicator
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.presentation.state.ResourceState

/**
 * Reusable loading content component
 */
@Composable
fun LoadingContent(
    modifier: Modifier = Modifier,
    message: String? = null
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DSCircularProgressIndicator()
            message?.let {
                DSText(
                    text = it,
                    style = DSJarvisTheme.typography.body.medium,
                    textAlign = TextAlign.Center,
                    color = DSJarvisTheme.colors.neutral.neutral100
                )
            }
        }
    }
}

/**
 * Reusable empty content component
 */
@Composable
fun EmptyContent(
    message: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DSText(
            text = message,
            style = DSJarvisTheme.typography.body.large,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(32.dp)
        )
        if (actionText != null && onAction != null) {
            DSButton(
                text = actionText,
                style = DSButtonStyle.PRIMARY,
                onClick = onAction
            )
        }
    }
}

/**
 * Reusable error content component
 */
@Composable
fun ErrorContent(
    error: Throwable,
    modifier: Modifier = Modifier,
    message: String? = null,
    onRetry: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DSText(
            text = message ?: "Something went wrong",
            style = DSJarvisTheme.typography.heading.large,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        DSText(
            text = error.message ?: "Unknown error",
            style = DSJarvisTheme.typography.body.medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (onRetry != null) {
            DSButton(
                text = stringResource(R.string.ds_retry),
                style = DSButtonStyle.PRIMARY,
                onClick = onRetry
            )
        }
        
        if (onDismiss != null) {
            Spacer(modifier = Modifier.height(8.dp))
            DSButton(
                text = stringResource(com.jarvis.core.designsystem.R.string.ds_dismiss),
                style = DSButtonStyle.SECONDARY,
                onClick = onDismiss
            )
        }
    }
}

/**
 * Generic resource state content handler
 */
@Composable
fun <T> ResourceStateContent(
    resourceState: ResourceState<T>,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    loadingMessage: String? = null,
    emptyMessage: String = "No data available",
    emptyActionText: String? = null,
    onEmptyAction: (() -> Unit)? = null,
    content: @Composable (T) -> Unit
) {
    when (resourceState) {
        is ResourceState.Idle -> {
            EmptyContent(
                message = emptyMessage,
                modifier = modifier,
                actionText = emptyActionText,
                onAction = onEmptyAction
            )
        }
        is ResourceState.Loading -> {
            LoadingContent(
                modifier = modifier,
                message = loadingMessage
            )
        }
        is ResourceState.Success -> {
            content(resourceState.data)
        }
        is ResourceState.Error -> {
            ErrorContent(
                error = resourceState.exception,
                message = resourceState.message,
                modifier = modifier,
                onRetry = onRetry,
                onDismiss = onDismiss
            )
        }
    }
}

// Preview components
@Preview(showBackground = true)
@Composable
fun LoadingContentPreview() {
    DSJarvisTheme {
        LoadingContent(message = "Loading data...")
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyContentPreview() {
    DSJarvisTheme {
        EmptyContent(
            message = "No items found",
            actionText = "Refresh",
            onAction = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorContentPreview() {
    DSJarvisTheme {
        ErrorContent(
            error = RuntimeException("Network connection failed"),
            message = "Failed to load data",
            onRetry = { },
            onDismiss = { }
        )
    }
}