package com.jarvis.api.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
// Using placeholder screens until feature modules are properly integrated

/**
 * Independent navigation host for Jarvis SDK
 * This navigation stack is completely separate from the host app's navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JarvisNavigationHost(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = JarvisDestinations.HOME,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Home/Main screen with SDK tools menu
            composable(JarvisDestinations.HOME) {
                JarvisHomeScreen(
                    onNavigateToInspector = {
                        navController.navigate(JarvisDestinations.INSPECTOR)
                    },
                    onNavigateToPreferences = {
                        navController.navigate(JarvisDestinations.PREFERENCES)
                    },
                    onDismiss = onDismiss
                )
            }
            
            // Network Inspector
            composable(JarvisDestinations.INSPECTOR) {
                PlaceholderInspectorScreen(
                    onNavigateToDetail = { transactionId ->
                        navController.navigate("${JarvisDestinations.TRANSACTION_DETAIL}/$transactionId")
                    },
                    onNavigateBack = {
                        if (!navController.popBackStack()) {
                            onDismiss()
                        }
                    }
                )
            }
            
            // Transaction Detail
            composable("${JarvisDestinations.TRANSACTION_DETAIL}/{transactionId}") { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
                PlaceholderTransactionDetailScreen(
                    transactionId = transactionId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            // Preferences Inspector
            composable(JarvisDestinations.PREFERENCES) {
                PlaceholderPreferencesScreen(
                    onNavigateBack = {
                        if (!navController.popBackStack()) {
                            onDismiss()
                        }
                    }
                )
            }
        }
    }
}

/**
 * Navigation destinations for Jarvis SDK
 */
object JarvisDestinations {
    const val HOME = "jarvis_home"
    const val INSPECTOR = "jarvis_inspector"
    const val TRANSACTION_DETAIL = "jarvis_transaction_detail"
    const val PREFERENCES = "jarvis_preferences"
}