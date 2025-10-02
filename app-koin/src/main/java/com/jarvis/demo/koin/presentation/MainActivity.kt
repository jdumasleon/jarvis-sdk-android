package com.jarvis.demo.koin.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import com.jarvis.api.JarvisSDK
import com.jarvis.config.JarvisConfig
import com.jarvis.core.internal.designsystem.theme.DSJarvisTheme
import com.jarvis.core.internal.navigation.EntryProviderInstaller
import com.jarvis.core.internal.navigation.Navigator
import com.jarvis.demo.koin.data.preferences.PreferencesDataStoreManager
import com.jarvis.demo.koin.data.preferences.ProtoDataStoreManager
import com.jarvis.demo.koin.presentation.home.HomeGraph
import com.jarvis.demo.koin.data.preferences.proto.UserSettings
import com.jarvis.demo.koin.presentation.ui.JarvisDemoApp
import com.jarvis.integration.koin.initializeWithKoin
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named

/**
 * Main Activity for the Koin Demo App
 *
 * PURE KOIN INTEGRATION - NO HILT REQUIRED!
 *
 * This Activity demonstrates using ONLY Koin for dependency injection,
 * including the Jarvis SDK. No Hilt annotations (@AndroidEntryPoint, @Inject) needed!
 */
class MainActivity : ComponentActivity() {

    // Inject ALL dependencies using Koin - including Jarvis SDK!
    private val navigator: Navigator by inject(named("demo_app"))
    private val entryProviderBuilders: Set<EntryProviderInstaller> by inject(named("demo_entry_providers"))
    private val preferencesDataStoreManager: PreferencesDataStoreManager by inject()
    private val protoDataStoreManager: ProtoDataStoreManager by inject()

    // Inject Jarvis SDK using Koin - NO HILT!
    private val jarvisSDK: JarvisSDK by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Initialize the UI
        initializeUI()

        // Initialize Jarvis SDK with comprehensive demo app configuration
        initializeJarvisSDK()
    }

    internal fun initializeUI() {
        navigator.initialize(HomeGraph.Home)

        setContent {
            val darkTheme = isSystemInDarkTheme()
            DSJarvisTheme(darkTheme = darkTheme) {
                JarvisDemoApp(
                    navigator = navigator,
                    entryProviderBuilders = entryProviderBuilders
                )
            }
        }
    }

    private fun initializeJarvisSDK() {
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

                // Register DataStore instances
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

        // Use Koin-compatible initialization method
        lifecycleScope.launch {
            jarvisSDK.initializeWithKoin(config = demoConfig, hostActivity = this@MainActivity)
        }
    }
}