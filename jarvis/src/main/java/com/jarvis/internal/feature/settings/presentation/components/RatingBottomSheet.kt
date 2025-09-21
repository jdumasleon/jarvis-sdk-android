package com.jarvis.internal.feature.settings.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jarvis.core.designsystem.component.DSBottomSheet
import com.jarvis.core.designsystem.component.DSButton
import com.jarvis.core.designsystem.component.DSButtonStyle
import com.jarvis.core.designsystem.component.DSIcon
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.component.DSTextField
import com.jarvis.core.designsystem.component.DSIconTint
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.library.R

/**
 * Rating data for the bottom sheet
 */
data class RatingData(
    val stars: Int = 0,
    val description: String = "",
    val isSubmitting: Boolean = false
)

/**
 * Rating bottom sheet dialog for SDK feedback
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingBottomSheet(
    ratingData: RatingData,
    onRatingChange: (Int) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Handle default description when submitting
    val handleSubmit = {
        // If description is empty, set it to "awesome" before submitting
        if (ratingData.description.isBlank()) {
            onDescriptionChange("awesome")
        }
        onSubmit()
    }
    DSBottomSheet(
        modifier = modifier,
        onDismissRequest = onCancel,
        title = { DSText(stringResource(R.string.rate_sdk_title)) },
        content = {
            DSText(
                text = stringResource(R.string.rate_sdk_subtitle),
            )

            // Star Rating
            StarRating(
                rating = ratingData.stars,
                onRatingChange = onRatingChange,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Description Input
            DSTextField(
                text = ratingData.description,
                onValueChange = onDescriptionChange,
                title = stringResource(R.string.rate_sdk_description_label),
                minLine = 4,
                placeholder = stringResource(R.string.rate_sdk_description_placeholder),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            DSButton(
                text = stringResource(R.string.submit),
                onClick = handleSubmit,
                modifier = Modifier.fillMaxWidth(),
                disabled = ratingData.stars <= 0 || ratingData.isSubmitting,
                isLoading = ratingData.isSubmitting
            )
        },
        dismissButton = {
            DSButton(
                text = stringResource(R.string.cancel),
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
                style = DSButtonStyle.SECONDARY,
                disabled = ratingData.isSubmitting
            )
        }
    )
}

/**
 * Star rating component
 */
@Composable
private fun StarRating(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxStars: Int = 5
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(maxStars) { index ->
            val starIndex = index + 1
            val isSelected = starIndex <= rating
            
            DSIcon(
                imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = stringResource(R.string.star_rating, starIndex),
                tint = DSIconTint.Solid(if (isSelected)
                    DSJarvisTheme.colors.warning.warning60
                else
                    DSJarvisTheme.colors.neutral.neutral60),
                modifier = Modifier
                    .size(DSJarvisTheme.dimensions.xxxxl)
                    .clickable(
                        indication = null,
                        interactionSource = interactionSource
                    ) {
                        onRatingChange(starIndex)
                    }
                    .padding(DSJarvisTheme.spacing.xs)
            )
        }
    }
}

// Preview
@Preview(showBackground = true, name = "Rating Bottom Sheet - Empty")
@Composable
private fun RatingBottomSheetEmptyPreview() {
    DSJarvisTheme {
        RatingBottomSheet(
            ratingData = RatingData(),
            onRatingChange = { },
            onDescriptionChange = { },
            onSubmit = { },
            onCancel = { }
        )
    }
}

@Preview(showBackground = true, name = "Rating Bottom Sheet - Filled")
@Composable
private fun RatingBottomSheetFilledPreview() {
    DSJarvisTheme {
        RatingBottomSheet(
            ratingData = RatingData(
                stars = 5,
                description = "Great SDK! Very helpful for debugging network issues.",
                isSubmitting = false
            ),
            onRatingChange = { },
            onDescriptionChange = { },
            onSubmit = { },
            onCancel = { }
        )
    }
}

@Preview(showBackground = true, name = "Rating Bottom Sheet - Submitting")
@Composable
private fun RatingBottomSheetSubmittingPreview() {
    DSJarvisTheme {
        RatingBottomSheet(
            ratingData = RatingData(
                stars = 4,
                description = "Very useful tool for network debugging!",
                isSubmitting = true
            ),
            onRatingChange = { },
            onDescriptionChange = { },
            onSubmit = { },
            onCancel = { }
        )
    }
}