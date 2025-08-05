package com.jarvis.core.designsystem.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jarvis.core.designsystem.icons.DSIcons
import com.jarvis.core.designsystem.theme.DSJarvisTheme

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
    enabled: Boolean = true
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(DSJarvisTheme.dimensions.xl),
        enabled = enabled
    ) {
        DSIcon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = if (enabled) DSJarvisTheme.colors.neutral.neutral80 else DSJarvisTheme.colors.neutral.neutral40
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
            imageVector = DSIcons.Add,
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
            imageVector = DSIcons.Add,
            contentDescription = "Add",
            enabled = false
        )
    }
}