package com.jarvis.core.navigation

/**
 * Registry for managing custom action callbacks
 * Allows features to register actions that can be triggered from the top app bar
 */
object ActionRegistry {
    private val actions = mutableMapOf<String, () -> Unit>()

    /**
     * Register an action callback with a key
     */
    fun registerAction(key: String, action: () -> Unit) {
        actions[key] = action
    }

    /**
     * Execute an action by key
     */
    fun executeAction(key: String) {
        actions[key]?.invoke()
    }

    /**
     * Unregister an action
     */
    fun unregisterAction(key: String) {
        actions.remove(key)
    }

    /**
     * Clear all actions
     */
    fun clearActions() {
        actions.clear()
    }
}