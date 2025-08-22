# Jarvis Android SDK

A comprehensive debugging and development toolkit for Android applications, providing real-time insights into your app's behavior, network traffic, preferences, and performance metrics.

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

### Gradle Setup

Add the Jarvis SDK to your `app/build.gradle` file:

```kotlin
dependencies {
    // For debug builds only
    debugImplementation 'com.jarvis:jarvis-sdk:1.0.0'
    
    // Optional: Release no-op version
    releaseImplementation 'com.jarvis:jarvis-sdk-noop:1.0.0'
}
```

### Maven Central

```xml
<dependency>
    <groupId>com.jarvis</groupId>
    <artifactId>jarvis-sdk</artifactId>
    <version>1.0.0</version>
    <scope>runtime</scope>
</dependency>
```

## Integration

### 1. Application Setup

Initialize Jarvis in your Application class:

```kotlin
class MyApplication : Application() {
    
    @Inject
    lateinit var jarvisSDK: JarvisSDK
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Jarvis SDK
        if (BuildConfig.DEBUG) {
            jarvisSDK.initialize(
                config = JarvisConfig(
                    enableShakeDetection = true,
                    enableNetworkMonitoring = true,
                    enablePreferencesInspection = true,
                    enablePerformanceTracking = true
                )
            )
        }
    }
}
```

### 2. Activity Integration

Add Jarvis to your main Activity:

```kotlin
class MainActivity : AppCompatActivity() {
    
    @Inject
    lateinit var jarvisSDK: JarvisSDK
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyAppTheme {
                // Your app content
                MyAppContent()
            }
        }
        
        // Initialize Jarvis for this Activity
        if (BuildConfig.DEBUG) {
            jarvisSDK.initialize(hostActivity = this)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (BuildConfig.DEBUG) {
            jarvisSDK.dismiss()
        }
    }
}
```

### 3. Hilt/Dagger Integration

Configure dependency injection:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object JarvisModule {
    
    @Provides
    @Singleton
    fun provideJarvisSDK(
        @ApplicationContext context: Context,
        configSync: ConfigurationSynchronizer,
        performanceManager: PerformanceManager
    ): JarvisSDK {
        return JarvisSDK(context, configSync, performanceManager)
    }
}
```

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

## Configuration

### JarvisConfig Options

```kotlin
val config = JarvisConfig(
    // Core Features
    enableShakeDetection = true,
    enableNetworkMonitoring = true,
    enablePreferencesInspection = true,
    enablePerformanceTracking = true,
    enableErrorTracking = true,
    
    // UI Configuration
    enableTransparentBars = true,
    themeMode = ThemeMode.AUTO, // AUTO, LIGHT, DARK
    fabPosition = FabPosition.BOTTOM_END,
    
    // Network Settings
    networkLogRetentionDays = 7,
    maxNetworkLogSize = 100, // MB
    captureRequestBodies = true,
    captureResponseBodies = true,
    
    // Performance Settings
    performanceMetricsInterval = 5000L, // 5 seconds
    enableMemoryProfiling = true,
    enableCpuProfiling = true,
    
    // Privacy Settings
    enableDataExport = true,
    enableRemoteLogging = false,
    anonymizeUserData = true
)
```

### Runtime Configuration

```kotlin
// Update configuration at runtime
jarvisSDK.updateConfiguration(
    config.copy(enableNetworkMonitoring = false)
)

// Get current configuration
val currentConfig = jarvisSDK.getConfiguration()
```

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
git clone https://github.com/your-org/jarvis-android-sdk.git

# Open in Android Studio
# Select "Open an existing Android Studio project"
# Choose the cloned directory

# Build project
./gradlew build

# Run tests
./gradlew test
```

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

### Version 1.0.0
- Initial release
- Network monitoring with HTTP/HTTPS support
- Preferences management for SharedPreferences and DataStore
- Performance tracking and analytics
- Material 3 design system
- Shake detection activation
- Twitter-style transparent navigation bars
- Compose and Classic View demos

---

**Built with ❤️ for Android developers**