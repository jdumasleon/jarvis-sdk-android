package com.jarvis.demo.presentation

import android.graphics.Color.TRANSPARENT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import com.jarvis.api.core.JarvisSDK
import com.jarvis.api.core.JarvisProvider
import com.jarvis.config.JarvisConfig
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.presentation.navigation.EntryProviderInstaller
import com.jarvis.core.presentation.navigation.Navigator
import com.jarvis.core.presentation.state.ResourceState
import com.jarvis.demo.data.preferences.PreferencesDataStoreManager
import com.jarvis.demo.data.preferences.ProtoDataStoreManager
import com.jarvis.demo.data.preferences.proto.UserSettings
import com.jarvis.demo.presentation.home.HomeGraph
import com.jarvis.demo.presentation.splash.SplashEvent
import com.jarvis.demo.presentation.splash.SplashScreen
import com.jarvis.demo.presentation.splash.SplashViewModel
import com.jarvis.demo.presentation.ui.JarvisDemoApp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var entryProviderBuilders: Set<@JvmSuppressWildcards EntryProviderInstaller>
    
    @Inject
    lateinit var jarvisSDK: JarvisSDK
    
    @Inject
    lateinit var preferencesDataStoreManager: PreferencesDataStoreManager
    
    @Inject
    lateinit var protoDataStoreManager: ProtoDataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Initialize Jarvis SDK with comprehensive demo app configuration
        val demoConfig = JarvisConfig.builder()
            .enableShakeDetection(true)
            .enableDebugLogging(true)
            .preferences {
                // Enable all auto-discovery for maximum data extraction
                autoDiscoverDataStores(true)
                autoDiscoverSharedPrefs(true) 
                autoDiscoverProtoDataStores(true)
                
                // Explicitly include known data sources
                includeDataStores("demo_preferences") // Our demo DataStore
                includeSharedPrefs("demo_shared_preferences") // Our demo SharedPreferences
                includeProtoDataStores("user_settings") // Our proto DataStore
                
                // Register DataStore instances with proto schema for proper data access
                registerDataStore("demo_preferences", preferencesDataStoreManager.getDataStore())
                registerProtoDataStore(
                    name = "user_settings",
                    dataStore = protoDataStoreManager.getDataStore(),
                    extractor = { userSettings: UserSettings ->
                        mapOf(
                            "username" to userSettings.username,
                            "is_premium" to userSettings.isPremium,
                            "preferred_language" to userSettings.preferredLanguage,
                            "email_notifications" to userSettings.emailNotifications,
                            "push_notifications" to userSettings.pushNotifications,
                            "marketing_notifications" to userSettings.marketingNotifications,
                            "theme_preference" to userSettings.themePreference,
                            "font_size_multiplier" to userSettings.fontSizeMultiplier,
                            "dark_mode_enabled" to userSettings.darkModeEnabled,
                            "analytics_enabled" to userSettings.analyticsEnabled,
                            "crash_reporting_enabled" to userSettings.crashReportingEnabled,
                            "location_sharing" to userSettings.locationSharing,
                            "auto_sync" to userSettings.autoSync,
                            "sync_interval_minutes" to userSettings.syncIntervalMinutes,
                            "wifi_only_sync" to userSettings.wifiOnlySync,
                            "max_cache_size_mb" to userSettings.maxCacheSizeMb,
                            "cache_expiry_hours" to userSettings.cacheExpiryHours
                        )
                    }
                )
                
                // Enable comprehensive scanning and editing
                enablePreferenceEditing(true)
                showSystemPreferences(true) // Show system preferences for debugging
                maxFileSize(50 * 1024 * 1024) // Increase max file size to 50MB
            }
            .networkInspection {
                enableNetworkLogging(true)
                enableRequestLogging(true)
                enableResponseLogging(true)
            }
            .build()
            
        jarvisSDK.initialize(
            application = application,
            config = demoConfig
        )
        
        // Set activity context for overlay operations
        jarvisSDK.setActivityContext(this)

        navigator.initialize(HomeGraph.Home)

        setContent {
            val darkTheme = isSystemInDarkTheme()
            DSJarvisTheme(darkTheme = darkTheme) {
                JarvisProvider(
                    sdk = jarvisSDK
                ) {
                    val splashViewModel = ViewModelProvider(this@MainActivity)[SplashViewModel::class.java]
                    val uiState by splashViewModel.uiState.collectAsState()
                    
                    // Determine if splash should be shown based on UI state
                    val showSplash = when (val state = uiState) {
                        is ResourceState.Success -> state.data.showSplash
                        is ResourceState.Loading -> true
                        is ResourceState.Idle -> true
                        is ResourceState.Error -> false
                    }
                    
                    // Show splash screen with fade transition
                    AnimatedVisibility(
                        visible = showSplash,
                        enter = fadeIn(animationSpec = tween(300)),
                        exit = fadeOut(animationSpec = tween(500))
                    ) {
                        SplashScreen(
                            onSplashFinished = { splashViewModel.onEvent(SplashEvent.CompleteSplash) }
                        )
                    }
                    
                    // Show main app with fade transition
                    AnimatedVisibility(
                        visible = !showSplash,
                        enter = fadeIn(animationSpec = tween(500)),
                        exit = fadeOut(animationSpec = tween(300))
                    ) {
                        JarvisDemoApp(
                            navigator = navigator,
                            entryProviderBuilders = entryProviderBuilders
                        )
                    }
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Unregister shake detection
        // TODO: ActivityJarvisMode.unregister(this) - check if needed
    }
}