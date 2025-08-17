package com.jarvis.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import com.jarvis.core.designsystem.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jarvis.core.designsystem.theme.DSJarvisTheme

/**
 * DSJarvis Pull-to-Refresh component
 * 
 * @param isRefreshing Whether the refresh is currently active
 * @param onRefresh Callback invoked when user triggers refresh
 * @param modifier Modifier for the component
 * @param content The content to display inside the pull-to-refresh container
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DSPullToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    PullToRefreshBox(
        modifier = modifier.fillMaxSize(),
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        content = {
            content()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "DSPullToRefresh Preview")
@Composable
private fun DSPullToRefreshPreview() {
    DSJarvisTheme {
        DSPullToRefresh(
            isRefreshing = false,
            onRefresh = { }
        ) {
            Box(
                modifier = Modifier.padding(16.dp)
            ) {
                DSText(
                    text = stringResource(R.string.ds_pull_to_refresh),
                    style = DSJarvisTheme.typography.body.large
                )
            }
        }
    }
}