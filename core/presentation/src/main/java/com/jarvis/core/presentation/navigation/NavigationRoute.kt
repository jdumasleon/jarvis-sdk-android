package com.jarvis.core.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.reflect.KClass

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
interface NavigationRoute {
    @get:StringRes
    val titleTextId: Int? get() = null
    val kclass: KClass<*> get() = this::class
    val route: String get() = ""

    //region .: TopAppBar properties :.
    val shouldShowTopAppBar: Boolean get() = false

    val navigationIcon: ImageVector? get() = null
    @get:StringRes
    val navigationIconContentDescription: Int? get() = null
    val onBackNavigate: NavigationRoute? get() = null

    val actionIcon: ImageVector? get() = null
    @get:StringRes
    val actionIconContentDescription: Int? get() = null
    val onActionNavigate: NavigationRoute? get() = null
    //endregion

    //region .: BottomBar properties :.
    val shouldShowBottomBar: Boolean get() = false

    val bottomBarSelectedIcon: ImageVector? get() = null
    val bottomBarSelectedIconContentDescription: String get() = ""

    val bottomBarIcon: ImageVector? get() = null
    val bottomBarIconContentDescription: String get() = ""

    @get:StringRes
    val bottomBarIconTextId: Int? get() = null
    //endregion

    //endregion
}