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
import com.jarvis.core.designsystem.theme.Neutral100
import com.jarvis.core.designsystem.theme.Neutral20
import com.jarvis.core.designsystem.theme.Neutral60
import com.jarvis.core.designsystem.theme.Primary100
import com.jarvis.core.designsystem.theme.Success100
import com.jarvis.core.designsystem.theme.Warning100

enum class DSTagStyle(
    val backgroundColor: Color,
    val tagTextColor: Color,
    val iconColor: Color,
) {
    Info(
        backgroundColor = Primary100,
        tagTextColor = Neutral0,
        iconColor = Neutral0
    ),
    Neutral(
        backgroundColor = Neutral20,
        tagTextColor = Neutral60,
        iconColor = Neutral60
    ),
    Success (
        backgroundColor = Success100,
        tagTextColor = Neutral0,
        iconColor = Neutral60
    ),
    Warning(
        backgroundColor = Warning100,
        tagTextColor = Neutral100,
        iconColor = Neutral100
    );

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

@Preview(showBackground = true, name = "All DSTag styles")
@Composable
private fun PreviewDSTag() {
    Column(
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.dimensions.s),
        modifier = Modifier.padding(DSJarvisTheme.dimensions.m)
    ) {
        DSTag(
            icon = DSIcons.home,
            tag = "Info tag with icon",
            style = DSTagStyle.Info
        )
        DSTag(
            tag = "Secondary Tag",
            style = DSTagStyle.Info
        )
        DSTag(
            icon = DSIcons.person,
            tag = "Neutral tag with icon",
            style = DSTagStyle.Neutral
        )
        DSTag(
            tag = "Neutral Tag",
            style = DSTagStyle.Neutral
        )
        DSTag(
            tag = "Success Tag",
            style = DSTagStyle.Success
        )
        DSTag(
            tag = "Warning Tag",
            style = DSTagStyle.Warning
        )


    }
}