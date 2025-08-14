package com.jarvis.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun DSJarvisTheme(
    content: @Composable () -> Unit
) {
    val dsBackgroundTheme = DSBackgroundTheme(
        color = DSJarvisTheme.colors.extra.background,
        tonalElevation = DSJarvisTheme.elevations.none
    )

    CompositionLocalProvider(
        LocalDSBackgroundTheme provides dsBackgroundTheme,
        LocalDSBorder provides DSBorder(),
        LocalDSColors provides DSColors(),
        LocalDSContentEmphasis provides DSContentEmphasis(),
        LocalDSDimensions provides DSDimensions(),
        LocalDSElevations provides DSElevations(),
        LocalDSGradientColors provides DSGradientColors(),
        LocalDSShapes provides DSShape(),
        LocalDSSpacing provides DSSpacing(),
        LocalDSTint provides DSTint(),
        LocalDSTypography provides DSTypography(),
        LocalDSMotionScheme provides DSMotionScheme(),
        content = content
    )
}

object DSJarvisTheme {

    val background: DSBackgroundTheme
        @Composable
        get() = LocalDSBackgroundTheme.current

    val border: DSBorder
        @Composable
        get() = LocalDSBorder.current

    val colors: DSColors
        @Composable
        get() = LocalDSColors.current

    val contentEmphasis: DSContentEmphasis
        @Composable
        get() = LocalDSContentEmphasis.current

    val dimensions: DSDimensions
        @Composable
        get() = LocalDSDimensions.current

    val elevations: DSElevations
        @Composable
        get() = LocalDSElevations.current

    val gradient: DSGradientColors
        @Composable
        get() = LocalDSGradientColors.current

    val shapes: DSShape
        @Composable
        get() = LocalDSShapes.current

    val spacing: DSSpacing
        @Composable
        get() = LocalDSSpacing.current

    val tint: DSTint
        @Composable
        get() = LocalDSTint.current

    val typography: DSTypography
        @Composable
        get() = LocalDSTypography.current

    val motionScheme: DSMotionScheme
        @Composable
        get() = LocalDSMotionScheme.current
}