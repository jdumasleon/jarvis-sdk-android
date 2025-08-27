package com.jarvis.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jarvis.core.designsystem.theme.DSJarvisTheme

/**
 * DSJarvis FilterChip component
 * A selectable chip for filtering content following Jarvis design system specifications
 */
@Composable
fun DSFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selectedGradient: Brush? = null
) {
    val backgroundColor = when {
        !enabled -> DSJarvisTheme.colors.neutral.neutral20
        selected -> DSJarvisTheme.colors.primary.primary100 // used as a fallback when no gradient
        else -> DSJarvisTheme.colors.extra.white
    }

    val borderColor = when {
        !enabled -> DSJarvisTheme.colors.neutral.neutral40
        selected -> DSJarvisTheme.colors.extra.transparent
        else -> DSJarvisTheme.colors.neutral.neutral60
    }

    val textColor = when {
        !enabled -> DSJarvisTheme.colors.neutral.neutral40
        selected -> DSJarvisTheme.colors.neutral.neutral0
        else -> DSJarvisTheme.colors.neutral.neutral80
    }

    val shape = DSJarvisTheme.shapes.l

    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (selected && enabled)
                    selectedGradient?.let {
                        Modifier.background(brush = it, shape = shape)
                    } ?: Modifier.background(color = backgroundColor, shape = shape)
                else
                    Modifier.background(color = backgroundColor, shape = shape)
            )
            .border(width = 1.dp, color = borderColor, shape = shape)
            .clickable(enabled = enabled) { onClick() }
            .padding(
                horizontal = DSJarvisTheme.spacing.m,
                vertical = DSJarvisTheme.spacing.s
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selected) {
                DSIcon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(DSJarvisTheme.dimensions.m)
                )
            }

            DSText(
                text = label,
                style = DSJarvisTheme.typography.body.small,
                color = textColor,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

// Preview components
@Preview(showBackground = true)
@Composable
fun DSFilterChipSelectedPreview() {
    DSJarvisTheme {
        DSFilterChip(
            selected = true,
            onClick = { },
            label = "Selected"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DSFilterChipUnselectedPreview() {
    DSJarvisTheme {
        DSFilterChip(
            selected = false,
            onClick = { },
            label = "Unselected"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DSFilterChipDisabledPreview() {
    DSJarvisTheme {
        DSFilterChip(
            selected = false,
            onClick = { },
            label = "Disabled",
            enabled = false
        )
    }
}