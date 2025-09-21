package com.jarvis.core.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jarvis.core.designsystem.component.DSDialog
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.component.DSButton
import com.jarvis.core.designsystem.component.DSButtonStyle
import com.jarvis.core.designsystem.theme.DSJarvisTheme

/**
 * Reusable confirmation dialog
 */
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    DSDialog(
        onDismissRequest = onDismiss,
        title = { DSText(title) },
        text = { DSText(message) },
        confirmButton = {
            DSButton(
                text = confirmText,
                style = DSButtonStyle.PRIMARY,
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        dismissButton = {
            DSButton(
                text = dismissText,
                style = DSButtonStyle.SECONDARY,
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    )
}

/**
 * Reusable info dialog
 */
@Composable
fun InfoDialog(
    title: String,
    message: String,
    buttonText: String = "OK",
    onDismiss: () -> Unit
) {
    DSDialog(
        onDismissRequest = onDismiss,
        title = { DSText(title) },
        text = { DSText(message) },
        confirmButton = {
            DSButton(
                text = buttonText,
                style = DSButtonStyle.PRIMARY,
                onClick = onDismiss
            )
        }
    )
}

/**
 * Reusable error dialog
 */
@Composable
fun ErrorDialog(
    error: Throwable,
    title: String = "Error",
    message: String? = null,
    onDismiss: () -> Unit
) {
    InfoDialog(
        title = title,
        message = message ?: error.message ?: "An unknown error occurred",
        onDismiss = onDismiss
    )
}

// Preview components
@Preview
@Composable
fun ConfirmationDialogPreview() {
    DSJarvisTheme {
        ConfirmationDialog(
            title = "Delete Item",
            message = "Are you sure you want to delete this item? This action cannot be undone.",
            onConfirm = { },
            onDismiss = { }
        )
    }
}

@Preview
@Composable
fun InfoDialogPreview() {
    DSJarvisTheme {
        InfoDialog(
            title = "Information",
            message = "This is an informational message.",
            onDismiss = { }
        )
    }
}

@Preview
@Composable
fun ErrorDialogPreview() {
    DSJarvisTheme {
        ErrorDialog(
            error = RuntimeException("Something went wrong"),
            onDismiss = { }
        )
    }
}