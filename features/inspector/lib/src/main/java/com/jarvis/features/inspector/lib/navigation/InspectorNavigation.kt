package com.jarvis.features.inspector.lib.navigation

import kotlinx.serialization.Serializable

@Serializable
data object NetworkInspectorRoute

@Serializable
data class NetworkTransactionDetailRoute(val transactionId: String)

@Serializable
data object NetworkTransactionListRoute