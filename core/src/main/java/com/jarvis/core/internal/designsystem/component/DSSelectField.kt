@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.core.internal.designsystem.component

import androidx.annotation.RestrictTo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.jarvis.core.internal.designsystem.icons.DSIcons
import com.jarvis.core.internal.designsystem.component.DSIconTint
import com.jarvis.core.internal.designsystem.theme.DSJarvisTheme

/**
 * DSJarvis Select Field component
 * A reusable dropdown selection component that uses DSTextField with dropdown icons
 * 
 * @param T the type of items in the options list
 * @param options list of available options to select from
 * @param selectedOption currently selected option (nullable)
 * @param onSelectionChange callback when an option is selected
 * @param label text to display as field label (optional)
 * @param placeholder text to show when no selection is made
 * @param displayText function to extract display text from option objects
 * @param modifier modifier for styling the component
 * @param enabled whether the field is enabled for interaction
 */
@Composable
fun <T> DSSelectField(
    options: List<T>,
    selectedOption: T?,
    onSelectionChange: (T) -> Unit,
    displayText: (T) -> String,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "Select an option",
    enabled: Boolean = true
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        // Select field using TextField with trailing icon
        Box(
            modifier = Modifier.clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { 
                isDropdownExpanded = !isDropdownExpanded 
            }
        ) {
            DSTextField(
                text = selectedOption?.let(displayText) ?: "",
                onValueChange = { }, // Read-only field
                title = label,
                placeholder = placeholder,
                isDisabled = true, // Always disabled to prevent text input
                trailingIcon = {
                    DSIcon(
                        imageVector = if (isDropdownExpanded) DSIcons.arrowUp else DSIcons.arrowDown,
                        contentDescription = if (isDropdownExpanded) "Collapse dropdown" else "Expand dropdown",
                        tint = DSIconTint.Solid(if (enabled) DSJarvisTheme.colors.neutral.neutral80 else DSJarvisTheme.colors.neutral.neutral40)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                appearance = DSTextFieldAppearance.select
            )
            
            DSDropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false },
                items = options.map { option ->
                    DSDropdownMenuItem(
                        text = displayText(option),
                        onClick = {
                            onSelectionChange(option)
                            isDropdownExpanded = false
                        }
                    )
                }
            )
        }
    }
}

/**
 * Convenience overload for simple string-based options
 */
@Composable
fun DSSelectField(
    options: List<String>,
    selectedOption: String?,
    onSelectionChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "Select an option",
    enabled: Boolean = true
) {
    DSSelectField(
        options = options,
        selectedOption = selectedOption,
        onSelectionChange = onSelectionChange,
        displayText = { it },
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        enabled = enabled
    )
}

// Preview data classes for demonstration
private enum class ExampleType {
    STRING, INTEGER, BOOLEAN, FLOAT;
    
    override fun toString(): String = name
}

private data class ExampleOption(
    val id: Int,
    val name: String,
    val description: String
)

@Preview(showBackground = true, name = "String Options")
@Composable
private fun DSSelectFieldStringPreview() {
    DSJarvisTheme {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.m)
        ) {
            val options = listOf("Option 1", "Option 2", "Option 3", "Option 4")
            var selectedOption by remember { mutableStateOf<String?>(null) }

            DSSelectField(
                options = options,
                selectedOption = selectedOption,
                onSelectionChange = { selectedOption = it },
                label = "Choose Option",
                placeholder = "Select an option"
            )
        }
    }
}

@Preview(showBackground = true, name = "Enum Options")
@Composable
private fun DSSelectFieldEnumPreview() {
    DSJarvisTheme {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.m)
        ) {
            val options = ExampleType.entries
            var selectedOption by remember { mutableStateOf<ExampleType?>(null) }

            DSSelectField(
                options = options,
                selectedOption = selectedOption,
                onSelectionChange = { selectedOption = it },
                displayText = { it.name },
                label = "Data Type",
                placeholder = "Select data type"
            )
        }
    }
}

@Preview(showBackground = true, name = "Custom Object Options")
@Composable
private fun DSSelectFieldObjectPreview() {
    DSJarvisTheme {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.m)
        ) {
            val options = listOf(
                ExampleOption(1, "First Option", "Description for first option"),
                ExampleOption(2, "Second Option", "Description for second option"),
                ExampleOption(3, "Third Option", "Description for third option")
            )
            var selectedOption by remember { mutableStateOf<ExampleOption?>(null) }

            DSSelectField(
                options = options,
                selectedOption = selectedOption,
                onSelectionChange = { selectedOption = it },
                displayText = { "${it.name} (${it.id})" },
                label = "Custom Object",
                placeholder = "Select custom object"
            )
        }
    }
}

@Preview(showBackground = true, name = "Different States", heightDp = 400)
@Composable
private fun DSSelectFieldStatesPreview() {
    DSJarvisTheme {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.m)
        ) {
            val options = listOf("Option 1", "Option 2", "Option 3")
            var selectedOption1 by remember { mutableStateOf<String?>(null) }
            var selectedOption2 by remember { mutableStateOf<String?>("Option 2") }
            var selectedOption3 by remember { mutableStateOf<String?>(null) }

            DSSelectField(
                options = options,
                selectedOption = selectedOption1,
                onSelectionChange = { selectedOption1 = it },
                label = "Empty Selection",
                placeholder = "Choose an option",
                modifier = Modifier.padding(bottom = DSJarvisTheme.spacing.m)
            )

            DSSelectField(
                options = options,
                selectedOption = selectedOption2,
                onSelectionChange = { selectedOption2 = it },
                label = "With Selection",
                placeholder = "Choose an option",
                modifier = Modifier.padding(bottom = DSJarvisTheme.spacing.m)
            )

            DSSelectField(
                options = options,
                selectedOption = selectedOption3,
                onSelectionChange = { selectedOption3 = it },
                label = "Disabled State",
                placeholder = "Choose an option",
                enabled = false
            )
        }
    }
}

@Preview(showBackground = true, name = "Dark Theme", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DSSelectFieldDarkThemePreview() {
    DSJarvisTheme {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.m)
        ) {
            val options = listOf("Dark Option 1", "Dark Option 2", "Dark Option 3")
            var selectedOption by remember { mutableStateOf<String?>(null) }

            DSSelectField(
                options = options,
                selectedOption = selectedOption,
                onSelectionChange = { selectedOption = it },
                label = "Dark Theme Select",
                placeholder = "Choose option in dark mode"
            )
        }
    }
}