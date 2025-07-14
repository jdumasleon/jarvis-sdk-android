package com.jarvis.core.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Instance of this interface represents a destination including its arguments.
 *
 * Each destination has to be a KotlinX-Serializable type.
 *
 * Utilize sealed interface to define a set of known destinations:
 *
 * ```
 * sealed interface Destinations : Destination {
 *      @Serializable
 *      object Home : Destinations
 *
 *      @Serializable
 *      data class Article(val id: Int) : Destinations
 * }
 * ```
 */
public interface Destination {

    @get:StringRes
    val titleTextId: Int

    val route: String
    val destination: Destination

    //region .: TopAppBar properties :.
    val navigationIcon: ImageVector? get() = null

    @get:StringRes
    val navigationIconContentDescription: Int? get() = null

    val actionIcon: ImageVector? get() = null

    @get:StringRes
    val actionIconContentDescription: Int? get() = null

    val onActionClick: () -> Unit get() = { }
    val onNavigationClick: () -> Unit get() = { }
    //endregion

    //region .: BottomBar properties :.
    val bottomBarSelectedIcon: ImageVector? get() = null
    val bottomBarUnSelectedIcon: ImageVector? get() = null

    @get:StringRes
    val iconTextId: Int? get() = null
    //endregion
}