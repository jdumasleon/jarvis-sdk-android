package com.jarvis.demo.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.designsystem.theme.DSSpacing
import com.jarvis.core.navigation.Destination

@Composable
internal fun HomeScreen(
    onNavigate: (Destination) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    HomeScreen(
        onNavigate = onNavigate,
        modifier = modifier,
    )
}

@Composable
internal fun HomeScreen(
    onNavigate: (Destination) -> Unit,
    modifier: Modifier = Modifier
) {
    Greeting("droids")
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Column (
        modifier = Modifier
            .padding(DSJarvisTheme.spacing.m)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DSText(
            text = "Hello $name! I'm Jarvis, your dev assistant.",
            modifier = modifier
        )

        DSText(
            text = "Shake me to use my powers.",
            modifier = modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DSJarvisTheme {
        HomeScreen(onNavigate = {})
    }
}