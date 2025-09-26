@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.core.internal.designsystem.component

import androidx.annotation.RestrictTo

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.times
import com.jarvis.core.internal.designsystem.theme.DSJarvisTheme

@Composable
fun DSToggle(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableIntStateOf(options.indexOf(selectedOption)) }

    BoxWithConstraints(
        modifier = modifier
            .background(
                color = DSJarvisTheme.colors.neutral.neutral40,
                shape = DSJarvisTheme.shapes.m
            )
            .padding(DSJarvisTheme.spacing.xs)
            .height(DSJarvisTheme.dimensions.xxxl)
    ) {
        val containerWidth = this.maxWidth
        val optionWidth = containerWidth / options.size
        val indicatorOffset by animateDpAsState(targetValue = selectedIndex * optionWidth, label = "")

        // Sliding Indicator
        Box(
            modifier = Modifier
                .offset { IntOffset(indicatorOffset.roundToPx(), 0) }
                .width(optionWidth)
                .height(DSJarvisTheme.dimensions.xxxl)
                .background(
                    color = DSJarvisTheme.colors.neutral.neutral0,
                    shape = DSJarvisTheme.shapes.s
                )
        )

        // Options Row
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEachIndexed { index, option ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(DSJarvisTheme.dimensions.xxxxxl)
                        .clickable {
                            onOptionSelected(option)
                            selectedIndex = index
                        },
                    contentAlignment = Alignment.Center
                ) {
                    DSText(
                        text = option,
                        color = if (index == selectedIndex) DSJarvisTheme.colors.neutral.neutral100 else DSJarvisTheme.colors.neutral.neutral80,
                        style = DSJarvisTheme.typography.body.medium,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "DSToggle Dynamic Width with BoxWithConstraints")
@Composable
fun DSTogglePreview() {
    var selectedOption by remember { mutableStateOf("option 1") }

    Column(
        modifier = Modifier.padding(DSJarvisTheme.spacing.m),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DSToggle(
            options = listOf("option 1", "option 2"),
            selectedOption = selectedOption,
            onOptionSelected = { newOption ->
                selectedOption = newOption
            }
        )

        Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.m))

        DSText(
            text = "Selected: $selectedOption",
            style = DSJarvisTheme.typography.body.medium,
            color = DSJarvisTheme.colors.neutral.neutral100
        )
    }
}