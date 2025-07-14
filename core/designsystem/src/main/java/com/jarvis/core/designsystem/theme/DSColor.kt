package com.jarvis.core.designsystem.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val Primary100 = Color(0xFF2752E7)
val Primary80 = Color(0xFF3756EC)
val Primary60 = Color(0xFF7D99F1)
val Primary40 = Color(0xFFD0D8FF)
val Primary20 = Color(0xFFF5F8FE)

val Secondary100 = Color(0xFF3F8A5F)
val Secondary80 = Color(0xFFFDB989)
val Secondary60 = Color(0xFFFEE8B6)
val Secondary40 = Color(0xFFFFF2D0)
val Secondary20 = Color(0xFFFFF8EF)

val Neutral100 = Color(0xFF000000)
val Neutral80 = Color(0xFF333333)
val Neutral60 = Color(0xFF666666)
val Neutral40 = Color(0xFF999999)
val Neutral20 = Color(0xFFD9D9D9)
val Neutral0 = Color(0xFFFFFFFF)

val Success100 = Color(0xFF3F8A5F)
val Success80 = Color(0xFF55977F)
val Success60 = Color(0xFF8CB59F)
val Success40 = Color(0xFFC8E0BF)
val Success20 = Color(0xFFDEE6DF)

val Warning100 = Color(0xFFE4C859)
val Warning80 = Color(0xFFEFD17C)
val Warning60 = Color(0xFFF0D0BD)
val Warning40 = Color(0xFFF4E0BD)
val Warning20 = Color(0xFFF4F4DE)

val Error100 = Color(0xFFEB5C5C)
val Error80 = Color(0xFFE57D7D)
val Error60 = Color(0xFFF18D8D)
val Error40 = Color(0xFFF3BEBE)
val Error20 = Color(0xFFF9EDE9)

val Info100 = Color(0xFF2661CA)
val Info80 = Color(0xFF417CB7)
val Info60 = Color(0xFF70B8D7)
val Info40 = Color(0xFFA5CCE2)
val Info20 = Color(0xFFDEF2F4)

data class DSColors(
    val primary: Primary = Primary(),
    val secondary: Secondary = Secondary(),
    val neutral: Neutral = Neutral(),
    val success: Success = Success(),
    val warning: Warning = Warning(),
    val error: Error = Error(),
    val info: Info = Info()
)

data class Primary(
    val primary100: Color = Primary100,
    val primary80: Color = Primary80,
    val primary60: Color = Primary60,
    val primary40: Color = Primary40,
    val primary20: Color = Primary20
)

data class Secondary(
    val secondary100: Color = Secondary100,
    val secondary80: Color = Secondary80,
    val secondary60: Color = Secondary60,
    val secondary40: Color = Secondary40,
    val secondary20: Color = Secondary20
)

data class Neutral(
    val neutral100: Color = Neutral100,
    val neutral80: Color = Neutral80,
    val neutral60: Color = Neutral60,
    val neutral40: Color = Neutral40,
    val neutral20: Color = Neutral20,
    val neutral0: Color = Neutral0
)

data class Success(
    val success100: Color = Success100,
    val success80: Color = Success80,
    val success60: Color = Success60,
    val success40: Color = Success40,
    val success20: Color = Success20
)

data class Warning(
    val warning100: Color = Warning100,
    val warning80: Color = Warning80,
    val warning60: Color = Warning60,
    val warning40: Color = Warning40,
    val warning20: Color = Warning20
)

data class Error(
    val error100: Color = Error100,
    val error80: Color = Error80,
    val error60: Color = Error60,
    val error40: Color = Error40,
    val error20: Color = Error20
)

data class Info(
    val info100: Color = Info100,
    val info80: Color = Info80,
    val info60: Color = Info60,
    val info40: Color = Info40,
    val info20: Color = Info20
)

val LocalDSColors = staticCompositionLocalOf { DSColors() }