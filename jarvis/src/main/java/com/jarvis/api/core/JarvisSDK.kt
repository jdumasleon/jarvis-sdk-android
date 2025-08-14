package com.jarvis.api.core

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jarvis.api.ui.JarvisOverlay
import com.jarvis.api.ui.JarvisSDKOverlay
import com.jarvis.config.JarvisConfig
import com.jarvis.config.ConfigurationSynchronizer
import com.jarvis.core.data.performance.PerformanceManager
import com.jarvis.features.home.lib.navigation.JarvisSDKHomeGraph
import com.jarvis.features.inspector.lib.navigation.JarvisSDKInspectorGraph
import com.jarvis.features.preferences.lib.navigation.JarvisSDKPreferencesGraph
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Main Jarvis SDK interface for initialization and configuration
 */
@Singleton
class JarvisSDK @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val configurationSynchronizer: ConfigurationSynchronizer,
    private val performanceManager: PerformanceManager
) {
    private var isInitialized = false
    private var configuration = JarvisConfig()
    private var jarvisOverlay: JarvisOverlay? = null
    private var _isJarvisActive by mutableStateOf(false)
    private var activityContext: Activity? = null
    
    /**
     * Initialize the Jarvis SDK with optional configuration
     */
    fun initialize(
        application: Application,
        config: JarvisConfig = JarvisConfig()
    ) {
        if (isInitialized) {
            return
        }
        
        this.configuration = config
        this.isInitialized = true
        
        // Synchronize configuration with all feature modules
        configurationSynchronizer.updateConfigurations(config)
        
        // Initialize performance monitoring for real-time metrics
        performanceManager.initialize()
        
        // Perform any necessary initialization
        // This could include setting up crash reporting, analytics, etc.
    }
    
    /**
     * Set the activity context for overlay operations
     * This should be called from the Activity where the overlay will be shown
     */
    fun setActivityContext(activity: Activity) {
        this.activityContext = activity
        // Re-create overlay with proper Activity context
        jarvisOverlay = JarvisOverlay(activity)
    }
    
    /**
     * Check if SDK is initialized
     */
    fun isInitialized(): Boolean = isInitialized
    
    /**
     * Get current configuration
     */
    fun getConfiguration(): JarvisConfig = configuration
    
    /**
     * Update configuration
     */
    fun updateConfiguration(config: JarvisConfig) {
        this.configuration = config
        
        // Synchronize the new configuration with all feature modules
        configurationSynchronizer.updateConfigurations(config)
    }
    
    /**
     * Show Jarvis overlay
     */
    fun showHome() {
        if (isInitialized && configuration.enableDebugLogging && activityContext != null) {
            // Ensure overlay is created with proper context
            if (jarvisOverlay == null) {
                jarvisOverlay = JarvisOverlay(activityContext!!)
            }
            jarvisOverlay?.show(JarvisSDKHomeGraph.JarvisHome)
        }
    }
    
    /**
     * Show Jarvis overlay with direct navigation to inspector
     */
    fun showInspector() {
        if (isInitialized && configuration.enableDebugLogging && activityContext != null) {
            // Ensure overlay is created with proper context
            if (jarvisOverlay == null) {
                jarvisOverlay = JarvisOverlay(activityContext!!)
            }
            jarvisOverlay?.show(JarvisSDKInspectorGraph.JarvisInspectorTransactions)
        }
    }
    
    /**
     * Show Jarvis overlay with direct navigation to preferences
     */
    fun showPreferences() {
        if (isInitialized && configuration.enableDebugLogging && activityContext != null) {
            // Ensure overlay is created with proper context
            if (jarvisOverlay == null) {
                jarvisOverlay = JarvisOverlay(activityContext!!)
            }
            jarvisOverlay?.show(JarvisSDKPreferencesGraph.JarvisPreferences)
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
    
    /**
     * Activate Jarvis debugging mode
     */
    fun activate() {
        if (isInitialized) {
            _isJarvisActive = true
        }
    }
    
    /**
     * Deactivate Jarvis debugging mode
     */
    fun deactivate() {
        _isJarvisActive = false
        hideOverlay()
    }
    
    /**
     * Check if Jarvis is active
     */
    fun isActive(): Boolean = _isJarvisActive
    
    /**
     * Toggle Jarvis active state
     */
    fun toggle(): Boolean {
        if (_isJarvisActive) {
            deactivate()
        } else {
            activate()
        }
        return _isJarvisActive
    }
    
    /**
     * Activate Jarvis only if not already active (for shake detection)
     * This prevents multiple activations when shake is detected
     */
    fun activateIfInactive(): Boolean {
        if (!_isJarvisActive && isInitialized) {
            activate()
            return true
        }
        return false
    }
    
}



/**
 * Composable that provides Jarvis SDK overlay
 */
@Composable
fun JarvisProvider(
    sdk: JarvisSDK,
    content: @Composable () -> Unit
) {
    content()
    
    // Add SDK overlay if enabled and initialized
    if (sdk.isInitialized() && sdk.getConfiguration().enableDebugLogging) {
        JarvisSDKOverlay(
            onShowOverlay = { sdk.showHome() },
            onShowInspector = { sdk.showInspector() },
            onShowPreferences = { sdk.showPreferences() },
            isJarvisActive = sdk.isActive(),
            enableShakeDetection = sdk.getConfiguration().enableShakeDetection,
            onToggleJarvisActive = { sdk.activateIfInactive() }
        )
    }
}