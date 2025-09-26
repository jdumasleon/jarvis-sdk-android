package com.jarvis.demo.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.jarvis.api.JarvisSDK
import com.jarvis.config.JarvisConfig
import com.jarvis.demo.data.preferences.PreferencesDataStoreManager
import com.jarvis.demo.data.preferences.ProtoDataStoreManager
import com.jarvis.demo.data.preferences.proto.UserSettings
import com.jarvis.demo.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    @Inject
    lateinit var jarvisSDK: JarvisSDK
    
    @Inject
    lateinit var preferencesDataStoreManager: PreferencesDataStoreManager
    
    @Inject
    lateinit var protoDataStoreManager: ProtoDataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Initialize the appropriate UI based on build variant
        initializeUI()

        // Initialize Jarvis SDK with comprehensive demo app configuration
        initializeJarvisSDK()
    }

    internal fun initializeUI() {
        setContentView(R.layout.activity_main_classic)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        (this as AppCompatActivity).setSupportActionBar(toolbar)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)

        val navHostFragment = (this as FragmentActivity).supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_classic) as NavHostFragment
        val navController = navHostFragment.navController

        // Set up ActionBar with NavController
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_inspector, R.id.nav_preferences),
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Set up drawer toggle
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Handle drawer close button in header
        val headerView = navView.getHeaderView(0)
        val closeButton = headerView.findViewById<android.widget.ImageView>(R.id.drawer_close_button)
        closeButton?.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
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

        jarvisSDK.initializeAsync(config = demoConfig, hostActivity = this)
    }
}