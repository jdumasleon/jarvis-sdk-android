package com.jarvis.core.presentation.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.EntryProviderBuilder
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

typealias EntryProviderInstaller = EntryProviderBuilder<NavigationRoute>.() -> Unit

/**
 * Central navigator for modular navigation system based on android-clean-config.yml template.
 * Manages navigation stack and provides type-safe navigation methods.
 */
@ActivityRetainedScoped
class Navigator @Inject constructor(startDestination: NavigationRoute) {

    private val _backStack: SnapshotStateList<NavigationRoute> = mutableStateListOf(startDestination)
    val backStack: SnapshotStateList<NavigationRoute> = _backStack

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
    
    val currentDestination: NavigationRoute?
        get() = _backStack.lastOrNull()
        
    val canGoBack: Boolean
        get() = _backStack.size > 1
}