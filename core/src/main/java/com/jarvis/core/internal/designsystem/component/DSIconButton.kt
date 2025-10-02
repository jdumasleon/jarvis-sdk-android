@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.core.internal.designsystem.component

import androidx.annotation.RestrictTo

import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.jarvis.core.internal.designsystem.component.DSIconTint
import com.jarvis.core.internal.designsystem.icons.DSIcons
import com.jarvis.core.internal.designsystem.theme.DSJarvisTheme

/**
 * DSJarvis IconButton component
 * A clickable icon button following Jarvis design system specifications
 */
@Composable
fun DSIconButton(
    onClick: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: DSIconTint = DSIconTint.Solid(DSJarvisTheme.colors.neutral.neutral80),
    enabled: Boolean = true
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        DSIcon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = if (enabled) tint else DSIconTint.Solid(DSJarvisTheme.colors.neutral.neutral40)
        )
    }
}

// Preview components
@Preview(showBackground = true)
@Composable
fun DSIconButtonPreview() {
    DSJarvisTheme {
        DSIconButton(
            onClick = { },
            imageVector = DSIcons.add,
            contentDescription = "add"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DSIconButtonDisabledPreview() {
    DSJarvisTheme {
        DSIconButton(
            onClick = { },
            imageVector = DSIcons.add,
            contentDescription = "Add",
            enabled = false
        )
    }
}