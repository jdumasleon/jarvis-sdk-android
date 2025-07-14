package com.jarvis.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun DSJarvisTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalDSColors provides DSColors(),
        LocalDSTypography provides DSTypography(),
        LocalDSSpacing provides DSSpacing(),
        LocalDSBorder provides DSBorder(),
        LocalDSGradientColors provides DSGradientColors(),
        LocalDSBackgroundTheme provides DSBackgroundTheme(),
        LocalDSTintTheme provides DSTintTheme(),
        LocalDSContentEmphasis provides DSContentEmphasis(),
        content = content
    )
}

object DSJarvisTheme {

    val colors: DSColors
        @Composable
        get() = LocalDSColors.current

    val typography: DSTypography
        @Composable
        get() = LocalDSTypography.current

    val spacing: DSSpacing
        @Composable
        get() = LocalDSSpacing.current

    val border: DSBorder
        @Composable
        get() = LocalDSBorder.current

    val gradient: DSGradientColors
        @Composable
        get() = LocalDSGradientColors.current

    val background: DSBackgroundTheme
        @Composable
        get() = LocalDSBackgroundTheme.current

    val tint: DSTintTheme
        @Composable
        get() = LocalDSTintTheme.current

    val contentEmphasis: DSContentEmphasis
        @Composable
        get() = LocalDSContentEmphasis.current
}