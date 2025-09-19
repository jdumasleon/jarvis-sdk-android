package com.jarvis.core.designsystem.component

import android.app.DatePickerDialog
import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.jarvis.core.designsystem.R
import com.jarvis.core.designsystem.component.DSIconTint
import com.jarvis.core.designsystem.icons.DSIcons
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DSDatePicker(
    modifier: Modifier = Modifier,
    selectedDate: Date? = null,
    isError: Boolean = false,
    isDisabled: Boolean = false,
    errorMessage: UiText? = null,
    isMandatory: Boolean = false,
    leadingIcon: (@Composable (() -> Unit))? = null,
    title: String? = null,
    placeholder: String? = null,
    appearance: DSDatePickerAppearance = DSDatePickerAppearance.default,
    onDateSelected: (Date?) -> Unit = {}
) {
    val context = LocalContext.current
    val showDatePicker = remember { mutableStateOf(false) }

    Column(modifier) {
        title?.let {
            DatePickerTitle(
                title = it,
                isMandatory = isMandatory,
                appearance = appearance
            )
        }

        DatePickerField(
            selectedDate = selectedDate,
            isError = isError,
            isDisabled = isDisabled,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            appearance = appearance,
            onClick = { showDatePicker.value = true }
        )

        if (isError) {
            ErrorMessage(errorMessage, appearance, context)
        }

        if (showDatePicker.value) {
            ShowDatePickerDialog(
                selectedDate = selectedDate,
                context = context,
                onDismiss = { showDatePicker.value = false },
                onDateSelected = { selected ->
                    onDateSelected(selected)
                }
            )
        }
    }
}

@Composable
private fun DatePickerTitle(
    title: String,
    isMandatory: Boolean,
    appearance: DSDatePickerAppearance
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            style = appearance.titleFont,
            color = appearance.titleColor
        )
        if (isMandatory) {
            Text(
                text = "*",
                style = appearance.titleFont,
                color = appearance.mandatoryIndicatorColor,
                modifier = Modifier.padding(start = DSJarvisTheme.dimensions.xxs)
            )
        }
    }
}

@Composable
private fun DatePickerField(
    selectedDate: Date?,
    isError: Boolean,
    isDisabled: Boolean,
    placeholder: String?,
    leadingIcon: (@Composable (() -> Unit))?,
    appearance: DSDatePickerAppearance,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val displayText = selectedDate?.let { dateFormat.format(it) } ?: placeholder.orEmpty()

    Box(
        modifier = Modifier
            .background(
                color = if (isDisabled) appearance.disabledBackgroundColor else appearance.backgroundColor,
                shape = appearance.cornerRadius
            )
            .border(
                width = if (isError) appearance.focusedBorderWidth else appearance.borderWidth,
                color = when {
                    isError -> appearance.errorColor
                    isDisabled -> appearance.disabledColor
                    else -> appearance.borderColor
                },
                shape = appearance.cornerRadius
            )
            .clickable(enabled = !isDisabled) { onClick() }
            .fillMaxWidth()
            .padding(horizontal = DSJarvisTheme.spacing.xs)
            .padding(vertical = DSJarvisTheme.spacing.s),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if(isError) {
                    ErrorIcon(appearance)
                }

                leadingIcon?.invoke()

                Spacer(modifier = Modifier.width(DSJarvisTheme.dimensions.xxs))

                DSText(
                    text = displayText,
                    style = appearance.placeholderFont,
                    color = when {
                        isDisabled -> appearance.disabledTextColor
                        selectedDate == null -> appearance.placeholderColor
                        else -> appearance.textColor
                    }
                )
            }

            ExpandableIcon(appearance, isError, isDisabled)
        }
    }
}

@Composable
private fun ErrorMessage(
    errorMessage: UiText?,
    appearance: DSDatePickerAppearance,
    context: Context
) {
    DSText(
        modifier = Modifier.padding(vertical = DSJarvisTheme.dimensions.xxs),
        text = errorMessage?.asString(context) ?: "",
        style = appearance.errorFont,
        color = appearance.errorColor,
    )
}

@Composable
private fun ErrorIcon(
    appearance: DSDatePickerAppearance
) {
    DSIcon(
        modifier = Modifier.padding(horizontal = DSJarvisTheme.dimensions.xxs),
        imageVector = DSIcons.Outlined.info,
        contentDescription = null,
        tint = DSIconTint.Solid(appearance.errorColor)
    )
}

@Composable
private fun ExpandableIcon(
    appearance: DSDatePickerAppearance,
    isError: Boolean,
    isDisabled: Boolean
) {
    val icon = appearance.collapseIcon
    DSIcon(
        imageVector = icon,
        contentDescription = null,
        tint = DSIconTint.Solid(when {
            isError -> appearance.errorColor
            isDisabled -> appearance.disabledColor
            else -> appearance.textColor
        })
    )
}

