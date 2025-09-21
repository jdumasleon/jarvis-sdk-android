package com.jarvis.features.inspector.di.presentation

import androidx.navigation3.runtime.entry
import com.jarvis.core.navigation.EntryProviderInstaller
import com.jarvis.core.navigation.routes.JarvisSDKInspectorGraph
import com.jarvis.features.inspector.presentation.breakpoints.NetworkBreakpointsScreen
import com.jarvis.features.inspector.presentation.transactions.NetworkInspectorRoute
import com.jarvis.features.inspector.presentation.transactionsDetails.NetworkTransactionDetailRoute
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
                    navigator.goTo(JarvisSDKInspectorGraph.JarvisInspectorBreakpoints)
                }
            )
        }

        entry<JarvisSDKInspectorGraph.JarvisInspectorTransactionDetail> { args ->
            NetworkTransactionDetailRoute(args.transactionId)
        }

        entry<JarvisSDKInspectorGraph.JarvisInspectorBreakpoints> {
            NetworkBreakpointsScreen(actionKey = JarvisSDKInspectorGraph.JarvisInspectorBreakpoints.actionKey)
        }
    }
}