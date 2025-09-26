@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.core.internal.designsystem.component

import androidx.annotation.RestrictTo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jarvis.core.internal.designsystem.theme.DSJarvisTheme

@Composable
fun DSRadio(
    selected: Boolean,
    modifier: Modifier = Modifier,
    isInvalid: Boolean = false,
    isDisabled: Boolean = false,
    onSelectedChange: (Boolean) -> Unit,
    label: (@Composable () -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(vertical = DSJarvisTheme.spacing.xxs)
    ) {
        RadioButton(
            selected = selected,
            onClick = { onSelectedChange(!selected) },
            enabled = !isDisabled,
            colors = RadioButtonDefaults.colors(
                selectedColor = if(isInvalid) DSJarvisTheme.colors.error.error100 else DSJarvisTheme.colors.primary.primary100,
                unselectedColor = DSJarvisTheme.colors.neutral.neutral60,
                disabledSelectedColor = DSJarvisTheme.colors.neutral.neutral40,
                disabledUnselectedColor = DSJarvisTheme.colors.neutral.neutral40
            )
        )

        label?.let {
            Spacer(modifier = Modifier.width(DSJarvisTheme.spacing.xxs))
            it.invoke()
            Spacer(modifier = Modifier.width(DSJarvisTheme.spacing.xxs))
        }
    }
}

@Preview(showBackground = true, name = "DSRadio")
@Composable
fun DSRadioPreview() {
    DSJarvisTheme {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.xxs)
        ) {
            DSRadio(
                selected = true,
                onSelectedChange = {},
                label = { DSText(text = "state=checked, isInvalid=false, isDisabled=false") }
            )
            DSRadio(
                selected = true,
                isInvalid = true,
                onSelectedChange = {},
                label = { DSText(text = "state=checked, isInvalid=true, isDisabled=false") }
            )
            DSRadio(
                selected = false,
                onSelectedChange = {},
                label = { DSText(text = "state=unchecked, isInvalid=false, isDisabled=false") }
            )
            DSRadio(
                selected = true,
                isDisabled = true,
                onSelectedChange = {},
                label = { DSText(text = "state=checked, isInvalid=false, isDisabled=true") }
            )
            DSRadio(
                selected = false,
                isDisabled = true,
                onSelectedChange = {},
                label = { DSText(text = "state=unchecked, isInvalid=false, isDisabled=true") }
            )
            DSRadio(
                selected = false,
                onSelectedChange = {},
            )
        }
    }
}

