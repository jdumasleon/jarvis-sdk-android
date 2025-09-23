@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)

package com.jarvis.core.internal.designsystem.component

import androidx.annotation.RestrictTo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.jarvis.core.internal.designsystem.icons.DSIcons
import com.jarvis.core.internal.designsystem.theme.DSJarvisTheme
import com.jarvis.core.internal.designsystem.theme.Error100
import com.jarvis.core.internal.designsystem.theme.Error20
import com.jarvis.core.internal.designsystem.theme.Error80
import com.jarvis.core.internal.designsystem.theme.Info100
import com.jarvis.core.internal.designsystem.theme.Info20
import com.jarvis.core.internal.designsystem.theme.Info80
import com.jarvis.core.internal.designsystem.theme.Neutral0
import com.jarvis.core.internal.designsystem.theme.Neutral100
import com.jarvis.core.internal.designsystem.theme.Primary100
import com.jarvis.core.internal.designsystem.theme.Success100
import com.jarvis.core.internal.designsystem.theme.Success20
import com.jarvis.core.internal.designsystem.theme.Success80
import com.jarvis.core.internal.designsystem.theme.Warning100
import com.jarvis.core.internal.designsystem.theme.Warning20
import com.jarvis.core.internal.designsystem.theme.Warning80
import com.jarvis.core.internal.designsystem.theme.JarvisPink
import com.jarvis.core.internal.designsystem.theme.JarvisBlue

@Composable
fun DSFlag(
    modifier: Modifier = Modifier,
    title: String? = null,
    description: String? = null,
    style: FlagStyle? = null,
    closable: Boolean = false,
    onClose: (() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null
) {
    var isVisible by remember { mutableStateOf(true) }

    AnimatedVisibility(visible = isVisible) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(DSJarvisTheme.shapes.s)
                .background(style?.backgroundColor ?: Color.White)
                .padding(DSJarvisTheme.spacing.m),
        ) {
            Column {
                Header(
                    title = title,
                    style = style,
                    closable = closable,
                    onClose = {
                        isVisible = false
                        onClose?.invoke()
                    }
                )

                description?.let {
                    Description(text = it, style = style)
                }

                content?.invoke()
            }
        }
    }
}

@Composable
private fun Header(
    title: String?,
    style: FlagStyle?,
    closable: Boolean,
    onClose: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        style?.iconResource?.let {
            DSIcon(
                imageVector = it,
                contentDescription = "DSFlag leading header icon",
                tint = DSIconTint.Solid(style.iconTint),
                modifier = Modifier.size(DSJarvisTheme.dimensions.l)
            )
        }

        Spacer(modifier = Modifier.width(DSJarvisTheme.spacing.xxs))

        title?.let {
            DSText(
                text = it,
                color = style?.titleTextColor ?: Color.Black,
                style = DSJarvisTheme.typography.body.medium,
                modifier = Modifier.weight(1f)
            )
        }

        if (closable) {
            DSIcon(
                imageVector = DSIcons.Rounded.close,
                contentDescription = "DSFlag trailing header close icon",
                tint = DSIconTint.Solid(style?.iconTint ?: DSJarvisTheme.colors.neutral.neutral100),
                modifier = Modifier.size(DSJarvisTheme.dimensions.l)
                    .clickable(onClick = onClose)
            )
        }
    }
}

@Composable
private fun Description(
    text: String,
    style: FlagStyle?
) {
    Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.xxs))

    DSText(
        text = text,
        color = style?.descriptionTextColor ?: DSJarvisTheme.colors.neutral.neutral100,
        style = DSJarvisTheme.typography.body.small,
    )
}

sealed class FlagStyle(
    val backgroundColor: Color,
    val titleTextColor: Color,
    val descriptionTextColor: Color,
    val iconResource: ImageVector,
    val iconTint: Color
) {
    data object Error : FlagStyle(
        backgroundColor = Error20,
        titleTextColor = Error100,
        descriptionTextColor = Error100,
        iconResource = DSIcons.Outlined.info,
        iconTint = Error80
    )
    data object Info : FlagStyle(
        backgroundColor = Info20,
        titleTextColor = Info100,
        descriptionTextColor = Info100,
        iconResource = DSIcons.Outlined.info,
        iconTint = Info80
    )
    data object Default : FlagStyle(
        backgroundColor = Success20,
        titleTextColor = Success100,
        descriptionTextColor = Success100,
        iconResource = DSIcons.Rounded.checkCircle,
        iconTint = Success80
    )
    data object Normal : FlagStyle(
        backgroundColor = Neutral0,
        titleTextColor = Neutral100,
        descriptionTextColor = Neutral100,
        iconResource = DSIcons.Outlined.info,
        iconTint = Primary100
    )
    data object Warning : FlagStyle(
        backgroundColor = Warning20,
        titleTextColor = Warning100,
        descriptionTextColor = Warning100,
        iconResource = DSIcons.Rounded.warning,
        iconTint = Warning80
    )
    
    data object Wealth : FlagStyle(
        backgroundColor = JarvisPink.copy(alpha = 0.1f),
        titleTextColor = JarvisPink,
        descriptionTextColor = JarvisBlue,
        iconResource = DSIcons.Rounded.checkCircle,
        iconTint = JarvisPink.copy(alpha = 0.8f)
    )
}

@Preview(showBackground = true, name = "All DSFlag styles")
@Composable
fun DSFlagPreviews() {
    DSJarvisTheme {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m)
        ) {
            DSFlag(
                title = "Error Title",
                description = "This is an error description.",
                style = FlagStyle.Error,
                closable = true
            )

            DSFlag(
                title = "Info Title",
                description = "This is an info description.",
                style = FlagStyle.Info,
                closable = true
            )

            DSFlag(
                title = "Default Title",
                description = "This is a default description.",
                style = FlagStyle.Default,
                closable = true
            )

            DSFlag(
                title = "Normal Title",
                description = "This is a normal description.",
                style = FlagStyle.Normal,
                closable = true
            )

            DSFlag(
                title = "Warning Title",
                description = "This is a warning description.",
                style = FlagStyle.Warning,
                closable = true
            )

            DSFlag(
                title = "Wealth Dashboard",
                description = "Welcome to your comprehensive analytics overview. Track performance, monitor network activity, and optimize your app's health in real-time.",
                style = FlagStyle.Wealth,
                closable = true
            )
        }
    }
}