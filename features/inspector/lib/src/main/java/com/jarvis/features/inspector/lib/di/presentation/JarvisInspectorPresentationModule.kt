package com.jarvis.features.inspector.lib.di.presentation

import androidx.compose.runtime.DisposableEffect
import androidx.navigation.compose.composable
import androidx.navigation3.runtime.entry
import com.jarvis.core.presentation.navigation.ActionRegistry
import com.jarvis.core.presentation.navigation.EntryProviderInstaller
import com.jarvis.features.inspector.lib.navigation.JarvisSDKInspectorGraph
import com.jarvis.features.inspector.presentation.ui.rules.NetworkRulesEvent
import com.jarvis.features.inspector.presentation.ui.rules.NetworkRulesScreen
import com.jarvis.features.inspector.presentation.ui.rules.NetworkRulesViewModel
import com.jarvis.features.inspector.presentation.ui.transactions.NetworkInspectorRoute
import com.jarvis.features.inspector.presentation.ui.transactionsDetails.NetworkTransactionDetailRoute
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
object JarvisInspectorPresentationModule {

    @IntoSet
    @Provides
    fun provideEntryProviderInstaller(): EntryProviderInstaller = { navigator ->
        entry<JarvisSDKInspectorGraph.JarvisInspectorTransactions> {
            NetworkInspectorRoute(
                onNavigateToDetail = { id ->
                    navigator.goTo(JarvisSDKInspectorGraph.JarvisInspectorTransactionDetail(id))
                },
                onNavigateToRules = {
                    navigator.goTo(JarvisSDKInspectorGraph.JarvisInspectorRules)
                }
            )
        }

        entry<JarvisSDKInspectorGraph.JarvisInspectorTransactionDetail> { args ->
            NetworkTransactionDetailRoute(args.transactionId)
        }

        entry<JarvisSDKInspectorGraph.JarvisInspectorRules> {
            NetworkRulesScreen(actionKey = JarvisSDKInspectorGraph.JarvisInspectorRules.actionKey)
        }
    }
}