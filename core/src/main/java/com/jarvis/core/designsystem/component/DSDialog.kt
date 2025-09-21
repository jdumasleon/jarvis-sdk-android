package com.jarvis.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jarvis.core.designsystem.theme.DSJarvisTheme

@Composable
fun DSDialog(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    text: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable () -> Unit = {},
    properties: DialogProperties = DialogProperties(),
) {
    DSDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        text = text,
        buttons = {
            confirmButton()
            dismissButton()
        },
        properties = properties,
        title = title,
    )
}

@Composable
fun DSDialog(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    text: @Composable () -> Unit,
    buttons: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties,
    ) {
        DialogContent(
            title = title,
            text = text,
            buttons = buttons,
            modifier = modifier,
        )
    }
}

@Composable
private fun DialogContent(
    title: @Composable () -> Unit,
    text: @Composable () -> Unit,
    buttons: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = DSJarvisTheme.shapes.m,
        color = DSJarvisTheme.colors.extra.surface,
        tonalElevation = DSJarvisTheme.elevations.level2,
    ) {
        Column(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .verticalScroll(rememberScrollState())
                .padding(DSJarvisTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m),
        ) {
            Header(
                title = title,
                description = text,
            )

            Column(verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)) {
                buttons()
            }
        }
    }
}

@Composable
private fun Header(
    title: @Composable () -> Unit,
    description: @Composable () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.xxs),
    ) {
        CompositionLocalProvider(LocalTextStyle provides DSJarvisTheme.typography.body.medium) {
            title()
        }

        CompositionLocalProvider(LocalTextStyle provides DSJarvisTheme.typography.body.small) {
            Box(
                modifier = Modifier
                    .padding(top = DSJarvisTheme.spacing.xxs)
                    .padding(bottom = DSJarvisTheme.spacing.s)
                    .fillMaxWidth(),
            ) {
                description()
            }
        }
    }
}

@Preview(showBackground = true, name = "DSDialog full")
@Composable
private fun DSDialogFullPreview() {
    DSJarvisTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            DSDialog(
                onDismissRequest = {},
                text = { DSText("Description") },
                confirmButton = {
                    DSButton(
                        text = "Confirm",
                        onClick = {},
                        size = DSButtonSize.SMALL,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                dismissButton = {
                    DSButton(
                        text = "Dismiss",
                        style = DSButtonStyle.TEXT,
                        size = DSButtonSize.SMALL,
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                title = { DSText("Title") },
            )
        }
    }
}

@Preview(showBackground = true, name = "DSDialog full with multiple actions")
@Composable
private fun DialogMultipleActionsFullPreview() {
    DSJarvisTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            DSDialog(
                onDismissRequest = {},
                title = { DSText("Title") },
                text = { DSText("Description") },
                buttons = {
                    DSButton(
                        text = "Confirm",
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                    )
                    DSButton(
                        text = "Delete",
                        style = DSButtonStyle.TEXT,
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                    )
                    DSButton(
                        text = "Dismiss",
                        style = DSButtonStyle.LINK,
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
            )
        }
    }
}