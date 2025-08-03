package com.jarvis.core.designsystem.component

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.jarvis.core.designsystem.R
import com.jarvis.core.designsystem.icons.DSIcons
import com.jarvis.core.designsystem.theme.DSJarvisTheme

@Composable
fun DSTextField(
    modifier: Modifier = Modifier,
    title: String? = null,
    placeholder: String,
    text: String = "",
    onValueChange: (String) -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Done,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    errorMessage: UiText? = null,
    isError: Boolean = false,
    isDisabled: Boolean = false,
    isMandatory: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    maxLine: Int = 1,
    isSecure: Boolean = false,
    appearance: DSTextFieldAppearance = DSTextFieldAppearance.default,
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val focusRequester = remember { FocusRequester() }
    val (isPasswordVisible, setPasswordVisible) = remember { mutableStateOf(isDisabled) }

    val borderWidth = if (isFocused) appearance.focusedBorderWidth else appearance.borderWidth

    Column(modifier) {
        title?.let{
            TextFieldTitle(title, isMandatory, appearance)
        }

        TextFieldContainer(
            text = text,
            placeholder = placeholder,
            onValueChange = onValueChange,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            isError = isError,
            isDisabled = isDisabled,
            isSecure = isSecure,
            isPasswordVisible = isPasswordVisible,
            setPasswordVisible = setPasswordVisible,
            appearance = appearance,
            borderWidth = borderWidth,
            keyboardType = keyboardType,
            imeAction = imeAction,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLine = maxLine,
            focusRequester = focusRequester,
            interactionSource = interactionSource
        )

        if (isError) {
            ErrorMessage(errorMessage, appearance, context)
        }
    }
}

@Composable
private fun TextFieldTitle(
    title: String,
    isMandatory: Boolean,
    appearance: DSTextFieldAppearance
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            style = appearance.titleTextStyle,
            color = appearance.titleColor
        )
        if (isMandatory) {
            Text(
                text = "*",
                style = appearance.mandatoryTextStyle,
                color = appearance.mandatoryTextColor,
                modifier = Modifier.padding(start = DSJarvisTheme.dimensions.xxs)
            )
        }
    }
}

@Composable
private fun TextFieldContainer(
    text: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    leadingIcon: @Composable (() -> Unit)?,
    trailingIcon: @Composable (() -> Unit)?,
    isError: Boolean,
    isDisabled: Boolean,
    isSecure: Boolean,
    isPasswordVisible: Boolean,
    setPasswordVisible: (Boolean) -> Unit,
    appearance: DSTextFieldAppearance,
    borderWidth: Dp,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    keyboardActions: KeyboardActions,
    singleLine: Boolean,
    maxLine: Int,
    focusRequester: FocusRequester,
    interactionSource: MutableInteractionSource
) {
    BasicTextField(
        modifier = Modifier
            .background(
                color = if (isDisabled) appearance.disabledBackgroundColor else appearance.backgroundColor,
                shape = appearance.cornerRadius
            )
            .border(
                width = borderWidth,
                color = when {
                    isError -> appearance.errorTextColor
                    isDisabled -> appearance.disabledColor
                    else -> appearance.borderColor
                },
                shape = appearance.cornerRadius
            )
            .focusRequester(focusRequester)
            .fillMaxWidth()
            .padding(horizontal = DSJarvisTheme.dimensions.xs)
            .padding(vertical = DSJarvisTheme.spacing.s),
        value = text,
        onValueChange = onValueChange,
        textStyle = appearance.titleTextStyle.copy(color =  if(isDisabled) appearance.disabledTextColor else appearance.textColor),
        maxLines = maxLine,
        singleLine = singleLine,
        interactionSource = interactionSource,
        visualTransformation = if (isSecure && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = !appearance.disableAutoCorrection,
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = keyboardActions,
        cursorBrush = SolidColor(appearance.textColor),
        enabled = !isDisabled,
        decorationBox = { innerTextField ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                leadingIcon?.invoke()

                Box(
                    modifier = Modifier
                        .weight(1.0f)
                        .padding(vertical = DSJarvisTheme.dimensions.xs)
                        .padding(horizontal = DSJarvisTheme.dimensions.xs)
                ) {
                    if (text.isEmpty()) {
                        DSText(
                            text = placeholder,
                            style = appearance.placeholderTextStyle,
                            color = when {
                                isDisabled -> appearance.disabledTextColor
                                else -> appearance.placeholderTextColor
                            }
                        )
                    }
                    innerTextField()
                }

                if (isSecure) {
                    SecureTextToggle(
                        isDisabled = isDisabled,
                        isPasswordVisible = isPasswordVisible,
                        setPasswordVisible = setPasswordVisible,
                        appearance = appearance
                    )
                }

                trailingIcon?.invoke()

                if (isError) {
                    ErrorIcon(isDisabled, appearance)
                }
            }
        }
    )
}

