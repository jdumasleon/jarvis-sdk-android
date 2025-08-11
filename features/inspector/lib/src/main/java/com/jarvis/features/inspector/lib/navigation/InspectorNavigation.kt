package com.jarvis.features.inspector.presentation.navigation

import com.jarvis.core.presentation.navigation.NavigationRoute
import com.jarvis.features.inspector.presentation.R
import kotlinx.serialization.Serializable

object JarvisSDKInspectorGraph {
    @Serializable
    data object JarvisInspectorTransactions : NavigationRoute {
        override val titleTextId: Int = R.string.jarvis_inspector
        override val shouldShowTopAppBar: Boolean = true
    }

    @Serializable
    data class JarvisInspectorTransactionDetail(val transactionId: String) : NavigationRoute {
        override val route: String = "network_transaction_detail/$transactionId"
        override val shouldShowTopAppBar: Boolean = true
    }
}