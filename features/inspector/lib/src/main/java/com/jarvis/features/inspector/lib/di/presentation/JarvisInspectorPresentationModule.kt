package com.jarvis.features.inspector.lib.di.presentation

import androidx.navigation3.runtime.entry
import com.jarvis.core.presentation.navigation.EntryProviderInstaller
import com.jarvis.features.inspector.lib.navigation.JarvisSDKInspectorGraph
import com.jarvis.features.inspector.presentation.ui.NetworkInspectorRoute
import com.jarvis.features.inspector.presentation.ui.NetworkTransactionDetailRoute
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
                }
            )
        }

        entry<JarvisSDKInspectorGraph.JarvisInspectorTransactionDetail> { args ->
            NetworkTransactionDetailRoute(args.transactionId)
        }
    }
}