package com.jarvis.internal.feature.home.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jarvis.core.designsystem.component.DSIcon
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.component.DSIconTint
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.internal.feature.home.domain.entity.SessionFilter

/**
 * Chip for selecting session filter (Last Session vs General)
 */
@Composable
fun SessionFilterChip(
    filter: SessionFilter,
    isSelected: Boolean,
    onSelected: (SessionFilter) -> Unit,
    modifier: Modifier = Modifier,
    selectedGradient: Brush = Brush.horizontalGradient(
        colors = listOf(
            DSJarvisTheme.colors.extra.jarvisPink,
            DSJarvisTheme.colors.extra.jarvisBlue
        )
    )
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected)
            DSJarvisTheme.colors.primary.primary100 // not used when gradient is on
        else
            DSJarvisTheme.colors.extra.white,
        animationSpec = tween(200),
        label = "chip_background"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected)
            DSJarvisTheme.colors.neutral.neutral0
        else
            DSJarvisTheme.colors.neutral.neutral80,
        animationSpec = tween(200),
        label = "chip_content"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected)
            DSJarvisTheme.colors.extra.transparent
        else
            DSJarvisTheme.colors.neutral.neutral40,
        animationSpec = tween(200),
        label = "chip_border"
    )

    val shape = DSJarvisTheme.shapes.l

    Row(
        modifier = modifier
            .clip(shape)
            // Use gradient when selected, solid color otherwise
            .then(
                if (isSelected)
                    Modifier.background(brush = selectedGradient, shape = shape)
                else
                    Modifier.background(color = backgroundColor, shape = shape)
            )
            .border(1.dp, borderColor, shape)
            .clickable(role = Role.RadioButton) { onSelected(filter) }
            .semantics { this.selected = isSelected }
            .padding(
                horizontal = DSJarvisTheme.spacing.m,
                vertical = DSJarvisTheme.spacing.s
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.xs)
    ) {
        DSIcon(
            imageVector = filter.icon,
            contentDescription = filter.displayName,
            tint = DSIconTint.Solid(contentColor),
            size = DSJarvisTheme.dimensions.m
        )

        DSText(
            text = filter.displayName,
            style = DSJarvisTheme.typography.body.small,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = contentColor
        )
    }
}

/**
 * Extended session filter chips with descriptions
 */
@Composable
fun ExtendedSessionFilterChips(
    selectedFilter: SessionFilter,
    onFilterSelected: (SessionFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
    ) {
        DSText(
            text = "Data Range",
            style = DSJarvisTheme.typography.body.medium,
            fontWeight = FontWeight.Medium,
            color = DSJarvisTheme.colors.neutral.neutral80
        )

        SessionFilter.values().forEach { filter ->
            SessionFilterCard(
                filter = filter,
                isSelected = selectedFilter == filter,
                onSelected = onFilterSelected
            )
        }
    }
}

/**
 * Detailed session filter card with description
 */
@Composable
private fun SessionFilterCard(
    filter: SessionFilter,
    isSelected: Boolean,
    onSelected: (SessionFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) DSJarvisTheme.colors.primary.primary60.copy(alpha = 0.1f) else DSJarvisTheme.colors.extra.background,
        animationSpec = tween(200),
        label = "card_background"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) DSJarvisTheme.colors.primary.primary60 else DSJarvisTheme.colors.neutral.neutral20,
        animationSpec = tween(200),
        label = "card_border"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(DSJarvisTheme.shapes.m)
            .background(backgroundColor)
            .border(width = if (isSelected) 2.dp else 1.dp, color = borderColor, shape = DSJarvisTheme.shapes.m)
            .clickable(role = Role.RadioButton) { onSelected(filter) }
            .semantics { this.selected = isSelected }
            .padding(DSJarvisTheme.spacing.m),
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.xs)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = filter.icon,
                contentDescription = filter.displayName,
                tint = if (isSelected) DSJarvisTheme.colors.primary.primary60 else DSJarvisTheme.colors.neutral.neutral60,
                modifier = Modifier.size(20.dp)
            )

            DSText(
                text = filter.displayName,
                style = DSJarvisTheme.typography.body.large,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) DSJarvisTheme.colors.primary.primary60 else DSJarvisTheme.colors.neutral.neutral100
            )
        }

        DSText(
            text = filter.description,
            style = DSJarvisTheme.typography.body.small,
            color = DSJarvisTheme.colors.neutral.neutral60
        )
    }
}

/**
 * Extensions for SessionFilter
 */
private val SessionFilter.icon: ImageVector
    get() = when (this) {
        SessionFilter.LAST_SESSION -> Icons.Default.AccessTime
        SessionFilter.GENERAL -> Icons.Default.ViewList
    }

private val SessionFilter.displayName: String
    get() = when (this) {
        SessionFilter.LAST_SESSION -> "SESSION"
        SessionFilter.GENERAL -> "All"
    }

private val SessionFilter.description: String
    get() = when (this) {
        SessionFilter.LAST_SESSION -> "Shows metrics from your current app session only. Useful for debugging current behavior and real-time monitoring."
        SessionFilter.GENERAL -> "Shows all historical data across all app sessions. Provides comprehensive insights and long-term trends."
    }

@Preview(showBackground = true)
@Composable
private fun SessionFilterChipPreview() {
    DSJarvisTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SessionFilterChip(
                filter = SessionFilter.LAST_SESSION,
                isSelected = true,
                onSelected = {}
            )
            SessionFilterChip(
                filter = SessionFilter.GENERAL,
                isSelected = false,
                onSelected = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExtendedSessionFilterChipsPreview() {
    DSJarvisTheme {
        ExtendedSessionFilterChips(
            selectedFilter = SessionFilter.LAST_SESSION,
            onFilterSelected = {}
        )
    }
}