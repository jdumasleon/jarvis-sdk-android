package com.jarvis.core.designsystem.component

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.jarvis.core.designsystem.theme.DSJarvisTheme

@Composable
fun DSCircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = CircularProgressIndicatorDefaults.circularColor,
    strokeWidth: Dp = CircularProgressIndicatorDefaults.circularStrokeWidth,
) {
    CircularProgressIndicator(
        modifier,
        color,
        strokeWidth,
        strokeCap = StrokeCap.Round,
    )
}

@Preview(showBackground = true, name = "DS Circular Progress Indicator")
@Composable
internal fun CircularProgressIndicatorPreview() {
    DSJarvisTheme {
        DSCircularProgressIndicator(
            color = DSJarvisTheme.colors.primary.primary100,
            strokeWidth = DSJarvisTheme.dimensions.xxs,
        )
    }
}

object CircularProgressIndicatorDefaults {
    val circularColor: Color
        @Composable get() = DSJarvisTheme.colors.primary.primary100

    val circularStrokeWidth: Dp
        @Composable get() = DSJarvisTheme.dimensions.xxs
}