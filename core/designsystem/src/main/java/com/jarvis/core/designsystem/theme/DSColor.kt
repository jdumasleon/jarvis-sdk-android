package com.jarvis.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Light Theme Colors
val Primary100 = Color(0xFF2752E7)
val Primary80 = Color(0xFF3756EC)
val Primary60 = Color(0xFF007AFF)
val Primary40 = Color(0xFF7D99F1)
val Primary20 = Color(0xFFD0D8FF)
val Primary0 = Color(0xFFF5F8FE)

val Secondary100 = Color(0xFF3F8A5F)
val Secondary80 = Color(0xFFFDB989)
val Secondary60 = Color(0xFFFEE8B6)
val Secondary40 = Color(0xFFFFF2D0)
val Secondary20 = Color(0xFFFFF8EF)

val Neutral100 = Color(0xFF8E8E93)
val Neutral80 = Color(0xFFAEAEB2)
val Neutral60 = Color(0xFFC7C7CC)
val Neutral40 = Color(0xFFD1D1D6)
val Neutral20 = Color(0xFFE5E5EA)
val Neutral0 = Color(0xFFF2F2F7)

val Success100 = Color(0xFF3F8A5F)
val Success80 = Color(0xFF55977F)
val Success60 = Color(0xFF8CB59F)
val Success40 = Color(0xFFC8E0BF)
val Success20 = Color(0xFFDEE6DF)

val Warning100 = Color(0xFFFF9800)
val Warning80 = Color(0xFFF6A732)
val Warning60 = Color(0xFFFAC87D)
val Warning40 = Color(0xFFFAE2BF)
val Warning20 = Color(0xFFF4F4DE)

val Error100 = Color(0xFFE84141)
val Error80 = Color(0xFFE05353)
val Error60 = Color(0xFFEF7979)
val Error40 = Color(0xFFF3BEBE)
val Error20 = Color(0xFFF9EDE9)

val Info100 = Color(0xFF2661CA)
val Info80 = Color(0xFF417CB7)
val Info60 = Color(0xFF70B8D7)
val Info40 = Color(0xFFA5CCE2)
val Info20 = Color(0xFFDEF2F4)

val background0 = Color(0xFFF2F2F7)
val background01 = Color(0x80F2F2F7)

// Dark Theme Colors
val DarkPrimary100 = Color(0xFF4A6EF5)
val DarkPrimary80 = Color(0xFF5B7AF7)
val DarkPrimary60 = Color(0xFF0A84FF)
val DarkPrimary40 = Color(0xFF6B8AFF)
val DarkPrimary20 = Color(0xFF2A3D66)
val DarkPrimary0 = Color(0xFF1A1F2E)

val DarkSecondary100 = Color(0xFF4CAF50)
val DarkSecondary80 = Color(0xFFFFB74D)
val DarkSecondary60 = Color(0xFFFFCC80)
val DarkSecondary40 = Color(0xFF3D2F1F)
val DarkSecondary20 = Color(0xFF2D241A)

val DarkNeutral100 = Color(0xFFFFFFFF)
val DarkNeutral80 = Color(0xFFE5E5EA)
val DarkNeutral60 = Color(0xFFAEAEB2)
val DarkNeutral40 = Color(0xFF636366)
val DarkNeutral20 = Color(0xFF2C2C2E)
val DarkNeutral0 = Color(0xFF1C1C1E)

val DarkSuccess100 = Color(0xFF4CAF50)
val DarkSuccess80 = Color(0xFF66BB6A)
val DarkSuccess60 = Color(0xFF81C784)
val DarkSuccess40 = Color(0xFF1B4332)
val DarkSuccess20 = Color(0xFF14301E)

val DarkWarning100 = Color(0xFFFFB74D)
val DarkWarning80 = Color(0xFFFFCC80)
val DarkWarning60 = Color(0xFFFFE0B2)
val DarkWarning40 = Color(0xFF4A3319)
val DarkWarning20 = Color(0xFF332313)

