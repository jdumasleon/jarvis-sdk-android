package com.jarvis.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toolingGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.constrain
import androidx.compose.ui.unit.dp
import com.jarvis.core.designsystem.icons.DSIcons
import com.jarvis.core.designsystem.theme.DSJarvisTheme

@Composable
fun DSIcon(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = DSJarvisTheme.colors.primary.primary60,
) {
    DSIcon(
        painter = rememberVectorPainter(imageVector),
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint,
    )
}

@Composable
fun DSIcon(
    bitmap: ImageBitmap,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = DSJarvisTheme.colors.primary.primary100,
) {
    val painter = remember(bitmap) { BitmapPainter(bitmap) }
    DSIcon(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint,
    )
}

@Composable
fun DSIcon(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = DSJarvisTheme.colors.primary.primary100,
) {
    val colorFilter = if (tint == Color.Unspecified) null else ColorFilter.tint(tint)
    val semantics = if (contentDescription != null) {
        Modifier.semantics {
            this.contentDescription = contentDescription
            this.role = Role.Image
        }
    } else {
        Modifier
    }
    val lineHeight = LocalTextStyle.current.lineHeight
    val sizeModifier = when (lineHeight.type) {
        TextUnitType.Sp -> DefaultSizeModifier(lineHeight)
        else -> Modifier
    }
    Box(
        modifier
            .toolingGraphicsLayer()
            .then(sizeModifier)
            .paint(
                painter = painter,
                colorFilter = colorFilter,
                contentScale = ContentScale.Fit,
            )
            .then(semantics),
    )
}

private data class DefaultSizeModifier(
    private val defaultSize: TextUnit,
) : LayoutModifier {
    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val size = defaultSize.roundToPx()
        val scaledConstraints = constraints.constrain(Constraints.fixed(size, size))
        val placeable = measurable.measure(scaledConstraints)
        return layout(placeable.width, placeable.height) {
            placeable.placeRelative(0, 0)
        }
    }

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(measurable: IntrinsicMeasurable, width: Int): Int =
        defaultSize.roundToPx()

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(measurable: IntrinsicMeasurable, height: Int): Int =
        defaultSize.roundToPx()

    override fun IntrinsicMeasureScope.minIntrinsicHeight(measurable: IntrinsicMeasurable, width: Int): Int =
        defaultSize.roundToPx()

    override fun IntrinsicMeasureScope.minIntrinsicWidth(measurable: IntrinsicMeasurable, height: Int): Int =
        defaultSize.roundToPx()
}

@Preview(showBackground = true, name = "Icons previews")
@Composable
internal fun IconPreview() {
    DSJarvisTheme {
        val calendarIcon = DSIcons.home
        val sparklesIcon = DSIcons.person

        Column {
            Row {
                DSIcon(calendarIcon, contentDescription = null)
                DSIcon(calendarIcon, contentDescription = null, modifier = Modifier.size(20.dp))
                DSIcon(calendarIcon, contentDescription = null, modifier = Modifier.size(24.dp))
                Box(Modifier.size(16.dp)) {
                    DSIcon(calendarIcon, contentDescription = null)
                }
            }

            Row {
                DSIcon(sparklesIcon, contentDescription = null)
                DSIcon(sparklesIcon, contentDescription = null, modifier = Modifier.size(22.dp))
                DSIcon(sparklesIcon, contentDescription = null, modifier = Modifier.size(26.dp))
                DSIcon(ImageBitmap(
                    width = 300,
                    height = 300,
                    config = ImageBitmapConfig.Argb8888,
                    hasAlpha = true,
                    colorSpace = ColorSpaces.Srgb
                ), contentDescription = null, modifier = Modifier.size(26.dp))
            }
        }
    }
}
