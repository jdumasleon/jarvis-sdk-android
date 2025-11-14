# Jarvis Android SDK

[![Maven Central](https://img.shields.io/maven-central/v/io.github.jdumasleon/jarvis-android-sdk?color=blue)](https://central.sonatype.com/artifact/io.github.jdumasleon/jarvis-android-sdk)
[![GitHub Packages](https://img.shields.io/badge/GitHub%20Packages-Available-green)](https://github.com/jdumasleon/mobile-jarvis-android-sdk/packages)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A comprehensive debugging and development toolkit for Android applications, providing real-time insights into your app's behavior, network traffic, preferences, and performance metrics.

**🎯 Perfect for development and debugging | 📱 Zero overhead in production builds**

## Table of Contents

- [Quick Start](#quick-start)
  - [1. Add Dependency](#1-add-dependency)
  - [2. Enable Hilt in Application](#2-enable-hilt-in-application)
  - [3. Initialize in Activity](#3-initialize-in-activity)
  - [4. Activate Jarvis](#4-activate-jarvis)
- [Features](#features)
  - [🌐 Network Inspection](#-network-inspection)
  - [📊 Preferences Management](#-preferences-management)
  - [🏠 Application Overview](#-application-overview)
  - [🎨 Design System](#-design-system)
- [Installation](#installation)
  - [Maven Central (Recommended)](#maven-central-recommended)
  - [Maven Central](#maven-central)
  - [GitHub Packages](#github-packages)
  - [Automatic Debug/Release Optimization](#automatic-debugrelease-optimization)
- [Integration Guide](#integration-guide)
  - [Prerequisites](#prerequisites)
  - [Step-by-Step Integration](#step-by-step-integration)
  - [Complete Configuration Options](#complete-configuration-options)
  - [Integration Checklist](#integration-checklist)
- [Usage](#usage)
  - [Activation Methods](#activation-methods)
  - [Network Monitoring](#network-monitoring)
  - [Preferences Management](#preferences-management-1)
  - [Performance Monitoring](#performance-monitoring)
  - [Error Tracking](#error-tracking)
- [Advanced Configuration](#advanced-configuration)
  - [Runtime Configuration Updates](#runtime-configuration-updates)
  - [Production Build Behavior](#production-build-behavior)
- [Demo Applications](#demo-applications)
  - [1. Jarvis Classic Demo](#1-jarvis-classic-demo-appsrcclassic)
  - [2. Jarvis Compose Demo](#2-jarvis-compose-demo-appsrccompose)
  - [Running Demo Apps](#running-demo-apps)
- [Advanced Usage](#advanced-usage)
  - [Custom Feature Integration](#custom-feature-integration)
  - [Data Export](#data-export)
  - [Remote Integration](#remote-integration)
- [Architecture](#architecture)
  - [Module Structure](#module-structure)
  - [Key Components](#key-components)
- [Proguard/R8 Configuration](#proguardr8-configuration)
- [Troubleshooting](#troubleshooting)
  - [Common Issues](#common-issues)
  - [Debug Mode](#debug-mode)
  - [Support](#support)
- [License](#license)
- [Changelog](#changelog)
  - [Version 1.1.0 (Latest)](#version-110-latest)
  - [Version 1.0.28](#version-1028)
  - [Version 1.0.20](#version-1020)
  - [Version 1.0.0-1.0.19](#version-100-1019)

---

## Quick Start

### 1. Add Dependency

Add to your `app/build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.jdumasleon:jarvis-android-sdk:1.3.4")
}
```

### 2. Setup Dependency Injection

**Option A: With Hilt**
```kotlin
@HiltAndroidApp
class MyApplication : Application() {
    // Jarvis SDK components are automatically available via Hilt
}
```

**Option B: With Koin**
```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApplication)
            modules(
                *allJarvisKoinModules.toTypedArray(),
                jarvisInspectorKoinModule,
                jarvisPreferencesKoinModule
            )
        }
    }
}
```

### 3. Initialize in Activity

**With Hilt:**
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var jarvisSDK: JarvisSDK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate()

        // Initialize Jarvis SDK with configuration
        initializeJarvisSDK()

        setContent {
            MyAppContent()
        }
    }

    private fun initializeJarvisSDK() {
        val config = JarvisConfig.builder()
            .enableShakeDetection(true)
            .enableDebugLogging(true)
            .networkInspection {
                enableNetworkLogging(true)
                enableRequestLogging(true)
                enableResponseLogging(true)
            }
            .preferences {
                autoDiscoverDataStores(true)
                autoDiscoverSharedPrefs(true)
                enablePreferenceEditing(true)
            }
            .build()

        jarvisSDK.initializeAsync(config = config, hostActivity = this)
    }

    override fun onDestroy() {
        super.onDestroy()
        jarvisSDK.dismiss()
    }
}
```

**With Koin:**
```kotlin
class MainActivity : ComponentActivity() {

    private val jarvisSDK: JarvisSDK by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate()

        // Initialize Jarvis SDK with configuration
        initializeJarvisSDK()

        setContent {
            MyAppContent()
        }
    }

    private fun initializeJarvisSDK() {
        val config = JarvisConfig.builder()
            .enableShakeDetection(true)
            .enableDebugLogging(true)
            .networkInspection {
                enableNetworkLogging(true)
                enableRequestLogging(true)
                enableResponseLogging(true)
            }
            .preferences {
                autoDiscoverDataStores(true)
                autoDiscoverSharedPrefs(true)
                enablePreferenceEditing(true)
            }
            .build()

        jarvisSDK.initializeWithKoin(config = config, hostActivity = this)
    }

    override fun onDestroy() {
        super.onDestroy()
        jarvisSDK.dismiss()
    }
}
```

### 4. Activate Jarvis

- **Shake your device** (if enabled in config)
- **Or call programmatically**: `jarvisSDK.activate()`

That's it! 🎉 Jarvis will now provide debugging capabilities and network inspection.

## Features

### 🌐 Network Inspection
- **Real-time HTTP/HTTPS monitoring** - Capture all network requests and responses
- **Request/Response details** - Headers, body, timing, and error information
- **GraphQL support** - Specialized handling for GraphQL queries and mutations
- **WebSocket monitoring** - Track WebSocket connections and messages
- **Export capabilities** - Export network logs for analysis

### 📊 Preferences Management
- **Multi-storage support** - SharedPreferences, DataStore, and Proto DataStore
- **Real-time editing** - Modify preferences on-the-fly for testing
- **Type-safe operations** - Proper handling of different data types
- **Search and filtering** - Quickly find specific preferences
- **Bulk operations** - Clear all, generate test data, import/export

### 🏠 Application Overview
- **System information** - Device details, OS version, app version
- **Performance metrics** - Memory usage, CPU utilization, battery impact

### 🎨 Design System
- **Material 3 integration** - Modern Material Design components
- **Twitter-style transparency** - Scroll-based transparent navigation bars
- **Dark/Light theme support** - Automatic theme switching
- **Responsive layouts** - Optimized for different screen sizes
- **Custom animations** - Smooth transitions and micro-interactions

## Installation

The Jarvis Android SDK is available through multiple distribution channels:

- **✅ Maven Central** (Recommended) - No additional repository configuration needed
- **✅ GitHub Packages** - Requires authentication setup
- **✅ Local Maven** - For development and testing

### Maven Central (Recommended)

No additional repository configuration needed. Simply add the dependency:

Add the Jarvis SDK to your `app/build.gradle` file:

```kotlin
dependencies {
    // Single artifact automatically provides full functionality in debug, optimized in release
    implementation("io.github.jdumasleon:jarvis-android-sdk:1.3.4")
}
```
This approach allows you to include only the features you need, reducing the final APK size.

### Maven Central

```xml
<dependency>
    <groupId>io.github.jdumasleon</groupId>
    <artifactId>jarvis-android-sdk</artifactId>
    <version>1.3.4</version>
    <scope>runtime</scope>
</dependency>
```

### GitHub Packages

```kotlin
repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/jdumasleon/mobile-jarvis-android-sdk")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") ?: System.getenv("TOKEN")
        }
    }
}
```

### Available Packages

Choose between the complete SDK or modular packages based on your needs:

#### Complete SDK (Recommended)
```kotlin
dependencies {
    implementation("io.github.jdumasleon:jarvis-android-sdk:1.3.4")  // All features included
}
```
- ✅ **All features included** - Network inspection, preferences management, and core functionality
- ✅ **Zero overhead in production** - Automatically disabled in release builds
- ✅ **Easy integration** - Single dependency with automatic feature detection
- ✅ **ProGuard/R8 optimized** - Dead code elimination removes unused code

#### Modular Packages (Advanced)
For fine-grained control over features and APK size:

```kotlin
dependencies {
    // Core module (required for modular approach)
    implementation("io.github.jdumasleon:jarvis-android-sdk-core:1.3.4")

    // Network inspection features
    implementation("io.github.jdumasleon:jarvis-android-sdk-inspector:1.3.4")

    // Preferences management features
    implementation("io.github.jdumasleon:jarvis-android-sdk-preferences:1.3.4")
}
```

**Modular Package Benefits:**
- 🎯 **Selective features** - Include only what you need
- 📦 **Smaller APK size** - Exclude unused functionality
- 🔧 **Custom integrations** - Build specialized debugging solutions
- ⚡ **Faster builds** - Reduced dependency graph

## Integration Guide

### Prerequisites

**🔧 Required:**
- Android API 24+ (Android 7.0 Nougat)
- Kotlin 2.2+
- Dependency Injection: **Hilt** or **Koin** (choose one)
- Jetpack Compose (recommended) or Android Views

**📦 Optional but Recommended:**
- OkHttp for automatic network interception
- DataStore for preferences management
- Proto DataStore for complex data structures

### Step-by-Step Integration

The Jarvis SDK supports both **Hilt** and **Koin** for dependency injection. Choose the integration method that matches your project's DI framework.

#### Option A: Integration with Hilt

##### 1. Application Setup

Add `@HiltAndroidApp` to your Application class:

```kotlin
@HiltAndroidApp
class MyApplication : Application() {
    // Jarvis SDK components are automatically available via Hilt
}
```

##### 2. Activity Integration

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var jarvisSDK: JarvisSDK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Jarvis SDK
        initializeJarvisSDK()

        setContent {
            MyAppTheme {
                MyAppContent()
            }
        }
    }

    private fun initializeJarvisSDK() {
        val config = JarvisConfig.builder()
            .enableShakeDetection(true)
            .enableDebugLogging(true)
            .networkInspection {
                enableNetworkLogging(true)
            }
            .build()

        jarvisSDK.initializeAsync(config = config, hostActivity = this)
    }

    override fun onDestroy() {
        super.onDestroy()
        jarvisSDK.dismiss()
    }
}
```

#### Option B: Integration with Koin

##### 1. Application Setup

Configure Koin with Jarvis SDK modules:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApplication)
            modules(
                // Your app modules
                appModule,

                // Jarvis SDK modules
                *allJarvisKoinModules.toTypedArray(),
                jarvisInspectorKoinModule,    // Network inspection
                jarvisPreferencesKoinModule   // Preferences management
            )
        }
    }
}
```

**Required imports for Koin integration:**
```kotlin
import com.jarvis.integration.koin.*
import com.jarvis.features.inspector.integration.koin.jarvisInspectorKoinModule
import com.jarvis.features.preferences.integration.koin.jarvisPreferencesKoinModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
```

##### 2. Activity Integration

```kotlin
class MainActivity : ComponentActivity() {

    private val jarvisSDK: JarvisSDK by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Jarvis SDK with Koin
        initializeJarvisSDK()

        setContent {
            MyAppTheme {
                MyAppContent()
            }
        }
    }

    private fun initializeJarvisSDK() {
        val config = JarvisConfig.builder()
            .enableShakeDetection(true)
            .enableDebugLogging(true)
            .networkInspection {
                enableNetworkLogging(true)
            }
            .build()

        jarvisSDK.initializeWithKoin(config = config, hostActivity = this)
    }

    override fun onDestroy() {
        super.onDestroy()
        jarvisSDK.dismiss()
    }
}
```

**Key differences for Koin:**
- Use `by inject()` instead of `@Inject lateinit var`
- Call `initializeWithKoin()` instead of `initializeAsync()`
- No `@AndroidEntryPoint` annotation needed
- Include all Jarvis Koin modules in your Koin configuration

#### 3. Network Monitoring Setup (Optional)

**Automatic Network Interception:**
Network requests are automatically intercepted when using OkHttp. For manual integration:

**With Hilt:**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        jarvisNetworkInspector: JarvisNetworkInspector
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(jarvisNetworkInspector.createInterceptor())
            .build()
    }
}
```

**With Koin:**
```kotlin
val networkModule = module {
    single {
        OkHttpClient.Builder()
            .addInterceptor(get<JarvisNetworkInspector>().createInterceptor())
            .build()
    }
}
```

**Note:** The `JarvisNetworkInspector` is automatically available for injection when Jarvis SDK is included.

#### 4. DataStore Integration

**Preferences DataStore:**

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var jarvisSDK: JarvisSDK
    
    @Inject
    lateinit var userPreferencesDataStore: DataStore<Preferences>
    
    private fun initializeJarvisSDK() {
        val config = JarvisConfig.builder()
            .preferences {
                // Register your DataStores
                registerDataStore("user_preferences", userPreferencesDataStore)
                
                // Include specific preference files
                includeDataStores("user_preferences", "settings")
                includeSharedPrefs("app_prefs", "user_data")
                
                enablePreferenceEditing(true)
            }
            .build()
            
        jarvisSDK.initialize(config = config, hostActivity = this)
    }
}
```

**Proto DataStore Integration:**

```kotlin
private fun initializeJarvisSDK() {
    val config = JarvisConfig.builder()
        .preferences {
            // Register Proto DataStore with custom extractor
            registerProtoDataStore(
                name = "user_settings",
                dataStore = userSettingsDataStore,
                extractor = { userSettings: UserSettings ->
                    mapOf(
                        "username" to userSettings.username,
                        "isPremium" to userSettings.isPremium,
                        "theme" to userSettings.themePreference,
                        "notifications" to userSettings.emailNotifications
                    )
                }
            )
        }
        .build()
        
    jarvisSDK.initialize(config = config, hostActivity = this)
}
```

#### 5. Build Configuration

**Gradle Setup for Build Variants:**

```kotlin
// app/build.gradle.kts
dependencies {
    // Complete SDK (recommended)
    implementation("io.github.jdumasleon:jarvis-android-sdk:1.3.4")

    // Or modular approach for selective features
    // implementation("io.github.jdumasleon:jarvis-android-sdk-core:1.3.4")
    // implementation("io.github.jdumasleon:jarvis-android-sdk-inspector:1.3.4")
    // implementation("io.github.jdumasleon:jarvis-android-sdk-preferences:1.3.4")
}
```

#### 6. Permissions (Optional)

Add to your `AndroidManifest.xml` if you want network monitoring:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

#### 7. Modular Integration (Advanced)

For projects using modular packages, ensure proper feature initialization:

```kotlin
// When using modular packages
dependencies {
    implementation("io.github.jdumasleon:jarvis-android-sdk-core:1.3.4")
    implementation("io.github.jdumasleon:jarvis-android-sdk-inspector:1.3.4")
    implementation("io.github.jdumasleon:jarvis-android-sdk-preferences:1.3.4")
}
```

**Feature-Specific Configuration:**
```kotlin
private fun initializeJarvisModular() {
    val config = JarvisConfig.builder()
        .enableShakeDetection(true)
        .enableDebugLogging(true)

        // Inspector module configuration (if included)
        .networkInspection {
            enableNetworkLogging(true)
            enableRequestLogging(true)
            enableResponseLogging(true)
        }

        // Preferences module configuration (if included)
        .preferences {
            autoDiscoverDataStores(true)
            autoDiscoverSharedPrefs(true)
            enablePreferenceEditing(true)
        }
        .build()

    jarvisSDK.initializeAsync(config = config, hostActivity = this)
}
```

### Complete Configuration Options

**Full JarvisConfig Example:**

```kotlin
private fun createAdvancedJarvisConfig(): JarvisConfig {
    return JarvisConfig.builder()
        // Core features
        .enableShakeDetection(true)
        .enableDebugLogging(BuildConfig.DEBUG)
        
        // Preferences configuration
        .preferences {
            // Auto-discovery settings
            autoDiscoverDataStores(true)
            autoDiscoverSharedPrefs(true)
            autoDiscoverProtoDataStores(true)
            
            // Include/exclude specific preference files
            includeDataStores("user_prefs", "settings", "cache")
            excludeDataStores("sensitive_data", "temp_cache")
            includeSharedPrefs("app_preferences", "user_data")
            excludeSharedPrefs("analytics_data", "crash_reports")
            
            // Register DataStore instances for deep inspection
            registerDataStore("user_preferences", userPreferencesDataStore)
            registerProtoDataStore(
                name = "app_settings",
                dataStore = appSettingsDataStore,
                extractor = { settings: AppSettings ->
                    mapOf(
                        "theme" to settings.theme.name,
                        "language" to settings.language,
                        "version" to settings.appVersion
                    )
                }
            )
            
            // Preferences behavior
            enablePreferenceEditing(true)
            showSystemPreferences(false)
            maxFileSize(50 * 1024 * 1024) // 50MB max file size
        }
        
        // Network inspection configuration
        .networkInspection {
            enableNetworkLogging(true)
            enableRequestLogging(true)
            enableResponseLogging(true)
            maxRequestBodySize(1024 * 1024) // 1MB
            maxResponseBodySize(1024 * 1024) // 1MB
            
            // Filter specific hosts
            excludeHosts("analytics.example.com", "crash.reporting.com")
            includeOnlyHosts("api.myapp.com", "backend.myapp.com")
        }
        
        .build()
}
```

### Integration Checklist

Before deploying to production, verify:

**For Hilt Integration:**
- ✅ **Hilt Setup**: `@HiltAndroidApp` on Application class
- ✅ **Activity Annotations**: `@AndroidEntryPoint` on Activities using SDK
- ✅ **SDK Initialization**: `jarvisSDK.initializeAsync()` called in Activity `onCreate()`

**For Koin Integration:**
- ✅ **Koin Setup**: `startKoin` configured in Application with all Jarvis modules
- ✅ **Module Inclusion**: `allJarvisKoinModules`, `jarvisInspectorKoinModule`, `jarvisPreferencesKoinModule` added
- ✅ **SDK Initialization**: `jarvisSDK.initializeWithKoin()` called in Activity `onCreate()`

**Common Checklist:**
- ✅ **SDK Cleanup**: `dismiss()` called in Activity `onDestroy()`
- ✅ **Build Variants**: Different behavior for debug/release confirmed
- ✅ **Network Integration**: OkHttp interceptor added if using network monitoring
- ✅ **DataStore Registration**: Custom DataStores registered if needed
- ✅ **Permissions**: Network permissions added if required
- ✅ **ProGuard Rules**: Jarvis SDK rules added if using code obfuscation

## Usage

### Activation Methods

#### 1. Shake Detection
Simply shake your device to open Jarvis (if enabled in configuration).

#### 2. Programmatic Activation
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var jarvisSDK: JarvisSDK

    fun controlJarvis() {
        // Activate Jarvis
        jarvisSDK.activate()

        // Deactivate Jarvis
        jarvisSDK.deactivate()

        // Toggle Jarvis state
        val isActive = jarvisSDK.toggle()

        // Check if active
        if (jarvisSDK.isActive()) {
            // Jarvis is currently active
        }
    }
}
```

#### 3. Floating Action Button
The Jarvis FAB appears when activated and provides quick access to:
- **Home** - Main dashboard
- **Inspector** - Network traffic analysis
- **Preferences** - App preferences management

### Network Monitoring

Jarvis automatically intercepts network traffic when enabled. No additional setup required for:
- **OkHttp** - Automatic interceptor injection
- **Retrofit** - Built-in support
- **Volley** - Automatic request tracking
- **Custom networking** - Manual integration available

### Preferences Management

#### Automatic Detection
Jarvis automatically detects and displays:
- **SharedPreferences** - All preference files
- **DataStore** - Preferences and Proto DataStore
- **Custom storage** - With manual integration

#### Custom Preferences Integration
```kotlin
// Register custom preferences
JarvisPreferences.register(
    storageType = "Custom",
    preferences = mapOf(
        "user_id" to "12345",
        "is_premium" to true,
        "last_sync" to System.currentTimeMillis()
    )
)

// Listen for preference changes
JarvisPreferences.observe { key, oldValue, newValue ->
    // Handle preference change
    updateAppBehavior(key, newValue)
}
```

### Performance Monitoring

#### Automatic Metrics
Jarvis collects performance data automatically:
- **Memory usage** - Heap, native, and total memory
- **CPU utilization** - Per-core and average usage
- **Battery impact** - Power consumption metrics
- **Frame rate** - UI performance monitoring

#### Custom Metrics
```kotlin
// Track custom events
JarvisPerformance.trackEvent(
    name = "user_action",
    properties = mapOf(
        "action_type" to "button_click",
        "screen_name" to "home",
        "duration_ms" to 150
    )
)

// Measure execution time
JarvisPerformance.measureTime("expensive_operation") {
    // Your expensive operation here
    processLargeDataSet()
}
```

### Error Tracking

#### Automatic Crash Detection
```kotlin
// Jarvis automatically captures unhandled exceptions
// No setup required
```

#### Manual Error Logging
```kotlin
// Log custom errors
JarvisErrorTracker.logError(
    throwable = exception,
    context = "user_profile_update",
    additionalInfo = mapOf(
        "user_id" to userId,
        "operation" to "profile_save"
    )
)
```

## Advanced Configuration

### Runtime Configuration Updates

```kotlin
// Get current configuration
val currentConfig = jarvisSDK.getConfiguration()

// SDK automatically applies configuration during initialization
// For runtime changes, reinitialize with new config
val updatedConfig = JarvisConfig.builder()
    .enableShakeDetection(false) // Disable shake detection
    .networkInspection {
        enableNetworkLogging(false) // Disable network monitoring
    }
    .build()

// Apply new configuration (requires reinitialization)
jarvisSDK.dismiss()
jarvisSDK.initialize(config = updatedConfig, hostActivity = this)
```

### Production Build Behavior

In release builds, the Jarvis SDK automatically optimizes for production:

```kotlin
// All these methods are optimized in release builds
jarvisSDK.initialize(config, hostActivity) // Minimal initialization
jarvisSDK.activate()                        // Disabled in production
jarvisSDK.toggle()                          // Returns false
jarvisSDK.isActive()                        // Returns false
jarvisSDK.getPlatform()                     // Returns null
```

This ensures **zero overhead** in production while maintaining the same API.

## Demo Applications

The SDK includes two demo applications showcasing different integration approaches:

### 1. Jarvis Classic Demo (`app/src/classic`)
- **Traditional Android Views** - XML layouts with ViewBinding
- **Fragment-based navigation** - Classic Android architecture
- **Material Components** - Traditional Material Design implementation

### 2. Jarvis Compose Demo (`app/src/compose`)
- **Jetpack Compose UI** - Modern declarative UI framework
- **Compose Navigation** - Type-safe navigation system
- **Material 3** - Latest Material Design implementation

### Running Demo Apps

```bash
# Build Classic Demo
./gradlew app:assembleClassicDebug

# Build Compose Demo
./gradlew app:assembleComposeDebug

# Install and run
adb install app/build/outputs/apk/classic/debug/app-classic-debug.apk
```

## Advanced Usage

### Custom Feature Integration

```kotlin
// Create custom Jarvis feature
class CustomFeature : JarvisFeature {
    override val name = "Custom Analytics"
    override val icon = R.drawable.ic_analytics
    
    @Composable
    override fun Content(navigator: Navigator) {
        CustomAnalyticsScreen(
            onNavigateBack = { navigator.goBack() }
        )
    }
}

// Register custom feature
jarvisSDK.registerFeature(CustomFeature())
```

### Data Export

```kotlin
// Export network logs
val networkData = jarvisSDK.exportNetworkLogs(
    startDate = Date(),
    endDate = Date(),
    format = ExportFormat.JSON
)

// Export preferences
val preferencesData = jarvisSDK.exportPreferences(
    includeSystemPreferences = false
)

// Export all data
val allData = jarvisSDK.exportAllData()
```

### Remote Integration

```kotlin
// Configure remote endpoint
val remoteConfig = RemoteConfig(
    endpoint = "https://your-backend.com/jarvis",
    apiKey = "your-api-key",
    enableRealTimeSync = true
)

jarvisSDK.configureRemoteIntegration(remoteConfig)
```

## Architecture

### Module Structure

```
jarvis/
├── core/
│   ├── designsystem/     # UI components and theme
│   ├── data/             # Data layer and repositories
│   └── presentation/     # Navigation and common UI
├── features/
│   ├── home/             # Dashboard and overview
│   ├── inspector/        # Network monitoring
│   └── preferences/      # Preferences management
└── api/                  # Public SDK interface
```

### Key Components

- **JarvisSDK** - Main SDK interface and entry point
- **JarvisSDKApplication** - Core UI application with navigation
- **ModularNavDisplay** - Dynamic feature loading system
- **ConfigurationSynchronizer** - Cross-module configuration management
- **PerformanceManager** - Real-time performance monitoring

## Troubleshooting

### Common Issues

#### Jarvis Not Appearing
1. Check if debug build is being used
2. Verify `jarvisSDK.initialize()` is called
3. Ensure shake detection is enabled in config
4. Try programmatic activation: `jarvisSDK.activate()`

#### Network Requests Not Showing
1. Verify network monitoring is enabled
2. Check if OkHttp interceptor is properly configured
3. Ensure network permissions are granted
4. Try manual network logging

#### Preferences Not Loading
1. Check if preferences inspection is enabled
2. Verify SharedPreferences and DataStore setup
3. Ensure proper file permissions
4. Try refreshing the preferences list

#### Performance Issues
1. Reduce performance metrics collection interval
2. Disable heavy features in production builds
3. Verify production build optimization
4. Monitor memory usage

### Debug Mode

Enable verbose logging:

```kotlin
val config = JarvisConfig(
    debugMode = true,
    logLevel = LogLevel.VERBOSE
)
```

### Support

For issues and questions:
- Create an issue in this repository
- Check existing documentation and examples
- Review demo applications for integration patterns

## License

```
Copyright 2024 Jarvis SDK

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Changelog

### Version 1.3.4 (Latest)
- 🔄 **Reactive Activation State** – `JarvisSDK.observeActiveState()` now emits whenever the SDK activates/deactivates (including shake gestures), so host apps can react instantly.
- 🧭 **Demo Home Sync** – Both Hilt and Koin demo Home screens subscribe to that flow, keeping CTA buttons and status indicators accurate without manual refreshes.
- 🎨 **Refined Filter Chips** – Updated `DSFilterChip` keeps selected chips on the Jarvis gradient, restores pristine white for deselected chips, and adds a subtle pressed overlay for better feedback.
- 🧱 **Reusable Presentation Kit** – Shared empty states, loading states, status cards, filter chips, and base view models extracted into the core presentation module.
- ⚙️ **GraphQL + WorkManager Plumbing** – Internal modules now include shared GraphQL helpers plus a background cleanup worker for long-lived network logs.

### Version 1.3.0
- 🚀 **Enhanced Publishing Configuration** - Streamlined build system with improved publishing scripts and automation
- ✅ **GitHub Actions Integration** - Automated release workflow with semantic versioning based on PR titles
- 🔧 **Build System Improvements** - Enhanced Gradle configuration for better dependency management and module publishing
- 📦 **Dual Repository Support** - Optimized publishing to both Maven Central and GitHub Packages simultaneously
- 🛠️ **Developer Experience** - Improved build scripts and release automation for easier maintenance
- 🔐 **Security Enhancements** - Enhanced PGP signing and credential management in CI/CD pipeline

### Version 1.1.1
- 🔧 **Critical Dependency Fix** - Resolved Maven Central dependency resolution issues with internal project dependencies
- ✅ **Clean POM Generation** - Removed problematic "JarvisDemo.core:common:unspecified" dependencies from published artifacts
- 🚀 **Improved Consumer Experience** - SDK now properly resolves all dependencies when added to consumer projects
- 🛠️ **Build System** - Enhanced publishing configuration to exclude internal project dependencies from transitive resolution

### Version 1.1.0
- ✅ **Enhanced DSIcon Tint System** - Migrated to DSIconTint sealed interface for better type safety and gradient support
- ✅ **Improved Design System** - Updated all icon components to use new tint system with solid colors and gradients
- ✅ **Maven Central Validation** - Fixed dependency version information for okhttp to meet Maven Central publishing requirements
- ✅ **Build System Improvements** - Enhanced gradle configuration for better dependency management
- 🛠️ **Code Quality** - Comprehensive DSIcon component updates across all features and design system
- 🔧 **Type Safety** - Better compile-time validation for icon tinting throughout the SDK

### Version 1.0.28
- ✅ **Interface-Based DI** - Clean dependency injection with NetworkInspectorProvider and PreferencesProvider
- ✅ **Enhanced DSFilterChip** - Fixed visual state issues with proper interaction feedback
- ✅ **Improved Architecture** - Cleaner separation between public API and internal implementation
- ✅ **No-op Implementations** - Graceful degradation when services are unavailable
- ✅ **Maven Central** - Available on Maven Central Repository
- ✅ **PGP Signed** - All artifacts properly signed for security
- 🛠️ **Updated Dependencies** - Kotlin 2.2+, Android API 24+, latest Compose BOM

### Version 1.0.20
- 🔧 **Publishing Fixes** - Resolved signature validation issues
- 🔐 **New PGP Key** - Enhanced security with 4096-bit RSA key
- 📦 **Dual Repository** - Simultaneous GitHub Packages and Maven Central publishing

### Version 1.0.0-1.0.19
- 🚀 **Initial Development** - Core SDK functionality
- 🌐 **Network Monitoring** - HTTP/HTTPS request interception
- ⚙️ **Preferences Management** - SharedPreferences and DataStore support
- 📊 **Performance Tracking** - Real-time analytics and metrics
- 🎨 **Material 3 Design** - Modern UI components
- 📱 **Shake Detection** - Intuitive activation method
- 🎭 **Transparent Bars** - Twitter-style navigation
- 🏗️ **Demo Applications** - Compose and Classic View examples

---

**Built with ❤️ for Android developers**