val DarkError100 = Color(0xFFEF5350)
val DarkError80 = Color(0xFFE57373)
val DarkError60 = Color(0xFFEF9A9A)
val DarkError40 = Color(0xFF4A1C1C)
val DarkError20 = Color(0xFF331414)

val DarkInfo100 = Color(0xFF42A5F5)
val DarkInfo80 = Color(0xFF64B5F6)
val DarkInfo60 = Color(0xFF90CAF9)
val DarkInfo40 = Color(0xFF1A2C4A)
val DarkInfo20 = Color(0xFF131F33)

val DarkBackground0 = Color(0xFF000000)

// Chart Colors - optimized for both light and dark themes
val ChartColors = listOf(
    Color(0xFF2196F3), // Blue
    Color(0xFF4CAF50), // Green
    Color(0xFFFF9800), // Orange
    Color(0xFF9C27B0), // Purple
    Color(0xFFF44336), // Red
    Color(0xFF00BCD4), // Cyan
    Color(0xFFFFEB3B), // Yellow
    Color(0xFF607D8B), // Blue Grey
    Color(0xFFE91E63), // Pink
    Color(0xFF795548)  // Brown
)

@Immutable
data class DSColors(
    val primary: Primary = Primary(),
    val secondary: Secondary = Secondary(),
    val neutral: Neutral = Neutral(),
    val success: Success = Success(),
    val warning: Warning = Warning(),
    val error: Error = Error(),
    val info: Info = Info(),
    val extra: Extra = Extra(),
    val chart: Chart = Chart()
)

@Immutable
data class Primary(
    val primary100: Color = Primary100,
    val primary80: Color = Primary80,
    val primary60: Color = Primary60,
    val primary40: Color = Primary40,
    val primary20: Color = Primary20
)

@Immutable
data class Secondary(
    val secondary100: Color = Secondary100,
    val secondary80: Color = Secondary80,
    val secondary60: Color = Secondary60,
    val secondary40: Color = Secondary40,
    val secondary20: Color = Secondary20
)

@Immutable
data class Neutral(
    val neutral100: Color = Neutral100,
    val neutral80: Color = Neutral80,
    val neutral60: Color = Neutral60,
    val neutral40: Color = Neutral40,
    val neutral20: Color = Neutral20,
    val neutral0: Color = Neutral0
)

@Immutable
data class Success(
    val success100: Color = Success100,
    val success80: Color = Success80,
    val success60: Color = Success60,
    val success40: Color = Success40,
    val success20: Color = Success20
)

@Immutable
data class Warning(
    val warning100: Color = Warning100,
    val warning80: Color = Warning80,
    val warning60: Color = Warning60,
    val warning40: Color = Warning40,
    val warning20: Color = Warning20
)

@Immutable
data class Error(
    val error100: Color = Error100,
    val error80: Color = Error80,
    val error60: Color = Error60,
    val error40: Color = Error40,
    val error20: Color = Error20
)

@Immutable
data class Info(
    val info100: Color = Info100,
    val info80: Color = Info80,
    val info60: Color = Info60,
    val info40: Color = Info40,
    val info20: Color = Info20
)

@Immutable
data class Extra(
    val background: Color = background0,
    val surface: Color = Color.White,
    val onSurface: Color = Color.Black,
    val white: Color = Color.White,
    val black: Color = Color.Black,
    val transparent: Color = Color.Transparent,
    val backgroun: Color = background01

)

@Immutable
data class Chart(
    val primary: Color = ChartColors[0],
    val secondary: Color = ChartColors[1],
    val tertiary: Color = ChartColors[2],
    val quaternary: Color = ChartColors[3],
    val quinary: Color = ChartColors[4],
    val colors: List<Color> = ChartColors
)

// Dark theme color schemes
@Immutable
data class DarkPrimary(
    val primary100: Color = DarkPrimary100,
    val primary80: Color = DarkPrimary80,
    val primary60: Color = DarkPrimary60,
    val primary40: Color = DarkPrimary40,
    val primary20: Color = DarkPrimary20
)

