package com.jarvis.core.designsystem.component

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
import com.jarvis.core.designsystem.icons.DSIcons
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.designsystem.theme.Neutral0
import com.jarvis.core.designsystem.theme.Neutral20
import com.jarvis.core.designsystem.theme.Neutral60
import com.jarvis.core.designsystem.theme.Primary100

enum class DSTagStyle(
    val backgroundColor: Color,
    val tagTextColor: Color,
    val iconColor: Color,
) {
    Primary(
        backgroundColor = Primary100,
        tagTextColor = Neutral0,
        iconColor = Neutral0
    ),
    Secondary(
        backgroundColor = Neutral20,
        tagTextColor = Neutral60,
        iconColor = Neutral60
    );

    val corners: RoundedCornerShape
        @Composable
        get() = when(this) {
            Primary -> DSJarvisTheme.shapes.m
            Secondary -> DSJarvisTheme.shapes.m
        }

    val fontStyle: TextStyle
        @Composable
        get() = when(this) {
            Primary -> DSJarvisTheme.typography.body.small
            Secondary -> DSJarvisTheme.typography.body.small
        }
}

@Composable
fun DSTag(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tag: String,
    style: DSTagStyle = DSTagStyle.Primary
) {
    DSTag(
        modifier = modifier,
        icon = icon,
        tag = tag,
        backgroundColor = style.backgroundColor,
        tagTextColor = style.tagTextColor,
        iconColor = style.iconColor
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
            .padding(DSJarvisTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically) {
        icon?.let {
            DSIcon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(DSJarvisTheme.dimensions.m)
                    .padding(end = DSJarvisTheme.spacing.xxs),
                tint = iconColor
            )
        }
        DSText(
            text = tag,
            style = tagTextStyle,
            color = tagTextColor
        )
    }
}

@Preview(showBackground = true, name = "All DSTag styles")
@Composable
private fun PreviewDSTag() {
    Column(
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.dimensions.s),
        modifier = Modifier.padding(DSJarvisTheme.dimensions.m)
    ) {
        DSTag(
            icon = DSIcons.Home,
            tag = "Primary Tag",
            style = DSTagStyle.Primary
        )
        DSTag(
            icon = DSIcons.person,
            tag = "Secondary Tag with icon",
            style = DSTagStyle.Secondary
        )
        DSTag(
            tag = "Secondary Tag",
            style = DSTagStyle.Secondary
        )
    }
}