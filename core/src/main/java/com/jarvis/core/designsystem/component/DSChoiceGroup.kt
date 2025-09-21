package com.jarvis.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jarvis.core.designsystem.theme.DSJarvisTheme

@Composable
fun <T> DSChoiceGroup(
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    isHorizontal: Boolean = false,
    itemContent: @Composable (T, Boolean, () -> Unit) -> Unit
) {
    val layout: @Composable (@Composable () -> Unit) -> Unit = if (isHorizontal) {
        { content ->
            Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween) {
                content()
            }
        }
    } else {
        { content ->
            Column(modifier = modifier, verticalArrangement = Arrangement.SpaceBetween) {
                content()
            }
        }
    }

    layout {
        options.forEach { option ->
            val isSelected = option == selectedOption
            itemContent(option, isSelected) { onOptionSelected(option) }
        }
    }
}

@Preview(showBackground = true, name = "DSChoiceGroup with Radio vertical")
@Composable
fun DSChoiceGroupWithRadioVerticalPreview() {
    DSJarvisTheme {
        val options = listOf("Option 1", "Option 2", "Option 3")
        var selectedOption by remember { mutableStateOf<String?>(null) }

        DSChoiceGroup(
            options = options,
            selectedOption = selectedOption,
            onOptionSelected = { selectedOption = it }
        ) { option, isSelected, onSelected ->
            DSRadio(
                selected = isSelected,
                onSelectedChange = { onSelected() },
                label = { DSText(text = option) }
            )
        }
    }
}

@Preview(showBackground = true, name = "DSChoiceGroup with Radio horizontal")
@Composable
fun DSChoiceGroupWithRadioHorizontalPreview() {
    DSJarvisTheme {
        val options = listOf("Option 1", "Option 2", "Option 3")
        var selectedOption by remember { mutableStateOf<String?>(null) }

        DSChoiceGroup(
            options = options,
            selectedOption = selectedOption,
            onOptionSelected = { selectedOption = it },
            isHorizontal = true
        ) { option, isSelected, onSelected ->
            DSRadio(
                selected = isSelected,
                onSelectedChange = { onSelected() },
                label = { DSText(text = option) }
            )
        }
    }
}

@Preview(showBackground = true, name = "DSChoiceGroup with checkbox vertical")
@Composable
fun DSChoiceGroupWithCheckBoxVerticalPreview() {
    DSJarvisTheme {
        val options = listOf("Option A", "Option B", "Option C")
        var selectedOption by remember { mutableStateOf<String?>(null) }

        DSChoiceGroup(
            options = options,
            selectedOption = selectedOption,
            onOptionSelected = { selectedOption = it }
        ) { option, isSelected, onSelected ->
            DSCheckBox(
                checked = isSelected,
                onCheckedChange = { onSelected() },
                label = { DSText(text = option) }
            )
        }
    }
}

@Preview(showBackground = true, name = "DSChoiceGroup with checkbox horizontal")
@Composable
fun DSChoiceGroupWithCheckBoxHorizontalPreview() {
    DSJarvisTheme {
        val options = listOf("Option A", "Option B", "Option C")
        var selectedOption by remember { mutableStateOf<String?>(null) }

        DSChoiceGroup(
            options = options,
            selectedOption = selectedOption,
            onOptionSelected = { selectedOption = it },
            isHorizontal = true
        ) { option, isSelected, onSelected ->
            DSCheckBox(
                checked = isSelected,
                onCheckedChange = { onSelected() },
                label = { DSText(text = option) }
            )
        }
    }
}