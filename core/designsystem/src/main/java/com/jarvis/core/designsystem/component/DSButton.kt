package com.jarvis.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.jarvis.core.designsystem.component.DSButtonStyle.*
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.designsystem.theme.Neutral0
import com.jarvis.core.designsystem.theme.Neutral40
import com.jarvis.core.designsystem.theme.Neutral80
import com.jarvis.core.designsystem.theme.Primary100

enum class DSButtonStyle {
    PRIMARY, SECONDARY, OUTLINE, TEXT, LINK
}

enum class DSButtonSize {
    SMALL, MEDIUM, LARGE;

    val height: Dp
        @Composable
        get() = when(this) {
            SMALL -> DSJarvisTheme.dimensions.xxxl
            MEDIUM -> DSJarvisTheme.dimensions.xxxxl
            LARGE -> DSJarvisTheme.dimensions.xxxxxl
        }

    val cornerRadius: CornerBasedShape
        @Composable
        get() = when(this) {
            SMALL -> DSJarvisTheme.shapes.xs
            LARGE, MEDIUM -> DSJarvisTheme.shapes.s
        }
}

@Composable
fun DSButton(
    text: String,
    modifier: Modifier = Modifier,
    style: DSButtonStyle = PRIMARY,
    size: DSButtonSize = DSButtonSize.MEDIUM,
    textColor: Color? = null,
    elevation: Dp? = null,
    disabled: Boolean = false,
    @DrawableRes leftIcon: Int? = null,
    @DrawableRes rightIcon: Int? = null,
    onClick: () -> Unit
) {
    val backgroundColor = when (style) {
        PRIMARY -> if (disabled) Neutral40 else Primary100
        SECONDARY -> if (disabled) Neutral40 else Neutral0
        OUTLINE, TEXT, LINK -> Color.Transparent
    }
    val _textColor = textColor ?: when (style) {
        PRIMARY -> if (disabled) Neutral80 else Neutral0
        SECONDARY, OUTLINE -> if (disabled) Neutral80 else Primary100
        TEXT, LINK -> if (disabled) Neutral80 else Primary100
    }

    val borderColor = if (style == OUTLINE) Neutral80 else Color.Transparent
    val textDecoration = if (style == LINK) TextDecoration.Underline else TextDecoration.None

    Button(
        onClick = { if (!disabled) onClick() },
        modifier = modifier
            .height(size.height)
            .shadow(elevation = elevation ?: DSJarvisTheme.elevations.none, shape = size.cornerRadius)
            .background(backgroundColor, size.cornerRadius)
            .border(DSJarvisTheme.dimensions.xxs, borderColor, size.cornerRadius),
        enabled = !disabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = _textColor,
            disabledContainerColor = backgroundColor,
            disabledContentColor = _textColor
        ),
        shape = size.cornerRadius
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            leftIcon?.let {
                DSIcon(
                    imageVector = ImageVector.vectorResource(id = it),
                    contentDescription = "DSButton left icon",
                    tint = _textColor
                )
            }
            Spacer(modifier = Modifier.width(DSJarvisTheme.spacing.xxs))
            DSText(
                text = text,
                color = _textColor,
                style = DSJarvisTheme.typography.body.medium,
                textDecoration = textDecoration,
            )
            Spacer(modifier = Modifier.width(DSJarvisTheme.spacing.xxs))
            rightIcon?.let {
                DSIcon(
                    imageVector = ImageVector.vectorResource(id = it),
                    contentDescription = "DSButton right icon",
                    tint = _textColor
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "All DSButton styles")
@Composable
fun DSButtonPreview() {
    DSJarvisTheme {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
        ) {
            Text("Primary Buttons", fontWeight = FontWeight.Bold)
            DSButton(
                modifier = Modifier.fillMaxWidth(),
                elevation = DSJarvisTheme.elevations.level2,
                text = "Primary",
                style = PRIMARY,
                onClick = {}
            )
            Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.xxs))
            DSButton(modifier = Modifier.fillMaxWidth(), text = "Disabled", style = PRIMARY, disabled = true, onClick = {})
            Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.xxs))

            Text("Secondary Buttons", fontWeight = FontWeight.Bold)
            DSButton(modifier = Modifier.fillMaxWidth(), elevation = DSJarvisTheme.elevations.level2, text = "Secondary", style = SECONDARY, onClick = {})
            Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.xxs))
            DSButton(modifier = Modifier.fillMaxWidth(), text = "Disabled", style = SECONDARY, disabled = true, onClick = {})
            Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.xxs))

            Text("Outline Buttons", fontWeight = FontWeight.Bold)
            DSButton(modifier = Modifier.fillMaxWidth(), text = "Outline", style = OUTLINE, onClick = {})
            Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.xxs))
            DSButton(modifier = Modifier.fillMaxWidth(), text = "Disabled", style = OUTLINE, disabled = true, onClick = {})
            Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.xxs))

            Text("Text Buttons", fontWeight = FontWeight.Bold)
            DSButton(modifier = Modifier.fillMaxWidth(), text = "Text", style = TEXT, onClick = {})
            Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.xxs))
            DSButton(modifier = Modifier.fillMaxWidth(), text = "Disabled", style = TEXT, disabled = true, onClick = {})
            Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.xxs))

            Text("Link Buttons", fontWeight = FontWeight.Bold)
            DSButton(modifier = Modifier.fillMaxWidth(), text = "Link", style = LINK, onClick = {})
            Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.xxs))
            DSButton(modifier = Modifier.fillMaxWidth(), text = "Disabled", style = LINK, disabled = true, onClick = {})
        }
    }
}