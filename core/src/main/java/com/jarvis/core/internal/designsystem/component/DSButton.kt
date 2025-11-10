@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)

package com.jarvis.core.internal.designsystem.component

import androidx.annotation.RestrictTo

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.jarvis.core.internal.designsystem.component.DSButtonStyle.*
import com.jarvis.core.internal.designsystem.theme.DSJarvisTheme
import com.jarvis.core.internal.designsystem.theme.Neutral0
import com.jarvis.core.internal.designsystem.theme.Neutral40
import com.jarvis.core.internal.designsystem.theme.Neutral80
import com.jarvis.core.internal.designsystem.theme.Primary100
import com.jarvis.core.R
import com.jarvis.core.internal.designsystem.theme.Error100
import com.jarvis.core.internal.designsystem.theme.Error60

enum class DSButtonStyle { PRIMARY, SECONDARY, OUTLINE, TEXT, LINK, DESTRUCTIVE }

enum class DSButtonSize {
    EXTRA_SMALL, SMALL, MEDIUM, LARGE;

    val height: Dp
        @Composable
        get() = when (this) {
            EXTRA_SMALL -> DSJarvisTheme.dimensions.xl
            SMALL -> DSJarvisTheme.dimensions.xxxl
            MEDIUM -> DSJarvisTheme.dimensions.xxxxl
            LARGE -> DSJarvisTheme.dimensions.xxxxxl
        }

    val cornerRadius: CornerBasedShape
        @Composable
        get() = when (this) {
            EXTRA_SMALL, SMALL -> DSJarvisTheme.shapes.xs
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
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onClick: () -> Unit
) {
    val backgroundColor = when (style) {
        PRIMARY -> if (disabled || isLoading) Neutral40 else Primary100
        SECONDARY -> if (disabled || isLoading) Neutral40 else Neutral0
        DESTRUCTIVE -> if (disabled || isLoading) Neutral40 else Error100
        OUTLINE, TEXT, LINK -> Color.Transparent
    }
    val color = textColor ?: when (style) {
        PRIMARY -> if (disabled || isLoading) Neutral80 else Neutral0
        SECONDARY, OUTLINE -> if (disabled || isLoading) Neutral80 else Primary100
        DESTRUCTIVE -> if (disabled || isLoading) Neutral80 else Neutral0
        TEXT, LINK -> if (disabled || isLoading) Neutral80 else Primary100
    }

    val borderColor = if (style == OUTLINE) Neutral80 else Color.Transparent
    val textDecoration = if (style == LINK) TextDecoration.Underline else TextDecoration.None
    val effectiveDisabled = disabled || isLoading

    Button(
        onClick = { if (!effectiveDisabled) onClick() },
        modifier = modifier
            .height(size.height)
            .shadow(elevation = elevation ?: DSJarvisTheme.elevations.none, shape = size.cornerRadius)
            .background(backgroundColor, size.cornerRadius)
            .border(DSJarvisTheme.dimensions.xxs, borderColor, size.cornerRadius),
        enabled = !effectiveDisabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = color,
            disabledContainerColor = backgroundColor,
            disabledContentColor = color
        ),
        shape = size.cornerRadius
    ) {
        if (isLoading) {
            DSCircularProgressIndicator(
                modifier = Modifier.size(size.height * 0.5f),
                color = color,
                strokeWidth = DSJarvisTheme.dimensions.xs
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                leadingIcon?.let {
                    DSIcon(
                        imageVector = it,
                        contentDescription = "DSButton left icon",
                        tint = DSIconTint.Solid(color)
                    )
                    Spacer(modifier = Modifier.width(DSJarvisTheme.spacing.xxs))
                }
                DSText(
                    text = text,
                    color = color,
                    style = DSJarvisTheme.typography.body.medium,
                    textDecoration = textDecoration,
                )
                trailingIcon?.let {
                    Spacer(modifier = Modifier.width(DSJarvisTheme.spacing.xxs))
                    DSIcon(
                        imageVector = it,
                        contentDescription = "DSButton right icon",
                        tint = DSIconTint.Solid(color)
                    )
                }
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
                text = stringResource(R.string.ds_primary),
                style = PRIMARY,
                onClick = {}
            )
            DSButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.ds_primary),
                style = PRIMARY,
                isLoading = true, // 👈 ejemplo cargando
                onClick = {}
            )
            DSButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.ds_disabled),
                style = PRIMARY,
                disabled = true,
                onClick = {}
            )

            Text("Secondary Buttons", fontWeight = FontWeight.Bold)
            DSButton(
                modifier = Modifier.fillMaxWidth(),
                elevation = DSJarvisTheme.elevations.level2,
                text = stringResource(R.string.ds_secondary),
                style = SECONDARY,
                onClick = {}
            )
            DSButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.ds_secondary),
                style = SECONDARY,
                isLoading = true,
                onClick = {}
            )
            DSButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.ds_disabled),
                style = SECONDARY,
                disabled = true,
                onClick = {}
            )

            Text("Outline Buttons", fontWeight = FontWeight.Bold)
            DSButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.ds_outline),
                style = OUTLINE,
                onClick = {}
            )
            DSButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.ds_disabled),
                style = OUTLINE,
                disabled = true,
                onClick = {}
            )

            Text("Text Buttons", fontWeight = FontWeight.Bold)
            DSButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.ds_text),
                style = TEXT,
                onClick = {}
            )
            DSButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.ds_text),
                style = TEXT,
                size = DSButtonSize.EXTRA_SMALL,
                onClick = {}
            )
            DSButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.ds_disabled),
                style = TEXT,
                disabled = true,
                onClick = {}
            )

            Text("Link Buttons", fontWeight = FontWeight.Bold)
            DSButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.ds_link),
                style = LINK,
                onClick = {}
            )
            DSButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.ds_disabled),
                style = LINK,
                disabled = true,
                onClick = {}
            )
        }
    }
}

