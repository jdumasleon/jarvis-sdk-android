package com.jarvis.features.inspector.lib.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.jarvis.core.designsystem.icons.DSIcons
import com.jarvis.core.presentation.navigation.NavigationRoute
import com.jarvis.features.inspector.presentation.R
import kotlinx.serialization.Serializable

object JarvisSDKInspectorGraph : NavigationRoute {

    @Serializable
    data object JarvisInspectorTransactions : NavigationRoute {
        override val titleTextId: Int = R.string.jarvis_inspector
        override val shouldShowTopAppBar: Boolean = true
        override val shouldShowBottomBar: Boolean = true
        override val dismissable: Boolean = true
    }
    @Serializable
    data class JarvisInspectorTransactionDetail(val transactionId: String) : NavigationRoute {
        override val titleTextId: Int = R.string.jarvis_inspector_transaction_detail
        override val shouldShowTopAppBar: Boolean = true
        override val shouldShowBottomBar: Boolean = true
        override val navigationIcon: ImageVector? = DSIcons.Rounded.arrowBack
        override val navigationIconContentDescription: Int? = R.string.jarvis_inspector_transaction_detail_back
        override val route: String = "network_transaction_detail/$transactionId"
    }
    
    @Serializable
    data object JarvisInspectorRules : NavigationRoute {
        override val titleTextId: Int = R.string.jarvis_inspector_rules
        override val shouldShowTopAppBar: Boolean = true
        override val shouldShowBottomBar: Boolean = true
        override val navigationIcon: ImageVector? = DSIcons.Rounded.arrowBack
        override val navigationIconContentDescription: Int? = R.string.jarvis_inspector_rules_back
        override val actionIcon: ImageVector? = DSIcons.add
        override val actionIconContentDescription: Int? = R.string.jarvis_inspector_rules_add
        override val actionKey: String = "inspector_rules_add_action"
    }
}