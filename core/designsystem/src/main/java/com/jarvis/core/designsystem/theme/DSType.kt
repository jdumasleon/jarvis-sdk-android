package com.jarvis.core.designsystem.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.jarvis.core.designsystem.R

class DSTypography(
    val heading: Heading = Heading(),
    val body: Body = Body(),
    val display: Display = Display()
) {

    data class Heading(
        val heading1: TextStyle = getTextStyle(R.font.sf_mono_semibold, 40.sp, 44.sp),  // Semibold, 110% line height
        val heading2: TextStyle = getTextStyle(R.font.sf_mono_medium, 32.sp, 32.sp),    // Medium, 100% line height
        val heading3: TextStyle = getTextStyle(R.font.sf_mono_medium, 30.sp, 30.sp),    // Medium, 100% line height
        val heading4: TextStyle = getTextStyle(R.font.sf_mono_medium, 28.sp, 37.sp),    // Medium, 133% line height
        val heading5: TextStyle = getTextStyle(R.font.sf_mono_medium, 22.sp, 32.sp)     // Medium, 145% line height
    )

    data class Body(
        val extraLargeTextMedium: TextStyle = getTextStyle(R.font.sf_mono_medium, 18.sp, 26.sp),  // Medium, 145% line height
        val largeTextMedium: TextStyle = getTextStyle(R.font.sf_mono_medium, 16.sp, 22.sp),       // Medium, 140% line height
        val mediumTextMedium: TextStyle = getTextStyle(R.font.sf_mono_medium, 14.sp, 20.sp),      // Medium, 145% line height
        val smallTextMedium: TextStyle = getTextStyle(R.font.sf_mono_medium, 12.sp, 18.sp),       // Medium, 150% line height
        val extraSmallTextMedium: TextStyle = getTextStyle(R.font.sf_mono_medium, 10.sp, 16.sp)   // Medium, 160% line height
    )

    data class Display(
        val display1: TextStyle = getTextStyle(R.font.sf_mono_regular, 56.sp, 62.sp),  // Regular, 110% line height
        val display2: TextStyle = getTextStyle(R.font.sf_mono_medium, 48.sp, 64.sp),   // Medium, 135% line height
        val display3: TextStyle = getTextStyle(R.font.sf_mono_regular, 28.sp, 34.sp)   // Regular, 120% line height
    )
}

val LocalDSTypography = staticCompositionLocalOf { DSTypography() }

private fun getTextStyle(fontFamilyResource: Int, fontSize: TextUnit, lineHeight: TextUnit) = TextStyle(
    fontFamily = FontFamily(Font(fontFamilyResource)),
    fontSize = fontSize,
    lineHeight = lineHeight
)