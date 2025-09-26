@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.core.internal.navigation.routes

import androidx.annotation.RestrictTo

import androidx.compose.ui.graphics.vector.ImageVector
import com.jarvis.core.internal.designsystem.icons.DSIcons
import com.jarvis.core.internal.navigation.NavigationRoute
import com.jarvis.core.R
import com.jarvis.core.internal.navigation.TopAppBarType
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

object JarvisSDKInspectorGraph {

    @Serializable
    data object JarvisInspectorTransactions : NavigationRoute {
        override val titleTextId: Int = R.string.core_navigation_inspector
        override val shouldShowTopAppBar: Boolean = true
        override val shouldShowBottomBar: Boolean = true
        override val topAppBarType: TopAppBarType = TopAppBarType.MEDIUM
        override val dismissable: Boolean = true
    }

    @Serializable
    data class JarvisInspectorTransactionDetail(val transactionId: String) : NavigationRoute {
        override val titleTextId: Int = R.string.core_navigation_transaction_detail
        override val shouldShowTopAppBar: Boolean = true
        override val shouldShowBottomBar: Boolean = true
        @Transient
        override val navigationIcon: ImageVector? = DSIcons.Rounded.arrowBack
        override val navigationIconContentDescription: Int? = R.string.core_navigation_go_back_navigation
        override val route: String = "network_transaction_detail/$transactionId"
    }
    
    @Serializable
    data object JarvisInspectorBreakpoints : NavigationRoute {
        override val titleTextId: Int = R.string.core_navigation_breakpoints
        override val shouldShowTopAppBar: Boolean = true
        override val shouldShowBottomBar: Boolean = true
        @Transient
        override val navigationIcon: ImageVector? = DSIcons.Rounded.arrowBack
        override val navigationIconContentDescription: Int? = R.string.core_navigation_go_back_navigation
        @Transient
        override val actionIcon: ImageVector? = DSIcons.add
        override val actionIconContentDescription: Int? = R.string.core_navigation_add_new_rule
        override val actionKey: String = "breakpoints_rules_add_action"
    }
}