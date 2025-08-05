package com.jarvis.features.inspector.lib.navigation

import com.jarvis.core.presentation.navigation.NavigationRoute
import kotlinx.serialization.Serializable

@Serializable
data object NetworkInspectorRoute : NavigationRoute {
    override val route: String = "network_inspector"
    override val shouldShowTopAppBar: Boolean = true
}

@Serializable
data class NetworkTransactionDetailRoute(val transactionId: String) : NavigationRoute {
    override val route: String = "network_transaction_detail/$transactionId"
    override val shouldShowTopAppBar: Boolean = true
}

@Serializable
data object NetworkTransactionListRoute : NavigationRoute {
    override val route: String = "network_transaction_list"
    override val shouldShowTopAppBar: Boolean = true
}