@Composable
private fun ShowDatePickerDialog(
    selectedDate: Date?,
    context: Context,
    onDismiss: () -> Unit,
    onDateSelected: (Date?) -> Unit
) {
    val calendar = Calendar.getInstance()
    selectedDate?.let { calendar.time = it }

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            onDateSelected(calendar.time)
            onDismiss()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        setOnDismissListener { onDismiss() }
    }.show()
}

data class DSDatePickerAppearance(
    val titleColor: Color,
    val placeholderColor: Color,
    val backgroundColor: Color,
    val errorColor: Color,
    val borderColor: Color,
    val focusedBorderColor: Color,
    val textColor: Color,
    val disabledBackgroundColor: Color,
    val disabledColor: Color,
    val disabledTextColor: Color,
    val mandatoryIndicatorColor: Color,
    val titleFont: TextStyle,
    val placeholderFont: TextStyle,
    val errorFont: TextStyle,
    val borderWidth: Dp,
    val focusedBorderWidth: Dp,
    val cornerRadius: CornerBasedShape,
    val expandableIcon: ImageVector,
    val collapseIcon: ImageVector
) {
    companion object {
        val default: DSDatePickerAppearance
            @Composable
            get() = DSDatePickerAppearance(
                titleColor = DSJarvisTheme.colors.neutral.neutral100,
                placeholderColor = DSJarvisTheme.colors.neutral.neutral60,
                backgroundColor = DSJarvisTheme.colors.neutral.neutral0,
                errorColor = DSJarvisTheme.colors.error.error100,
                borderColor = DSJarvisTheme.colors.neutral.neutral40,
                focusedBorderColor = DSJarvisTheme.colors.neutral.neutral100,
                textColor = DSJarvisTheme.colors.neutral.neutral100,
                disabledBackgroundColor = DSJarvisTheme.colors.neutral.neutral20,
                disabledColor = DSJarvisTheme.colors.neutral.neutral20,
                disabledTextColor = DSJarvisTheme.colors.neutral.neutral40,
                mandatoryIndicatorColor = DSJarvisTheme.colors.error.error80,
                titleFont = DSJarvisTheme.typography.body.medium,
                placeholderFont = DSJarvisTheme.typography.body.medium,
                errorFont = DSJarvisTheme.typography.body.small,
                borderWidth = DSJarvisTheme.dimensions.xxs,
                focusedBorderWidth = DSJarvisTheme.dimensions.xxs,
                cornerRadius = DSJarvisTheme.shapes.s,
                expandableIcon = DSIcons.arrowUp,
                collapseIcon = DSIcons.arrowDown
            )

        val dark: DSDatePickerAppearance
            @Composable
            get() = DSDatePickerAppearance(
                titleColor = DSJarvisTheme.colors.neutral.neutral0,
                placeholderColor = DSJarvisTheme.colors.neutral.neutral40,
                backgroundColor = DSJarvisTheme.colors.neutral.neutral0,
                errorColor = DSJarvisTheme.colors.error.error100,
                borderColor = DSJarvisTheme.colors.neutral.neutral0,
                focusedBorderColor = DSJarvisTheme.colors.primary.primary100,
                textColor = DSJarvisTheme.colors.neutral.neutral100,
                disabledBackgroundColor = DSJarvisTheme.colors.neutral.neutral60,
                disabledColor = DSJarvisTheme.colors.neutral.neutral60,
                disabledTextColor = DSJarvisTheme.colors.neutral.neutral40,
                mandatoryIndicatorColor = DSJarvisTheme.colors.warning.warning100,
                titleFont = DSJarvisTheme.typography.body.medium,
                placeholderFont = DSJarvisTheme.typography.body.medium,
                errorFont = DSJarvisTheme.typography.body.small,
                borderWidth = DSJarvisTheme.dimensions.xxs,
                focusedBorderWidth = DSJarvisTheme.dimensions.xxs,
                cornerRadius = DSJarvisTheme.shapes.s,
                expandableIcon = DSIcons.arrowUp,
                collapseIcon = DSIcons.arrowDown
            )
    }
}

sealed class UiText {
    data class DynamicString(val value: String) : UiText()
    class StringResource(
        @param:StringRes val resId: Int,
        vararg val args: Any
    ) : UiText()

    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> stringResource(resId, *args)
        }
    }

    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> context.getString(resId, *args)
        }
    }
}

