package com.jarvis.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jarvis.core.designsystem.theme.DSJarvisTheme

/**
 * Design System Bottom Sheet component with title, content, and action buttons.
 * Follows iOS-style bottom sheet design with slide up animation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DSBottomSheet(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    confirmButton: @Composable () -> Unit = {},
    dismissButton: @Composable () -> Unit = {},
    containerColor: Color = DSJarvisTheme.colors.extra.background,
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    windowInsets: androidx.compose.foundation.layout.WindowInsets = BottomSheetDefaults.windowInsets
) {
    DSBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        content = content,
        buttons = {
            confirmButton()
            dismissButton()
        },
        containerColor = containerColor,
        dragHandle = dragHandle,
        windowInsets = windowInsets,
        title = title,
    )
}

/**
 * Design System Bottom Sheet component with custom buttons layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DSBottomSheet(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
    buttons: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = DSJarvisTheme.colors.extra.background,
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    windowInsets: WindowInsets = BottomSheetDefaults.windowInsets
) {
    val bottomSheetState = rememberModalBottomSheetState()
    
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = bottomSheetState,
        modifier = modifier,
        shape = RoundedCornerShape(
            topStart = DSJarvisTheme.dimensions.l,
            topEnd = DSJarvisTheme.dimensions.l,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        ),
        containerColor = containerColor,
        dragHandle = dragHandle
    ) {
        BottomSheetContent(
            title = title,
            content = content,
            buttons = buttons,
        )
    }
}

@Composable
private fun BottomSheetContent(
    title: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
    buttons: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .verticalScroll(rememberScrollState())
            .padding(
                start = DSJarvisTheme.spacing.m,
                end = DSJarvisTheme.spacing.m,
                bottom = DSJarvisTheme.spacing.xl
            ),
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m),
    ) {
        // Title Section
        CompositionLocalProvider(LocalTextStyle provides DSJarvisTheme.typography.title.large) {
            title()
        }

        // Content Section
        CompositionLocalProvider(LocalTextStyle provides DSJarvisTheme.typography.body.medium) {
            content()
        }

        // Buttons Section
        Column(
            verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
        ) {
            buttons()
        }
    }
}

@Preview(showBackground = true, name = "DSBottomSheet with confirm/dismiss")
@Composable
private fun DSBottomSheetPreview() {
    DSJarvisTheme {
        DSBottomSheet(
            onDismissRequest = {},
            title = { DSText("Add New Item") },
            content = {
                DSText("This is the content of the bottom sheet. You can add any composable content here.")
                
                DSTextField(
                    text = "",
                    onValueChange = {},
                    placeholder = "Enter value",
                    title = "Input Field"
                )
            },
            confirmButton = {
                DSButton(
                    text = "Confirm",
                    onClick = {},
                    style = DSButtonStyle.PRIMARY,
                    size = DSButtonSize.MEDIUM,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            dismissButton = {
                DSButton(
                    text = "Cancel",
                    style = DSButtonStyle.SECONDARY,
                    size = DSButtonSize.MEDIUM,
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        )
    }
}

@Preview(showBackground = true, name = "DSBottomSheet with multiple actions")
@Composable
private fun DSBottomSheetMultipleActionsPreview() {
    DSJarvisTheme {
        DSBottomSheet(
            onDismissRequest = {},
            title = { DSText("Multiple Actions") },
            content = {
                DSText("This bottom sheet has multiple action buttons to demonstrate the flexible layout.")
            },
            buttons = {
                DSButton(
                    text = "Primary Action",
                    onClick = {},
                    style = DSButtonStyle.PRIMARY,
                    modifier = Modifier.fillMaxWidth(),
                )
                DSButton(
                    text = "Secondary Action",
                    onClick = {},
                    style = DSButtonStyle.SECONDARY,
                    modifier = Modifier.fillMaxWidth(),
                )
                DSButton(
                    text = "Cancel",
                    style = DSButtonStyle.TEXT,
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )
            },
        )
    }
}