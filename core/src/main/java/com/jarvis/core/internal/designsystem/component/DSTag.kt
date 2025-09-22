@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.core.internal.designsystem.component

import androidx.annotation.RestrictTo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import com.jarvis.core.internal.designsystem.icons.DSIcons
import com.jarvis.core.internal.designsystem.theme.DSJarvisTheme

enum class DSTagStyle {
    Info,
    Neutral,
    Success,
    Warning;

    val corners: RoundedCornerShape
        @Composable
        get() = when(this) {
            Info -> DSJarvisTheme.shapes.m
            Neutral -> DSJarvisTheme.shapes.m
            Success -> DSJarvisTheme.shapes.m
            Warning -> DSJarvisTheme.shapes.m
        }

    val fontStyle: TextStyle
        @Composable
        get() = when(this) {
            Info -> DSJarvisTheme.typography.body.small
            Neutral -> DSJarvisTheme.typography.body.small
            Success -> DSJarvisTheme.typography.body.small
            Warning -> DSJarvisTheme.typography.body.small
        }
}

@Composable
private fun DSTagStyle.getBackgroundColor(): Color = when(this) {
    DSTagStyle.Info -> DSJarvisTheme.colors.primary.primary100
    DSTagStyle.Neutral -> DSJarvisTheme.colors.neutral.neutral20
    DSTagStyle.Success -> DSJarvisTheme.colors.success.success100
    DSTagStyle.Warning -> DSJarvisTheme.colors.warning.warning100
}

@Composable
private fun DSTagStyle.getTextColor(): Color = when(this) {
    DSTagStyle.Info -> DSJarvisTheme.colors.neutral.neutral0
    DSTagStyle.Neutral -> DSJarvisTheme.colors.neutral.neutral60
    DSTagStyle.Success -> DSJarvisTheme.colors.neutral.neutral0
    DSTagStyle.Warning -> DSJarvisTheme.colors.neutral.neutral100
}

@Composable
private fun DSTagStyle.getIconColor(): Color = when(this) {
    DSTagStyle.Info -> DSJarvisTheme.colors.neutral.neutral0
    DSTagStyle.Neutral -> DSJarvisTheme.colors.neutral.neutral60
    DSTagStyle.Success -> DSJarvisTheme.colors.neutral.neutral60
    DSTagStyle.Warning -> DSJarvisTheme.colors.neutral.neutral100
}

@Composable
fun DSTag(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tag: String,
    style: DSTagStyle = DSTagStyle.Info
) {
    DSTag(
        modifier = modifier,
        icon = icon,
        tag = tag,
        backgroundColor = style.getBackgroundColor(),
        tagTextColor = style.getTextColor(),
        iconColor = style.getIconColor()
    )
}

@Composable
fun DSTag(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tag: String,
    backgroundColor: Color,
    tagTextColor: Color,
    iconColor: Color,
    tagTextStyle: TextStyle = DSJarvisTheme.typography.body.small,
    shape: RoundedCornerShape = DSJarvisTheme.shapes.m,
) {
    Row(
        modifier = modifier
            .background(backgroundColor, shape)
            .padding(DSJarvisTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically) {
        icon?.let {
            DSIcon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(DSJarvisTheme.dimensions.m)
                    .padding(end = DSJarvisTheme.spacing.xxs),
                tint = DSIconTint.Solid(iconColor)
            )
        }
        DSText(
            text = tag,
            style = tagTextStyle,
            color = tagTextColor
        )
    }
}

@Preview(showBackground = true, name = "Tag with all styles")
@Composable
fun DSTagPreview() {
    DSJarvisTheme {
        val icon = DSIcons.home
        Column(verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)) {
            DSTag(
                tag = "Info Tag",
                style = DSTagStyle.Info,
                icon = icon
            )
            DSTag(
                tag = "Neutral Tag",
                style = DSTagStyle.Neutral,
                icon = icon
            )
            DSTag(
                tag = "Success Tag",
                style = DSTagStyle.Success,
                icon = icon
            )
            DSTag(
                tag = "Warning Tag",
                style = DSTagStyle.Warning,
                icon = icon
            )
        }
    }
}