@Preview(showBackground = true, name = "All DSDatePicker states")
@Composable
fun DSDatePickerPreview() {
    val isError = remember { mutableStateOf(false) }
    val isMandatory = true
    val isDisabled = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(DSJarvisTheme.spacing.m),
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.l)
    ) {
        DSDatePicker()

        DSDatePicker(
            title = "Title - Default",
            placeholder = "Select date"
        )

        DSDatePicker(
            selectedDate = Date(),
            title = "Title - Focus"
        )

        DSDatePicker(
            isError = remember { mutableStateOf(true) }.value,
            errorMessage = UiText.StringResource(R.string.core_design_system_error_incorrect_password),
            isMandatory = isMandatory,
            title = "Title - Focus and empty"
        )

        DSDatePicker(
            selectedDate = Date(),
            title = "Title - Not focus and filled"
        )

        DSDatePicker(
            selectedDate = Date(),
            isError = remember { mutableStateOf(true) }.value,
            errorMessage = UiText.StringResource(R.string.core_design_system_error_incorrect_password),
            title = "Title - Error and unfocused"
        )

        DSDatePicker(
            selectedDate = Date(),
            isError = remember { mutableStateOf(true) }.value,
            errorMessage = UiText.StringResource(R.string.core_design_system_error_incorrect_password),
            leadingIcon = {
                DSIcon(
                    imageVector = DSIcons.dateRange,
                    contentDescription = "Error icon",
                    tint = DSIconTint.Solid(DSJarvisTheme.colors.primary.primary100)
                )
            },
            title = "Title - Error and focused"
        )

        DSDatePicker(
            isDisabled = remember { mutableStateOf(true) }.value,
            title = "Title - Disabled",
            placeholder = "Placeholder"
        )

        DSDatePicker(
            selectedDate = Date(),
            isError = isError.value,
            errorMessage = UiText.StringResource(R.string.core_design_system_error_incorrect_password),
            isDisabled = isDisabled.value,
            isMandatory = true,
            title = "Pick a Date",
            placeholder = "Select date",
            leadingIcon = {
                DSIcon(
                    imageVector = DSIcons.dateRange,
                    contentDescription = null,
                    tint = DSIconTint.Solid(DSJarvisTheme.colors.primary.primary100)
                )
            },
            appearance = DSDatePickerAppearance.default
        )
    }
}

@Preview(showBackground = false, name = "All Dark DSDatePicker states")
@Composable
fun DSDatePickerPreviewDark() {
    val isError = remember { mutableStateOf(false) }
    val isMandatory = true
    val isDisabled = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(DSJarvisTheme.spacing.m),
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.l)
    ) {
        DSDatePicker(appearance = DSDatePickerAppearance.dark)

        DSDatePicker(
            title = "Title - Default",
            placeholder = "Select date",
            appearance = DSDatePickerAppearance.dark
        )

        DSDatePicker(
            selectedDate = Date(),
            title = "Title - Focus",
            appearance = DSDatePickerAppearance.dark
        )

        DSDatePicker(
            isError = remember { mutableStateOf(true) }.value,
            errorMessage = UiText.StringResource(R.string.core_design_system_error_incorrect_password),
            isMandatory = isMandatory,
            title = "Title - Focus and empty",
            appearance = DSDatePickerAppearance.dark
        )

        DSDatePicker(
            selectedDate = Date(),
            title = "Title - Not focus and filled",
            appearance = DSDatePickerAppearance.dark
        )

        DSDatePicker(
            selectedDate = Date(),
            isError = remember { mutableStateOf(true) }.value,
            errorMessage = UiText.StringResource(R.string.core_design_system_error_incorrect_password),
            title = "Title - Error and unfocused",
            appearance = DSDatePickerAppearance.dark
        )

        DSDatePicker(
            selectedDate = Date(),
            isError = remember { mutableStateOf(true) }.value,
            errorMessage = UiText.StringResource(R.string.core_design_system_error_incorrect_password),
            leadingIcon = {
                DSIcon(
                    imageVector = DSIcons.dateRange,
                    contentDescription = "Error icon",
                    tint = DSIconTint.Solid(DSJarvisTheme.colors.error.error100)
                )
            },
            title = "Title - Error and focused",
            appearance = DSDatePickerAppearance.dark
        )

        DSDatePicker(
            isDisabled = remember { mutableStateOf(true) }.value,
            title = "Title - Disabled",
            placeholder = "Placeholder",
            appearance = DSDatePickerAppearance.dark
        )

        DSDatePicker(
            selectedDate = Date(),
            isError = isError.value,
            errorMessage = UiText.StringResource(R.string.core_design_system_error_incorrect_password),
            isDisabled = isDisabled.value,
            isMandatory = true,
            title = "Pick a Date",
            placeholder = "Select date",
            leadingIcon = {
                DSIcon(
                    imageVector = DSIcons.dateRange,
                    contentDescription = null,
                    tint = DSIconTint.Solid(DSJarvisTheme.colors.primary.primary100)
                )
            },
            appearance = DSDatePickerAppearance.dark
        )
    }
}