@Preview(
    showBackground = true,
    name = "All DSButton styles - Dark",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun DSButtonDarkPreview() {
    DSJarvisTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
        ) {
            Text("Primary Buttons", fontWeight = FontWeight.Bold)
            DSButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.ds_primary),
                style = PRIMARY,
                onClick = {}
            )
            DSButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.ds_primary),
                style = PRIMARY,
                isLoading = true,
                onClick = {}
            )
            DSButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.ds_disabled),
                style = PRIMARY,
                disabled = true,
                onClick = {}
            )

            Text("Secondary Buttons", fontWeight = FontWeight.Bold)
            DSButton(
                modifier = Modifier.fillMaxWidth(),
                elevation = DSJarvisTheme.elevations.level2,
                text = stringResource(R.string.ds_secondary),
                style = SECONDARY,
                onClick = {}
            )
            DSButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.ds_secondary),
                style = SECONDARY,
                isLoading = true,
                onClick = {}
            )
            DSButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.ds_disabled),
                style = SECONDARY,
                disabled = true,
                onClick = {}
            )

            Text("Outline Buttons", fontWeight = FontWeight.Bold)
            DSButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.ds_outline),
                style = OUTLINE,
                onClick = {}
            )
            DSButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.ds_disabled),
                style = OUTLINE,
                disabled = true,
                onClick = {}
            )

            Text("Text Buttons", fontWeight = FontWeight.Bold)
            DSButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.ds_text),
                style = TEXT,
                onClick = {}
            )
            DSButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.ds_text),
                style = TEXT,
                size = DSButtonSize.EXTRA_SMALL,
                onClick = {}
            )
            DSButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.ds_disabled),
                style = TEXT,
                disabled = true,
                onClick = {}
            )

            Text("Link Buttons", fontWeight = FontWeight.Bold)
            DSButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.ds_link),
                style = LINK,
                onClick = {}
            )
            DSButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.ds_disabled),
                style = LINK,
                disabled = true,
                onClick = {}
            )
        }
    }
}