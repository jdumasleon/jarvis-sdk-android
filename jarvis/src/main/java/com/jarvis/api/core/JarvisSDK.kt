package com.jarvis.api.core

import android.app.Application
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import com.jarvis.api.ui.JarvisOverlay
import com.jarvis.api.ui.JarvisSDKOverlay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Main Jarvis SDK interface for initialization and configuration
 */
@Singleton
class JarvisSDK @Inject constructor(
    private val context: Context
) {
    private var isInitialized = false
    private var configuration = JarvisConfiguration()
    private var jarvisOverlay: JarvisOverlay? = null
    
    /**
     * Initialize the Jarvis SDK with optional configuration
     */
    fun initialize(
        application: Application,
        config: JarvisConfiguration = JarvisConfiguration()
    ) {
        if (isInitialized) {
            return
        }
        
        this.configuration = config
        this.isInitialized = true
        
        // Initialize the overlay system
        jarvisOverlay = JarvisOverlay(context)
        
        // Perform any necessary initialization
        // This could include setting up crash reporting, analytics, etc.
    }
    
    /**
     * Check if SDK is initialized
     */
    fun isInitialized(): Boolean = isInitialized
    
    /**
     * Get current configuration
     */
    fun getConfiguration(): JarvisConfiguration = configuration
    
    /**
     * Update configuration
     */
    fun updateConfiguration(config: JarvisConfiguration) {
        this.configuration = config
    }
    
    /**
     * Show Jarvis overlay
     */
    fun showOverlay() {
        if (isInitialized && configuration.debugMode) {
            jarvisOverlay?.show()
        }
    }
    
    /**
     * Hide Jarvis overlay
     */
    fun hideOverlay() {
        jarvisOverlay?.dismiss()
    }
    
    /**
     * Check if overlay is showing
     */
    fun isOverlayShowing(): Boolean {
        return jarvisOverlay?.isShowing() ?: false
    }
}

/**
 * Configuration class for Jarvis SDK
 */
data class JarvisConfiguration(
    val enableShakeDetection: Boolean = true,
    val enableNetworkInspector: Boolean = true,
    val enablePreferencesInspector: Boolean = true,
    val enableFloatingButton: Boolean = true,
    val debugMode: Boolean = true
) {
    companion object {
        fun production() = JarvisConfiguration(
            enableShakeDetection = false,
            enableNetworkInspector = false,
            enablePreferencesInspector = false,
            enableFloatingButton = false,
            debugMode = false
        )
        
        fun development() = JarvisConfiguration(
            enableShakeDetection = true,
            enableNetworkInspector = true,
            enablePreferencesInspector = true,
            enableFloatingButton = true,
            debugMode = true
        )
    }
}

/**
 * CompositionLocal for accessing Jarvis SDK
 */
val LocalJarvisSDK = compositionLocalOf<JarvisSDK> {
    error("JarvisSDK not provided")
}

/**
 * Composable that provides Jarvis SDK overlay
 */
@Composable
fun JarvisProvider(
    sdk: JarvisSDK,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalJarvisSDK provides sdk) {
        content()
        
        // Add SDK overlay if enabled and initialized
        if (sdk.isInitialized() && sdk.getConfiguration().debugMode) {
            JarvisSDKOverlay(
                onShowOverlay = { sdk.showOverlay() },
                enableShakeDetection = sdk.getConfiguration().enableShakeDetection
            )
        }
    }
}