@Composable
private fun SecureTextToggle(
    isDisabled: Boolean,
    isPasswordVisible: Boolean,
    setPasswordVisible: (Boolean) -> Unit,
    appearance: DSTextFieldAppearance
) {
    val icon = if (isPasswordVisible) appearance.secureTextIconOpen else appearance.secureTextIconClose

    DSIcon(
        imageVector = ImageVector.vectorResource(id = icon),
        contentDescription = null,
        tint = when {
            isDisabled -> appearance.disabledColor
            else -> appearance.trailingIconForegroundColor
        },
        modifier = Modifier.size(DSJarvisTheme.dimensions.l)
            .padding(horizontal = DSJarvisTheme.dimensions.xxs)
            .clickable { setPasswordVisible(!isPasswordVisible) }
    )
}

@Composable
private fun ErrorMessage(
    errorMessage: UiText?,
    appearance: DSTextFieldAppearance,
    context: Context
) {
    DSText(
        modifier = Modifier.padding(vertical = DSJarvisTheme.dimensions.xxs),
        text = errorMessage?.asString(context) ?: "",
        style = appearance.errorTextStyle,
        color = appearance.errorTextColor,
    )
}

@Composable
private fun ErrorIcon(
    isDisabled: Boolean,
    appearance: DSTextFieldAppearance
) {
    DSIcon(
        modifier = Modifier
            .padding(horizontal = DSJarvisTheme.dimensions.xxs),
        imageVector = DSIcons.Outlined.info,
        contentDescription = null,
        tint = when {
            isDisabled -> appearance.disabledColor
            else -> appearance.errorTextColor
        }
    )
}

data class DSTextFieldAppearance(
    val textColor: Color,
    val titleColor: Color,
    val placeholderTextColor: Color,
    val disabledBackgroundColor: Color,
    val disabledColor: Color,
    val disabledTextColor: Color,
    val backgroundColor: Color,
    val errorTextColor: Color,
    val trailingIconForegroundColor: Color,
    val leadingIconForegroundColor: Color,
    val borderColor: Color,
    val focusedBorderColor: Color,
    val mandatoryTextColor: Color,

    val titleTextStyle: TextStyle,
    val errorTextStyle: TextStyle,
    val placeholderTextStyle: TextStyle,
    val mandatoryTextStyle: TextStyle,

    val borderWidth: Dp,
    val focusedBorderWidth: Dp,
    val cornerRadius: CornerBasedShape,
    val disableAutoCorrection: Boolean,
    val textFieldHeight: Dp,

    val secureTextIconOpen: Int,
    val secureTextIconClose: Int
) {
    companion object {
        val default: DSTextFieldAppearance
            @Composable
            get() = DSTextFieldAppearance(
                textColor = DSJarvisTheme.colors.neutral.neutral100,
                titleColor = DSJarvisTheme.colors.neutral.neutral100,
                placeholderTextColor = DSJarvisTheme.colors.neutral.neutral40,
                disabledBackgroundColor = DSJarvisTheme.colors.neutral.neutral20,
                disabledColor = DSJarvisTheme.colors.neutral.neutral20,
                disabledTextColor = DSJarvisTheme.colors.neutral.neutral40,
                backgroundColor = DSJarvisTheme.colors.neutral.neutral0,
                errorTextColor = DSJarvisTheme.colors.error.error100,
                trailingIconForegroundColor =  DSJarvisTheme.colors.primary.primary100,
                leadingIconForegroundColor =  DSJarvisTheme.colors.primary.primary100,
                borderColor = DSJarvisTheme.colors.neutral.neutral40,
                focusedBorderColor = DSJarvisTheme.colors.neutral.neutral100,
                mandatoryTextColor = DSJarvisTheme.colors.error.error80,

                titleTextStyle =  DSJarvisTheme.typography.body.medium,
                errorTextStyle = DSJarvisTheme.typography.body.small,
                placeholderTextStyle = DSJarvisTheme.typography.body.medium,
                mandatoryTextStyle = DSJarvisTheme.typography.body.medium,

                borderWidth = DSJarvisTheme.dimensions.xxs,
                focusedBorderWidth = DSJarvisTheme.dimensions.xxs,
                cornerRadius = DSJarvisTheme.shapes.s,
                disableAutoCorrection = false,
                textFieldHeight = DSJarvisTheme.dimensions.xxxxxl,

                secureTextIconOpen = R.drawable.ic_eye_open,
                secureTextIconClose = R.drawable.ic_eye_closed
            )

        val dark: DSTextFieldAppearance
            @Composable
            get() = DSTextFieldAppearance(
                textColor = DSJarvisTheme.colors.neutral.neutral100,
                titleColor = DSJarvisTheme.colors.neutral.neutral0,
                placeholderTextColor = DSJarvisTheme.colors.neutral.neutral40,
                disabledBackgroundColor = DSJarvisTheme.colors.neutral.neutral60,
                disabledColor = DSJarvisTheme.colors.neutral.neutral60,
                disabledTextColor = DSJarvisTheme.colors.neutral.neutral40,
                backgroundColor = DSJarvisTheme.colors.neutral.neutral0,
                errorTextColor = DSJarvisTheme.colors.error.error100,
                trailingIconForegroundColor = DSJarvisTheme.colors.primary.primary100,
                leadingIconForegroundColor = DSJarvisTheme.colors.primary.primary100,
                borderColor = DSJarvisTheme.colors.neutral.neutral0,
                focusedBorderColor = DSJarvisTheme.colors.neutral.neutral0,
                mandatoryTextColor = DSJarvisTheme.colors.error.error80,

                titleTextStyle =  DSJarvisTheme.typography.body.medium,
                errorTextStyle = DSJarvisTheme.typography.body.small,
                placeholderTextStyle = DSJarvisTheme.typography.body.medium,
                mandatoryTextStyle = DSJarvisTheme.typography.body.medium,

                borderWidth = DSJarvisTheme.dimensions.xxs,
                focusedBorderWidth = DSJarvisTheme.dimensions.xxs,
                cornerRadius = DSJarvisTheme.shapes.s,
                disableAutoCorrection = false,
                textFieldHeight = DSJarvisTheme.dimensions.xxxxxl,

                secureTextIconOpen = R.drawable.ic_eye_open,
                secureTextIconClose = R.drawable.ic_eye_closed
            )
    }
}