@Immutable
data class DarkSecondary(
    val secondary100: Color = DarkSecondary100,
    val secondary80: Color = DarkSecondary80,
    val secondary60: Color = DarkSecondary60,
    val secondary40: Color = DarkSecondary40,
    val secondary20: Color = DarkSecondary20
)

@Immutable
data class DarkNeutral(
    val neutral100: Color = DarkNeutral100,
    val neutral80: Color = DarkNeutral80,
    val neutral60: Color = DarkNeutral60,
    val neutral40: Color = DarkNeutral40,
    val neutral20: Color = DarkNeutral20,
    val neutral0: Color = DarkNeutral0
)

@Immutable
data class DarkSuccess(
    val success100: Color = DarkSuccess100,
    val success80: Color = DarkSuccess80,
    val success60: Color = DarkSuccess60,
    val success40: Color = DarkSuccess40,
    val success20: Color = DarkSuccess20
)

@Immutable
data class DarkWarning(
    val warning100: Color = DarkWarning100,
    val warning80: Color = DarkWarning80,
    val warning60: Color = DarkWarning60,
    val warning40: Color = DarkWarning40,
    val warning20: Color = DarkWarning20
)

@Immutable
data class DarkError(
    val error100: Color = DarkError100,
    val error80: Color = DarkError80,
    val error60: Color = DarkError60,
    val error40: Color = DarkError40,
    val error20: Color = DarkError20
)

@Immutable
data class DarkInfo(
    val info100: Color = DarkInfo100,
    val info80: Color = DarkInfo80,
    val info60: Color = DarkInfo60,
    val info40: Color = DarkInfo40,
    val info20: Color = DarkInfo20
)

@Immutable
data class DarkExtra(
    val background: Color = DarkBackground0,
    val surface: Color = DarkNeutral0,
    val onSurface: Color = DarkNeutral100,
    val white: Color = Color.White,
    val black: Color = Color.Black,
    val transparent: Color = Color.Transparent
)

// Factory functions for creating color schemes
fun lightColors() = DSColors(
    primary = Primary(),
    secondary = Secondary(),
    neutral = Neutral(),
    success = Success(),
    warning = Warning(),
    error = Error(),
    info = Info(),
    extra = Extra(),
    chart = Chart()
)

fun darkColors() = DSColors(
    primary = Primary(
        primary100 = DarkPrimary100,
        primary80 = DarkPrimary80,
        primary60 = DarkPrimary60,
        primary40 = DarkPrimary40,
        primary20 = DarkPrimary20
    ),
    secondary = Secondary(
        secondary100 = DarkSecondary100,
        secondary80 = DarkSecondary80,
        secondary60 = DarkSecondary60,
        secondary40 = DarkSecondary40,
        secondary20 = DarkSecondary20
    ),
    neutral = Neutral(
        neutral100 = DarkNeutral100,
        neutral80 = DarkNeutral80,
        neutral60 = DarkNeutral60,
        neutral40 = DarkNeutral40,
        neutral20 = DarkNeutral20,
        neutral0 = DarkNeutral0
    ),
    success = Success(
        success100 = DarkSuccess100,
        success80 = DarkSuccess80,
        success60 = DarkSuccess60,
        success40 = DarkSuccess40,
        success20 = DarkSuccess20
    ),
    warning = Warning(
        warning100 = DarkWarning100,
        warning80 = DarkWarning80,
        warning60 = DarkWarning60,
        warning40 = DarkWarning40,
        warning20 = DarkWarning20
    ),
    error = Error(
        error100 = DarkError100,
        error80 = DarkError80,
        error60 = DarkError60,
        error40 = DarkError40,
        error20 = DarkError20
    ),
    info = Info(
        info100 = DarkInfo100,
        info80 = DarkInfo80,
        info60 = DarkInfo60,
        info40 = DarkInfo40,
        info20 = DarkInfo20
    ),
    extra = Extra(
        background = DarkBackground0,
        surface = DarkNeutral0,
        onSurface = DarkNeutral100,
        white = Color.White,
        black = Color.Black,
        transparent = Color.Transparent
    ),
    chart = Chart()
)

val LocalDSColors = staticCompositionLocalOf { DSColors() }