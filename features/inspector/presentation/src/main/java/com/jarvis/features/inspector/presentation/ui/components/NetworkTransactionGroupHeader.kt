package com.jarvis.features.inspector.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.theme.DSJarvisTheme

/**
 * Header component for grouping network transactions by date
 */
@Composable
internal fun NetworkTransactionGroupHeader(
    title: String,
    transactionCount: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = DSJarvisTheme.spacing.m,
                vertical = DSJarvisTheme.spacing.xs
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DSText(
                text = title,
                style = DSJarvisTheme.typography.body.medium,
                fontWeight = FontWeight.Thin,
                color = DSJarvisTheme.colors.neutral.neutral100
            )
            
            DSText(
                text = "$transactionCount ${if (transactionCount == 1) "request" else "requests"}",
                style = DSJarvisTheme.typography.body.small,
                fontWeight = FontWeight.Thin,
                color = DSJarvisTheme.colors.neutral.neutral100
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NetworkTransactionGroupHeaderPreview() {
    DSJarvisTheme {
        Column {
            NetworkTransactionGroupHeader(
                title = "Today",
                transactionCount = 12
            )
            Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.s))
            NetworkTransactionGroupHeader(
                title = "Yesterday",
                transactionCount = 5
            )
            Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.s))
            NetworkTransactionGroupHeader(
                title = "December 15, 2024",
                transactionCount = 3
            )
        }
    }
}