@Preview(showBackground = true, name = "All DSTextField states")
@Composable
fun DSTextFieldPreview() {
    val textState = remember { mutableStateOf("") }
    val errorMessage = UiText.StringResource(R.string.core_design_system_error_incorrect_password)

    Column(
        modifier = Modifier.padding(DSJarvisTheme.spacing.m),
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m)
    ) {
        DSTextField(
            title = "Default",
            placeholder = "Enter text",
            text = textState.value,
            onValueChange = { textState.value = it }
        )

        DSTextField(
            title = "Focused",
            placeholder = "Enter text",
            text = "Focused Text"
        )

        DSTextField(
            title = "Error",
            placeholder = "Enter text",
            text = "",
            isError = true,
            errorMessage = errorMessage
        )

        DSTextField(
            title = "Filled",
            placeholder = "Enter text",
            text = "Filled Text"
        )

        DSTextField(
            title = "Disabled",
            placeholder = "Cannot type",
            text = "Text",
            isDisabled = true
        )

        DSTextField(
            title = "Mandatory",
            placeholder = "Enter text",
            isMandatory = true,
            text = textState.value,
            onValueChange = { textState.value = it }
        )

        DSTextField(
            title = "Secure",
            placeholder = "Enter password",
            isSecure = true,
            text = "password",
            onValueChange = {}
        )

        DSTextField(
            title = "With Icons",
            placeholder = "Enter text",
            text = "Text with icons",
            leadingIcon = {
                DSIcon(
                    imageVector = DSIcons.dateRange,
                    contentDescription = "Leading DSIcon"
                )
            },
            trailingIcon = {
                DSIcon(
                    imageVector = DSIcons.arrowForward,
                    contentDescription = "Trailing DSIcon"
                )
            }
        )
    }
}


@Preview(showBackground = false, name = "All DSTextField dark states")
@Composable
fun DSTextFieldPreviewDark() {
    val textState = remember { mutableStateOf("") }
    val errorMessage = UiText.StringResource(R.string.core_design_system_error_incorrect_password)

    Column(
        modifier = Modifier.padding(DSJarvisTheme.spacing.m),
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m)
    ) {
        DSTextField(
            title = "Default",
            placeholder = "Enter text",
            text = textState.value,
            onValueChange = { textState.value = it },
            appearance = DSTextFieldAppearance.dark
        )

        DSTextField(
            title = "Error",
            placeholder = "Enter text",
            text = "",
            isError = true,
            errorMessage = errorMessage,
            appearance = DSTextFieldAppearance.dark
        )

        DSTextField(
            title = "Filled",
            placeholder = "Enter text",
            text = "Filled Text",
            appearance = DSTextFieldAppearance.dark
        )

        DSTextField(
            title = "Disabled",
            placeholder = "Cannot type",
            text = "",
            isDisabled = true,
            appearance = DSTextFieldAppearance.dark
        )

        DSTextField(
            title = "Mandatory",
            placeholder = "Enter text",
            isMandatory = true,
            text = textState.value,
            onValueChange = { textState.value = it },
            appearance = DSTextFieldAppearance.dark
        )

        DSTextField(
            title = "Secure",
            placeholder = "Enter password",
            isSecure = true,
            text = "password",
            onValueChange = {},
            appearance = DSTextFieldAppearance.dark
        )

        DSTextField(
            title = "With Icons",
            placeholder = "Enter text",
            text = "Text with icons",
            leadingIcon = {
                DSIcon(
                    imageVector = DSIcons.dateRange,
                    contentDescription = "Leading DSIcon",
                    tint = DSTextFieldAppearance.dark.leadingIconForegroundColor
                )
            },
            trailingIcon = {
                DSIcon(
                    imageVector = DSIcons.arrowForward,
                    contentDescription = "Trailing DSIcon",
                    tint = DSTextFieldAppearance.dark.trailingIconForegroundColor
                )
            },
            appearance = DSTextFieldAppearance.dark
        )
    }
}

