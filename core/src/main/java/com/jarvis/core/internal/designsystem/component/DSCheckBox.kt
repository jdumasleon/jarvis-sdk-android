@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)

package com.jarvis.core.internal.designsystem.component

import androidx.annotation.RestrictTo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jarvis.core.internal.designsystem.theme.DSJarvisTheme

@Composable
fun DSCheckBox(
    checked: Boolean,
    modifier: Modifier = Modifier,
    isInvalid: Boolean = false,
    isDisabled: Boolean = false,
    onCheckedChange: (Boolean) -> Unit,
    label: (@Composable () -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(vertical = DSJarvisTheme.spacing.xxs)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onCheckedChange(it) },
            enabled = !isDisabled,
            colors = CheckboxDefaults.colors(
                checkedColor = if(isInvalid) DSJarvisTheme.colors.error.error100 else DSJarvisTheme.colors.primary.primary100,
                uncheckedColor = DSJarvisTheme.colors.neutral.neutral60,
                checkmarkColor = DSJarvisTheme.colors.neutral.neutral0,
                disabledCheckedColor = DSJarvisTheme.colors.neutral.neutral40,
                disabledUncheckedColor = DSJarvisTheme.colors.neutral.neutral40
            )
        )

        label?.let {
            Spacer(modifier = Modifier.width(DSJarvisTheme.spacing.xxs))
            it.invoke()
            Spacer(modifier = Modifier.width(DSJarvisTheme.spacing.xxs))
        }
    }
}

@Preview(showBackground = true, name = "All DSCheckBox states")
@Composable
fun DSCheckBoxPreview() {
    DSJarvisTheme {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.xxs)
        ) {
            DSCheckBox(
                checked = true,
                onCheckedChange = {},
                label = { DSText(text = "state=checked, isInvalid=false, isDisabled=false") }
            )
            DSCheckBox(
                checked = true,
                isInvalid = true,
                onCheckedChange = {},
                label = { DSText(text = "state=checked, isInvalid=true, isDisabled=false") }
            )
            DSCheckBox(
                checked = false,
                onCheckedChange = {},
                label = { DSText(text = "state=unchecked, isInvalid=false, isDisabled=false") }
            )
            DSCheckBox(
                checked = true,
                isDisabled = true,
                onCheckedChange = {},
                label = { DSText(text = "state=checked, isInvalid=false, isDisabled=true") }
            )
            DSCheckBox(
                checked = false,
                isDisabled = true,
                onCheckedChange = {},
                label = { DSText(text = "state=unchecked, isInvalid=false, isDisabled=true") }
            )
            DSCheckBox(
                checked = false,
                onCheckedChange = {},
            )
        }
    }
}