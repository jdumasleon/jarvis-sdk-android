@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)

package com.jarvis.core.internal.designsystem.component

import androidx.annotation.RestrictTo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.jarvis.core.internal.designsystem.component.DSIconTint
import com.jarvis.core.internal.designsystem.theme.DSJarvisTheme
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search

/**
 * DSJarvis Dropdown Menu component
 */
@Composable
fun DSDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<DSDropdownMenuItem>,
    modifier: Modifier = Modifier,
    backgroundColor: Color = DSJarvisTheme.colors.neutral.neutral0
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
            .background(
                backgroundColor,
                shape = DSJarvisTheme.shapes.s
            ),
    ) {
        items.forEach { item ->
            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = DSJarvisTheme.spacing.l)
                    ) {
                        DSText(
                            modifier = Modifier.weight(1f),
                            text = item.text,
                            style = DSJarvisTheme.typography.body.small,
                            color = if (item.enabled) item.textColor ?: DSJarvisTheme.colors.extra.black else DSJarvisTheme.colors.neutral.neutral40
                        )
                        Spacer(modifier = Modifier.width(DSJarvisTheme.dimensions.l))
                        if (item.icon != null) {
                            DSIcon(
                                imageVector = item.icon,
                                contentDescription = item.text,
                                size =DSJarvisTheme.dimensions.m,
                                tint = DSIconTint.Solid(if (item.enabled) item.iconTint ?: DSJarvisTheme.colors.extra.black else DSJarvisTheme.colors.neutral.neutral40)
                            )
                        }
                    }
                },
                onClick = {
                    item.onClick()
                    onDismissRequest()
                },
                enabled = item.enabled
            )
        }
    }
}

/**
 * DSJarvis Three Dots Menu Button with Dropdown
 */
@Composable
fun DSThreeDotsMenu(
    items: List<DSDropdownMenuItem>,
    modifier: Modifier = Modifier,
    iconTint: Color = DSJarvisTheme.colors.extra.black,
    backgroundColor: Color = DSJarvisTheme.colors.extra.white
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        DSIconButton(
            onClick = { expanded = !expanded },
            imageVector = Icons.Default.MoreVert,
            contentDescription = "More options",
            modifier = modifier.padding(DSJarvisTheme.spacing.none),
            tint = DSIconTint.Solid(iconTint)
        )

        DSDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            items = items,
            backgroundColor = backgroundColor
        )
    }
}

/**
 * Data class for dropdown menu items
 */
data class DSDropdownMenuItem(
    val text: String,
    val onClick: () -> Unit,
    val icon: ImageVector? = null,
    val iconTint: Color? = null,
    val textColor: Color? = null,
    val enabled: Boolean = true
)

@Preview(showBackground = true, name = "Three Dots Menu")
@Composable
private fun DSThreeDotsMenuPreview() {
    DSJarvisTheme {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.m)
        ) {
            DSThreeDotsMenu(
                items = listOf(
                    DSDropdownMenuItem(
                        text = "Copy to clipboard",
                        onClick = { },
                        icon = Icons.Default.ContentCopy
                    ),
                    DSDropdownMenuItem(
                        text = "Search content",
                        onClick = { },
                        icon = Icons.Default.Search
                    )
                )
            )
        }
    }
}

@Preview(showBackground = true, name = "Dropdown Menu")
@Composable
private fun DSDropdownMenuPreview() {
    DSJarvisTheme {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.m)
        ) {
            DSDropdownMenu(
                expanded = true,
                onDismissRequest = { },
                items = listOf(
                    DSDropdownMenuItem(
                        text = "Copy to clipboard",
                        onClick = { },
                        icon = Icons.Default.ContentCopy
                    ),
                    DSDropdownMenuItem(
                        text = "Search content",
                        onClick = { },
                        icon = Icons.Default.Search
                    )
                )
            )
        }
    }
}

@Preview(showBackground = true, name = "Dark Theme", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DSThreeDotsMenuDarkThemePreview() {
    DSJarvisTheme {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.m)
        ) {
            DSThreeDotsMenu(
                items = listOf(
                    DSDropdownMenuItem(
                        text = "Copy to clipboard",
                        onClick = { },
                        icon = Icons.Default.ContentCopy
                    ),
                    DSDropdownMenuItem(
                        text = "Search content",
                        onClick = { },
                        icon = Icons.Default.Search
                    )
                )
            )
        }
    }
}