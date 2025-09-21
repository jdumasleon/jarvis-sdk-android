package com.jarvis.core.designsystem.component

import androidx.annotation.FloatRange
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import com.jarvis.core.designsystem.theme.DSJarvisTheme

@Composable
fun DSLinearProgressIndicator(
    @FloatRange(from = 0.0, to = 1.0) progress: Float,
    modifier: Modifier = Modifier,
    color: Color = DSJarvisTheme.colors.primary.primary100,
    backgroundColor: Color = DSJarvisTheme.colors.neutral.neutral20,
    progressAnimationSpec: AnimationSpec<Float> = SpringSpec(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessVeryLow,
        // The default threshold is 0.01, or 1% of the overall progress range, which is quite
        // large and noticeable. We purposefully choose a smaller threshold.
        visibilityThreshold = 1 / 1000f,
    ),
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = progressAnimationSpec,
        label = "LinearProgressWidth",
    )
    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = modifier.fillMaxWidth(),
        color = color,
        trackColor = backgroundColor,
        strokeCap = StrokeCap.Round,
    )
}

@Preview(showBackground = true, name = "DS Linear Progress Indicator")
@Composable
internal fun LinearProgressIndicatorPreview() {
    DSJarvisTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m),
            modifier = Modifier.padding(DSJarvisTheme.spacing.xxs),
        ) {
            DSLinearProgressIndicator(1f)
            DSLinearProgressIndicator(0f)
            DSLinearProgressIndicator(
                progress = 0.5f,
                color = DSJarvisTheme.colors.primary.primary100,
                backgroundColor = DSJarvisTheme.colors.neutral.neutral20,
            )
            DSLinearProgressIndicator(
                modifier = Modifier.height(DSJarvisTheme.dimensions.xxs),
                progress = 0.5f,
                color = DSJarvisTheme.colors.primary.primary100,
                backgroundColor = DSJarvisTheme.colors.neutral.neutral20,
            )
        }
    }
}