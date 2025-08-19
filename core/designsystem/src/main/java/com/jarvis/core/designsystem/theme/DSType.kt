package com.jarvis.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.jarvis.core.designsystem.R

val SF = FontFamily(
    Font(R.font.sf_pro_text_thin, FontWeight.Thin),
    Font(R.font.sf_pro_text_light, FontWeight.Light),
    Font(R.font.sf_pro_text_regular, FontWeight.Normal),
    Font(R.font.sf_pro_text_medium, FontWeight.Medium),
    Font(R.font.sf_pro_text_semibold, FontWeight.SemiBold),
    Font(R.font.sf_pro_text_bold, FontWeight.Bold),
    Font(R.font.sf_pro_text_black, FontWeight.Black),
    Font(R.font.sf_pro_text_heavy, FontWeight.Black),
)

@Immutable
class DSTypography(
    val display: Display = Display(),
    val heading: Heading = Heading(),
    val title: Title = Title(),
    val body: Body = Body(),
    val label: Label = Label()
) {
    data class Display(
        val large: TextStyle = getFontStyle(FontWeight.Light, 57.sp, 64.sp),
        val medium: TextStyle = getFontStyle(FontWeight.Light, 45.sp, 52.sp),
        val small: TextStyle = getFontStyle(FontWeight.Normal, 36.sp, 44.sp)
    )

    data class Heading(
        val large: TextStyle = getFontStyle(FontWeight.Normal, 32.sp, 40.sp),
        val medium: TextStyle = getFontStyle(FontWeight.Normal, 28.sp, 36.sp),
        val small: TextStyle = getFontStyle(FontWeight.Normal, 24.sp, 32.sp)
    )

    data class Title(
        val large: TextStyle = getFontStyle(FontWeight.Medium, 22.sp, 28.sp),
        val medium: TextStyle = getFontStyle(FontWeight.Medium, 16.sp, 24.sp),
        val small: TextStyle = getFontStyle(FontWeight.Medium, 14.sp, 20.sp)
    )

    data class Body(
        val large: TextStyle = getFontStyle(FontWeight.Normal, 16.sp, 24.sp),
        val medium: TextStyle = getFontStyle(FontWeight.Normal, 14.sp, 20.sp),
        val small: TextStyle = getFontStyle(FontWeight.Normal, 12.sp, 16.sp)
    )

    data class Label(
        val large: TextStyle = getFontStyle(FontWeight.Medium, 14.sp, 20.sp),
        val medium: TextStyle = getFontStyle(FontWeight.Medium, 12.sp, 16.sp),
        val small: TextStyle = getFontStyle(FontWeight.Medium, 11.sp, 16.sp),
    )
}

val LocalDSTypography = staticCompositionLocalOf { DSTypography() }

private fun getFontStyle(
    weight: FontWeight,
    size: TextUnit,
    lineHeight: TextUnit,
    letterSpacing: TextUnit = TextUnit.Unspecified
) = TextStyle(
    fontFamily = SF,
    fontWeight = weight,
    fontSize = size,
    lineHeight = lineHeight,
    letterSpacing = letterSpacing
)