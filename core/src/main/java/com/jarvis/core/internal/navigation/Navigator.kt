package com.jarvis.core.internal.navigation

import androidx.annotation.RestrictTo

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.EntryProviderBuilder
import com.jarvis.core.internal.navigation.NavigationRoute
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

typealias EntryProviderInstaller = EntryProviderBuilder<NavigationRoute>.(Navigator) -> Unit

/**
 * Centralized navigator for modular navigation
 * Manages navigation stack and provides type-safe navigation
 */
@ActivityRetainedScoped
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class Navigator @Inject constructor() {
    
    private val _backStack: SnapshotStateList<NavigationRoute> = mutableStateListOf()
    val backStack: List<NavigationRoute> = _backStack
    
    val currentDestination: NavigationRoute? get() = _backStack.lastOrNull()
    
    private val tabStacks = mutableMapOf<String, List<NavigationRoute>>()
    
    fun initialize(startDestination: NavigationRoute) {
        if (_backStack.isEmpty()) {
            _backStack.add(startDestination)
        }
    }
    
    /**
     * Navigate to a destination
     */
    fun goTo(destination: NavigationRoute) {
        _backStack.add(destination)
    }
    
    /**
     * Navigate back
     */
    fun goBack(): Boolean {
        return if (_backStack.size > 1) {
            _backStack.removeLastOrNull()
            true
        } else {
            false
        }
    }

    /**
     * Replace current destination with new one
     */
    fun replace(destination: NavigationRoute) {
        if (_backStack.isNotEmpty()) {
            _backStack.removeLastOrNull()
        }
        _backStack.add(destination)
    }
    
    /**
     * Pop to a specific destination in the stack
     */
    fun popTo(destination: NavigationRoute): Boolean {
        val index = _backStack.indexOf(destination)
        return if (index != -1 && index < _backStack.size - 1) {
            val itemsToRemove = _backStack.size - index - 1
            repeat(itemsToRemove) {
                _backStack.removeLastOrNull()
            }
            true
        } else {
            false
        }
    }
    
    /**
     * Pop to root of current tab
     */
    fun popToRoot() {
        if (_backStack.size > 1) {
            val root = _backStack.first()
            _backStack.clear()
            _backStack.add(root)
        }
    }

    /**
     * Clear entire navigation stack
     */
    fun clear() {
        _backStack.clear()
    }
    
    /**
     * Clear entire navigation stack and start fresh
     */
    fun clearAndGoTo(destination: NavigationRoute) {
        _backStack.clear()
        _backStack.add(destination)
    }
    
    /**
     * Get tab key for a destination to enable tab-aware navigation
     */
    fun getTabKey(destination: NavigationRoute): String? {
        return when {
            destination::class.qualifiedName?.contains("home", ignoreCase = true) == true -> "home"
            destination::class.qualifiedName?.contains("inspector", ignoreCase = true) == true -> "inspector"
            destination::class.qualifiedName?.contains("preferences", ignoreCase = true) == true -> "preferences"
            destination::class.qualifiedName?.contains("settings", ignoreCase = true) == true -> "settings"
            else -> null
        }
    }
    
    /**
     * Switch to tab with history preservation
     */
    fun switchToTab(destination: NavigationRoute, currentTabKey: String, targetTabKey: String) {
        // Save current tab stack
        tabStacks[currentTabKey] = _backStack.toList()
        
        // Restore target tab stack or start fresh
        val targetStack = tabStacks[targetTabKey]
        if (targetStack != null && targetStack.isNotEmpty()) {
            _backStack.clear()
            _backStack.addAll(targetStack)
        } else {
            clearAndGoTo(destination)
        }
    }
    
    /**
     * Check if we can go back
     */
    fun canGoBack(): Boolean = _backStack.size > 1
    
    /**
     * Get the previous destination in stack
     */
    fun getPreviousDestination(): NavigationRoute? {
        return if (_backStack.size > 1) {
            _backStack[_backStack.size - 2]
        } else {
            null
        }
    }
}