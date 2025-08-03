package com.jarvis.demo.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.jarvis.core.designsystem.component.DSCard
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.demo.R

@Composable
internal fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    HomeScreen(
        modifier = modifier,
    )
}

@Composable
internal fun HomeScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(DSJarvisTheme.spacing.l),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Welcome card
        DSCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(DSJarvisTheme.spacing.xl)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App name and title
                DSText(
                    text = stringResource(R.string.welcome_message),
                    style = DSJarvisTheme.typography.heading.heading3,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = DSJarvisTheme.colors.primary.primary60
                )
                
                Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.s))
                
                // Description
                DSText(
                    text = stringResource(R.string.jarvis_description),
                    style = DSJarvisTheme.typography.body.large,
                    textAlign = TextAlign.Center,
                    color = DSJarvisTheme.colors.neutral.neutral60
                )
                
                Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.xs))
                
                // Version
                DSText(
                    text = stringResource(R.string.version),
                    style = DSJarvisTheme.typography.body.small,
                    textAlign = TextAlign.Center,
                    color = DSJarvisTheme.colors.neutral.neutral40
                )
                
                Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.l))
                
                // Instructions
                DSText(
                    text = stringResource(R.string.shake_instructions),
                    style = DSJarvisTheme.typography.body.medium,
                    textAlign = TextAlign.Center,
                    color = DSJarvisTheme.colors.neutral.neutral80,
                    lineHeight = DSJarvisTheme.typography.body.medium.lineHeight * 1.3f
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DSJarvisTheme {
        HomeScreen()
    }
}