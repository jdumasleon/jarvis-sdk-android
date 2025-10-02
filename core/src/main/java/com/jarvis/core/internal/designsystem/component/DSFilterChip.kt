@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)

package com.jarvis.core.internal.designsystem.component

import androidx.annotation.RestrictTo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jarvis.core.internal.designsystem.component.DSIconTint
import com.jarvis.core.internal.designsystem.theme.DSJarvisTheme

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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Enhanced state logic that considers both selection and interaction states
    val backgroundColor = when {
        !enabled -> DSJarvisTheme.colors.neutral.neutral20
        selected && !isPressed -> DSJarvisTheme.colors.primary.primary100 // Normal selected state
        selected && isPressed -> DSJarvisTheme.colors.primary.primary80  // Pressed selected state
        !selected && isPressed -> DSJarvisTheme.colors.neutral.neutral20 // Pressed unselected state
        else -> DSJarvisTheme.colors.extra.white // Normal unselected state
    }

    val borderColor = when {
        !enabled -> DSJarvisTheme.colors.neutral.neutral40
        selected && !isPressed -> DSJarvisTheme.colors.extra.transparent
        selected && isPressed -> DSJarvisTheme.colors.primary.primary60
        !selected && isPressed -> DSJarvisTheme.colors.neutral.neutral40
        else -> DSJarvisTheme.colors.neutral.neutral60
    }

    val textColor = when {
        !enabled -> DSJarvisTheme.colors.neutral.neutral40
        selected -> DSJarvisTheme.colors.neutral.neutral0
        isPressed -> DSJarvisTheme.colors.neutral.neutral60
        else -> DSJarvisTheme.colors.neutral.neutral80
    }

    val shape = DSJarvisTheme.shapes.l

    Box(
        modifier = modifier
            .clip(shape)
            .then(
                // Use gradient only for selected state when not pressed
                if (selected && !isPressed && enabled && selectedGradient != null)
                    Modifier.background(brush = selectedGradient, shape = shape)
                else
                    Modifier.background(color = backgroundColor, shape = shape)
            )
            .border(width = 1.dp, color = borderColor, shape = shape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null // We handle visual feedback manually
            ) { 
                onClick() 
            }
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
                    tint = DSIconTint.Solid(textColor),
                    size = DSJarvisTheme.dimensions.m
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