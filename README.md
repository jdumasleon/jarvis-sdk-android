# Jarvis Android SDK

[![Maven Central](https://img.shields.io/maven-central/v/io.github.jdumasleon/jarvis-android-sdk?color=blue)](https://central.sonatype.com/artifact/io.github.jdumasleon/jarvis-android-sdk)
[![GitHub Packages](https://img.shields.io/badge/GitHub%20Packages-Available-green)](https://github.com/jdumasleon/mobile-jarvis-android-sdk/packages)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A comprehensive debugging and development toolkit for Android applications, providing real-time insights into your app's behavior, network traffic, preferences, and performance metrics.

**🎯 Perfect for development and debugging | 📱 Zero overhead in production builds**

## Quick Start

### 1. Add Dependency

Add to your `app/build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.jdumasleon:jarvis-android-sdk:1.0.21")
}
```

### 2. Initialize in Application

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        if (BuildConfig.DEBUG) {
            JarvisSDK.initialize(this)
        }
    }
}
```

### 3. Activate Jarvis

- **Shake your device** (default activation method)
- **Or call programmatically**: `JarvisSDK.activate()`

That's it! 🎉 Jarvis will automatically detect network requests and app preferences.

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
- **Feature toggles** - Runtime configuration switching
- **Crash reporting** - Automatic crash detection and reporting
- **Analytics tracking** - Event monitoring and user behavior insights

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
    // Single artifact automatically provides full functionality in debug, no-op in release
    implementation("io.github.jdumasleon:jarvis-android-sdk:1.0.21")
}
```

#### Alternative: Separate Artifacts (Advanced)

For fine-grained control, you can use separate artifacts:

```kotlin
dependencies {
    // Full SDK for debug builds
    debugImplementation("io.github.jdumasleon:jarvis-android-sdk:1.0.21")
    
    // No-op version for release builds (zero overhead)
    releaseImplementation("io.github.jdumasleon:jarvis-android-sdk-noop:1.0.21")
}
```

### Maven Central

```xml
<dependency>
    <groupId>io.github.jdumasleon</groupId>
    <artifactId>jarvis-android-sdk</artifactId>
    <version>1.0.21</version>
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

### Automatic Debug/Release Optimization

The Jarvis SDK automatically adapts based on your build type:

#### Single Artifact Approach (Recommended)
- ✅ **Automatic switching** - Full functionality in debug, no-op in release
- ✅ **Zero overhead in production** - All methods become no-ops in release builds
- ✅ **Same API everywhere** - No code changes needed
- ✅ **ProGuard/R8 optimization** - Dead code elimination removes unused code

```kotlin
dependencies {
    implementation("io.github.jdumasleon:jarvis-android-sdk:1.0.21")  // Works everywhere
}
```

#### Separate Artifacts (Advanced)
For projects requiring explicit control:

```kotlin
dependencies {
    debugImplementation("io.github.jdumasleon:jarvis-android-sdk:1.0.21")        // Full features
    releaseImplementation("io.github.jdumasleon:jarvis-android-sdk-noop:1.0.21") // Zero overhead
}
```

## Integration Guide

### Prerequisites

**🔧 Required:**
- Android API 21+ (Android 5.0 Lollipop)
- Kotlin 1.9+
- Hilt for dependency injection
- Jetpack Compose (recommended) or Android Views

**📦 Optional but Recommended:**
- OkHttp for automatic network interception
- DataStore for preferences management
- Proto DataStore for complex data structures

### Step-by-Step Integration

#### 1. Application Setup

Add `@HiltAndroidApp` to your Application class:

```kotlin
@HiltAndroidApp
class MyApplication : Application() {
    // No additional code needed - SDK is auto-injected via Hilt
}
```

#### 2. Activity Integration

**For Jetpack Compose Activities:**

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var jarvisSDK: JarvisSDK
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configure and initialize Jarvis SDK
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
            .enableDebugLogging(BuildConfig.DEBUG)
            .preferences {
                autoDiscoverDataStores(true)
                autoDiscoverSharedPrefs(true)
                enablePreferenceEditing(true)
            }
            .networkInspection {
                enableNetworkLogging(true)
                enableRequestLogging(true)
                enableResponseLogging(true)
            }
            .build()
            
        jarvisSDK.initialize(config = config, hostActivity = this)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        jarvisSDK.dismiss()
    }
}
```

**For Traditional Activities (Views):**

```kotlin
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    @Inject
    lateinit var jarvisSDK: JarvisSDK
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize Jarvis SDK
        val config = JarvisConfig.builder()
            .enableShakeDetection(true)
            .build()
            
        jarvisSDK.initialize(config = config, hostActivity = this)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        jarvisSDK.dismiss()
    }
}
```

#### 3. Network Monitoring Setup

**Automatic OkHttp Integration:**

```kotlin
@Singleton
class NetworkModule {
    
    @Inject
    lateinit var jarvisNetworkInspector: JarvisNetworkInspector
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(jarvisNetworkInspector.createInterceptor())
            .build()
    }
}
```

**Manual Network Logging:**

```kotlin
// For custom network clients
JarvisNetworkLogger.logRequest(
    url = "https://api.example.com/data",
    method = "POST",
    headers = mapOf("Authorization" to "Bearer token"),
    body = requestBody
)

JarvisNetworkLogger.logResponse(
    requestId = requestId,
    statusCode = 200,
    responseBody = responseBody,
    duration = 1500L
)
```

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
android {
    buildTypes {
        debug {
            // Jarvis SDK is automatically enabled in debug builds
            buildConfigField("boolean", "JARVIS_ENABLED", "true")
        }
        release {
            // Jarvis SDK is automatically disabled in release builds
            buildConfigField("boolean", "JARVIS_ENABLED", "false")
        }
    }
}

dependencies {
    // Single artifact approach (recommended)
    implementation("io.github.jdumasleon:jarvis-android-sdk:1.0.21")
    
    // Or separate artifacts for explicit control
    // debugImplementation("io.github.jdumasleon:jarvis-android-sdk:1.0.21")
    // releaseImplementation("io.github.jdumasleon:jarvis-android-sdk-noop:1.0.21")
}
```

#### 6. Permissions (Optional)

Add to your `AndroidManifest.xml` if you want network monitoring:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
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

- ✅ **Hilt Setup**: `@HiltAndroidApp` on Application class
- ✅ **Activity Annotations**: `@AndroidEntryPoint` on Activities using SDK
- ✅ **SDK Initialization**: Called in Activity `onCreate()`
- ✅ **SDK Cleanup**: `dismiss()` called in Activity `onDestroy()`
- ✅ **Build Variants**: Different behavior for debug/release confirmed
- ✅ **Network Integration**: OkHttp interceptor added if using network monitoring
- ✅ **DataStore Registration**: Custom DataStores registered if needed
- ✅ **Permissions**: Network permissions added if required
- ✅ **ProGuard Rules**: Jarvis SDK rules added if using code obfuscation

## Usage

### Activation Methods

#### 1. Shake Detection (Default)
Simply shake your device to open Jarvis.

#### 2. Programmatic Activation
```kotlin
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

#### Manual Network Logging
```kotlin
// Log a custom network request
JarvisNetworkLogger.logRequest(
    url = "https://api.example.com/data",
    method = "POST",
    headers = mapOf("Authorization" to "Bearer token"),
    body = requestBody
)

// Log the response
JarvisNetworkLogger.logResponse(
    requestId = requestId,
    statusCode = 200,
    headers = responseHeaders,
    body = responseBody,
    duration = 1500L
)
```

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

In release builds, the Jarvis SDK automatically becomes a no-op implementation:

```kotlin
// All these methods become no-ops in release builds
jarvisSDK.initialize(config, hostActivity) // Does nothing
jarvisSDK.activate()                        // Does nothing  
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

## Proguard/R8 Configuration

Add to your `proguard-rules.pro`:

```proguard
# Jarvis SDK
-keep class com.jarvis.api.** { *; }
-keep class com.jarvis.core.** { public *; }
-dontwarn com.jarvis.**

# Network monitoring
-keepattributes Signature
-keepattributes *Annotation*
```

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
3. Use release no-op version for production
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

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Update documentation
6. Submit a pull request

### Development Setup

```bash
# Clone repository
git clone https://github.com/jdumasleon/mobile-jarvis-android-sdk.git

# Open in Android Studio
# Select "Open an existing Android Studio project"
# Choose the cloned directory

# Build project
./gradlew build

# Run tests
./gradlew test
```

### Publishing Workflow

The project includes an automated publishing script for maintainers:

```bash
# Publish to all repositories (GitHub Packages + Maven Central)
./scripts/publish.sh 1.0.22 all

# Publish to specific repository
./scripts/publish.sh 1.0.22 github    # GitHub Packages only
./scripts/publish.sh 1.0.22 maven     # Maven Central only  
./scripts/publish.sh 1.0.22 local     # Local Maven only

# Dry run (test without publishing)
./scripts/publish.sh 1.0.22 all --dry-run
```

**Publishing Requirements:**
- Valid PGP key for artifact signing
- Maven Central credentials (username/password)
- GitHub Personal Access Token for GitHub Packages
- Proper repository permissions

The publishing script automatically:
- ✅ Updates version numbers
- ✅ Builds and signs artifacts
- ✅ Publishes to repositories simultaneously
- ✅ Creates git tags
- ✅ Validates signatures and credentials

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

### Version 1.0.21 (Latest)
- ✅ **Stable Release** - Production-ready SDK
- ✅ **Maven Central** - Available on Maven Central Repository
- ✅ **GitHub Packages** - Available on GitHub Packages
- ✅ **Optimized Publishing** - Single execution publishing workflow
- ✅ **PGP Signed** - All artifacts properly signed for security
- 🛠️ **Improved Build System** - Enhanced Gradle configuration
- 📚 **Complete Documentation** - Comprehensive setup and usage guide

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