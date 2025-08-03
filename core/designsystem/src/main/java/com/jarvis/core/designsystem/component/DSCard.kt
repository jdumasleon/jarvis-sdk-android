package com.jarvis.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.designsystem.theme.Neutral0
import com.jarvis.core.designsystem.theme.Neutral40
import com.jarvis.core.designsystem.theme.Primary100

@Composable
fun DSCard(
    modifier: Modifier = Modifier,
    shape: CornerBasedShape = DSJarvisTheme.shapes.none,
    color: Color = Neutral0,
    elevation: Dp = DSJarvisTheme.elevations.none,
    border: BorderStroke? = null,
    isDisabled: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = if (isDisabled) Neutral40 else color,
        contentColor = if (isDisabled) Neutral40 else color,
        tonalElevation = elevation,
        shadowElevation = elevation,
        border = border,
    ) {
        Column(
            modifier = modifier
                .padding(DSJarvisTheme.spacing.m),
            content = content
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DSCardPreview() {
    DSJarvisTheme {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m)
        ) {
            DSCard(color = Primary100) {
                DSText(text = "DSCard Preview")
            }
            DSCard(
                shape = DSJarvisTheme.shapes.s,
                elevation = DSJarvisTheme.elevations.level2,
            ) {
                DSText(text = "DSText Preview with small shape and level 2 elevation")
            }
            DSCard(
                shape = DSJarvisTheme.shapes.m,
                elevation = DSJarvisTheme.elevations.level3,
            ) {
                DSText(text = "DSText Preview with medium shape and level 3 elevation")
            }
            DSCard(
                shape = DSJarvisTheme.shapes.l,
                elevation = DSJarvisTheme.elevations.level4,
            ) {
                DSText(text = "DSText Preview with large shape and level 4 elevation")
            }
            DSCard(
                shape = DSJarvisTheme.shapes.l,
                elevation = DSJarvisTheme.elevations.level4,
                isDisabled = true
            ) {
                DSText(text = "DSText Preview with large shape and level 4 elevation")
            }
        }
    }
}