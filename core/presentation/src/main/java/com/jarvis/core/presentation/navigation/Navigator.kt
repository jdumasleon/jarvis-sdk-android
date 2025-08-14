package com.jarvis.core.presentation.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.EntryProviderBuilder
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

typealias EntryProviderInstaller = EntryProviderBuilder<NavigationRoute>.(Navigator) -> Unit

/**
 * Central navigator for modular navigation system based on android-clean-config.yml template.
 * Manages navigation stack and provides type-safe navigation methods.
 */
@ActivityRetainedScoped
class Navigator @Inject constructor() {

    private val _backStack: SnapshotStateList<NavigationRoute> = mutableStateListOf()
    val backStack: SnapshotStateList<NavigationRoute> = _backStack
    
    // Tab navigation stacks to preserve history per tab
    private val tabStacks = mutableMapOf<String, List<NavigationRoute>>()

    fun initialize(startDestination: NavigationRoute) {
        if (_backStack.isEmpty()) {
            _backStack.add(startDestination)
        }
    }
    
    fun goTo(destination: NavigationRoute) {
        _backStack.add(destination)
    }
    
    fun goBack() {
        if (_backStack.size > 1) {
            _backStack.removeLastOrNull()
        }
    }
    
    fun popToRoot() {
        if (_backStack.size > 1) {
            val root = _backStack.first()
            _backStack.clear()
            _backStack.add(root)
        }
    }
    
    fun replace(destination: NavigationRoute) {
        if (_backStack.isNotEmpty()) {
            _backStack.removeLastOrNull()
        }
        _backStack.add(destination)
    }
    
    fun popTo(destination: NavigationRoute) {
        val index = _backStack.indexOfLast { it == destination }
        if (index != -1 && index < _backStack.size - 1) {
            // Remove all items after the found destination
            val itemsToRemove = _backStack.size - index - 1
            repeat(itemsToRemove) {
                _backStack.removeLastOrNull()
            }
        }
    }

    fun clear() {
        _backStack.clear()
    }

    val currentDestination: NavigationRoute?
        get() = _backStack.lastOrNull()
        
    val canGoBack: Boolean
        get() = _backStack.size > 1
        
    /**
     * Switch to a tab, preserving the current tab's navigation stack and restoring the target tab's stack.
     * @param tabDestination The root destination of the tab
     * @param currentTabKey Identifier for the current tab (e.g., "inspector", "preferences")
     * @param targetTabKey Identifier for the target tab
     */
    fun switchToTab(tabDestination: NavigationRoute, currentTabKey: String, targetTabKey: String) {
        // Save current tab's navigation stack
        tabStacks[currentTabKey] = _backStack.toList()
        
        // Restore target tab's navigation stack or start with tab root
        val targetStack = tabStacks[targetTabKey]
        _backStack.clear()
        
        if (targetStack != null && targetStack.isNotEmpty()) {
            // Restore the saved stack for this tab
            _backStack.addAll(targetStack)
        } else {
            // First time visiting this tab, start with root
            _backStack.add(tabDestination)
        }
    }
    
    /**
     * Determines the tab key based on the navigation route.
     */
    fun getTabKey(route: NavigationRoute): String? {
        val routeName = route::class.qualifiedName ?: ""
        return when {
            routeName.contains("inspector", ignoreCase = true) -> "inspector"
            routeName.contains("preferences", ignoreCase = true) -> "preferences"
            routeName.contains("home", ignoreCase = true) -> "home"
            else -> null
        }
    }
}