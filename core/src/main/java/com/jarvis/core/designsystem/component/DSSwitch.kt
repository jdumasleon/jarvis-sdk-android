package com.jarvis.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.jarvis.core.R
import com.jarvis.core.designsystem.theme.DSJarvisTheme

@Composable
fun DSSwitch(
    checked: Boolean,
    modifier: Modifier = Modifier,
    isInvalid: Boolean = false,
    isDisabled: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit),
    label: (@Composable () -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(vertical = DSJarvisTheme.spacing.xxs)
    ) {
        Switch(
            checked = checked,
            onCheckedChange = { if (!isDisabled) onCheckedChange(it) },
            thumbContent = if (checked) {
                {
                    DSIcon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_ellipse),
                        tint = DSIconTint.Solid(if (isDisabled) DSJarvisTheme.colors.neutral.neutral20 else DSJarvisTheme.colors.neutral.neutral0),
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize),
                    )
                }
            } else {
                {
                    DSIcon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_ellipse),
                        tint = DSIconTint.Solid(if (isDisabled) DSJarvisTheme.colors.neutral.neutral20 else DSJarvisTheme.colors.neutral.neutral0),
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize),
                    )
                }
            },
            enabled = !isDisabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = DSJarvisTheme.colors.neutral.neutral0,
                uncheckedThumbColor = DSJarvisTheme.colors.neutral.neutral0,
                disabledCheckedThumbColor = DSJarvisTheme.colors.neutral.neutral20,
                disabledUncheckedThumbColor = DSJarvisTheme.colors.neutral.neutral20,
                checkedTrackColor = if (isInvalid) DSJarvisTheme.colors.error.error100 else DSJarvisTheme.colors.primary.primary100,
                uncheckedTrackColor = DSJarvisTheme.colors.neutral.neutral40,
                disabledCheckedTrackColor = DSJarvisTheme.colors.neutral.neutral40,
                disabledUncheckedTrackColor = DSJarvisTheme.colors.neutral.neutral40,
                uncheckedBorderColor = DSJarvisTheme.colors.neutral.neutral40,
                checkedBorderColor = if (isInvalid) DSJarvisTheme.colors.error.error100 else DSJarvisTheme.colors.primary.primary100,
                disabledCheckedBorderColor = DSJarvisTheme.colors.neutral.neutral40,
                disabledUncheckedBorderColor = DSJarvisTheme.colors.neutral.neutral40,
            )
        )

        label?.let {
            Spacer(modifier = Modifier.width(DSJarvisTheme.spacing.xxs))
            it.invoke()
            Spacer(modifier = Modifier.width(DSJarvisTheme.spacing.xxs))
        }
    }
}

@Preview(showBackground = true, name = "DSSwitch")
@Composable
fun DSSwitchPreview() {
    DSJarvisTheme {
        val checkedState = remember { mutableStateOf(true) }
        val unCheckedState = remember { mutableStateOf(false) }
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.xxs)
        ) {
            DSSwitch(
                checked = checkedState.value,
                onCheckedChange = { checked -> checkedState.value = checked},
                label = { DSText(text = "state=on, isInvalid=false, isDisabled=false") }
            )
            DSSwitch(
                checked = checkedState.value,
                onCheckedChange = { checked -> checkedState.value = checked},
                isInvalid = true,
                label = { DSText(text = "state=on, isInvalid=true, isDisabled=false") }
            )
            DSSwitch(
                checked = unCheckedState.value,
                onCheckedChange = { checked -> unCheckedState.value = checked},
                label = { DSText(text = "state=off, isInvalid=false, isDisabled=false") }
            )
            DSSwitch(
                checked = unCheckedState.value,
                onCheckedChange = { checked -> unCheckedState.value = checked},
                isDisabled = true,
                label = { DSText(text = "state=on, isInvalid=false, isDisabled=true") }
            )
            DSSwitch(
                checked = checkedState.value,
                onCheckedChange = { checked -> checkedState.value = checked},
                isDisabled = true,
                label = { DSText(text = "state=off, isInvalid=false, isDisabled=true") }
            )
            DSSwitch(
                checked = unCheckedState.value,
                onCheckedChange = { checked -> unCheckedState.value = checked},
            )
        }
    }
}