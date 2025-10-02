# Jarvis Demo App - Koin Version

This is a **Koin-based demo application** that replicates exactly the same functionality as the existing Hilt demo app, but uses **Koin for dependency injection** instead of Hilt.

## Overview

This demo app showcases:
- **Complete Jarvis SDK integration** using Koin dependency injection
- **Home screen** with SDK activation/deactivation controls
- **Network Inspector** with random API calls to FakeStore API and RestfulAPI
- **Preferences screen** with DataStore, SharedPreferences, and ProtoDataStore management
- **Clean Architecture** with Domain/Data/Presentation layers
- **Jetpack Compose UI** with modern Android development practices

## Key Features

### 🏠 **Home Screen**
- Jarvis SDK activation toggle
- Welcome message and app status
- Last refresh timestamp
- Shake detection instructions

### 🔍 **Inspector Screen**
- Random API calls to demonstrate network monitoring
- FakeStore API integration (products, users, carts, etc.)
- RestfulAPI integration (CRUD operations)
- Network call history and details

### ⚙️ **Preferences Screen**
- SharedPreferences management
- DataStore Preferences viewing/editing
- Proto DataStore integration
- Multi-storage type support

## Architecture

### **Dependency Injection - Koin**
```kotlin
// Application.kt
startKoin {
    androidContext(this@KoinDemoApplication)
    modules(
        // Demo app modules
        networkModule,
        repositoryModule,
        useCaseModule,
        viewModelModule,
        navigationModule,
        // Jarvis SDK modules
        *allJarvisKoinModules.toTypedArray()
    )
}
```

### **SDK Integration**
```kotlin
// MainActivity.kt
class MainActivity : ComponentActivity() {
    // Koin dependency injection
    private val jarvisSDK: JarvisSDK by createJarvisSDKWithKoin()

    override fun onCreate(savedInstanceState: Bundle?) {
        jarvisSDK.attach(this)
        jarvisSDK.initializeAsync(config = demoConfig, hostActivity = this)
    }
}
```

### **ViewModel Integration**
```kotlin
// HomeScreen.kt
@Composable
internal fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
) {
    // UI implementation
}
```

## Project Structure

```
app-koin/
├── src/main/java/com/jarvis/demo/koin/
│   ├── KoinDemoApplication.kt              # Koin initialization
│   ├── di/                                 # Dependency injection modules
│   │   ├── NetworkModule.kt                # Network dependencies
│   │   ├── RepositoryModule.kt             # Repository dependencies
│   │   ├── UseCaseModule.kt                # Use case dependencies
│   │   ├── ViewModelModule.kt              # ViewModel dependencies
│   │   └── NavigationModule.kt             # Navigation dependencies
│   ├── data/                               # Data layer
│   │   ├── api/                            # API services
│   │   ├── repository/                     # Repository implementations
│   │   └── preferences/                    # Preference managers
│   ├── domain/                             # Domain layer
│   │   └── usecase/                        # Use case implementations
│   └── presentation/                       # Presentation layer
│       ├── MainActivity.kt                 # Main activity
│       ├── home/                           # Home feature
│       ├── inspector/                      # Inspector feature
│       ├── preferences/                    # Preferences feature
│       └── ui/                             # Common UI components
├── build.gradle.kts                        # Koin dependencies
└── README.md                               # This file
```

## Dependencies

### **Koin Dependencies**
```kotlin
implementation("io.insert-koin:koin-android:3.5.0")
implementation("io.insert-koin:koin-androidx-compose:3.5.0")
implementation("io.insert-koin:koin-core:3.5.0")
```

### **Jarvis SDK Dependencies**
```kotlin
implementation(project(":jarvis"))
implementation(project(":core"))
implementation(project(":features:inspector"))
implementation(project(":features:preferences"))
```

## Key Differences from Hilt Version

| Aspect | Hilt Version | Koin Version |
|--------|-------------|-------------|
| **DI Framework** | Hilt with annotations | Koin with DSL |
| **Module Declaration** | `@Module @InstallIn` | `val module = module { }` |
| **Dependency Injection** | `@Inject` constructor | `by inject()` delegate |
| **ViewModel Integration** | `hiltViewModel()` | `koinViewModel()` |
| **Application Class** | `@HiltAndroidApp` | `startKoin { }` |
| **Activity Integration** | `@AndroidEntryPoint` | No annotations needed |

## Usage

### **Building the App**
```bash
./gradlew :app-koin:assembleDebug
```

### **Running Tests**
```bash
./gradlew :app-koin:testDebugUnitTest
```

### **Installing**
```bash
./gradlew :app-koin:installDebug
```

## Configuration

The demo app includes comprehensive Jarvis SDK configuration:

```kotlin
val demoConfig = JarvisConfig.builder()
    .enableShakeDetection(true)
    .enableDebugLogging(true)
    .preferences {
        autoDiscoverDataStores(true)
        autoDiscoverSharedPrefs(true)
        autoDiscoverProtoDataStores(true)
        enablePreferenceEditing(true)
        showSystemPreferences(true)
    }
    .networkInspection {
        enableNetworkLogging(true)
        enableRequestLogging(true)
        enableResponseLogging(true)
    }
    .build()
```

## API Endpoints Used

### **FakeStore API**
- `GET /products` - Fetch all products
- `GET /products/{id}` - Fetch specific product
- `GET /products?limit={n}` - Fetch limited products
- `GET /products/categories` - Fetch categories
- `POST /products` - Create product
- `GET /carts` - Fetch carts
- `GET /users` - Fetch users
- `POST /auth/login` - User login

### **RestfulAPI**
- `GET /objects` - Fetch all objects
- `GET /objects/{id}` - Fetch specific object
- `POST /objects` - Create object
- `PUT /objects/{id}` - Update object
- `PATCH /objects/{id}` - Partial update object
- `DELETE /objects/{id}` - Delete object

## Benefits of Koin Version

1. **No Annotation Processing** - Faster compilation times
2. **Pure Kotlin DSL** - More readable and flexible
3. **Lightweight** - Smaller runtime footprint
4. **Easy Testing** - Simple mock injection
5. **Gradual Migration** - Can coexist with other DI frameworks

## Notes

- This app demonstrates that the **Jarvis SDK works seamlessly with Koin**
- All original functionality is preserved
- **No Hilt dependencies required**
- Uses the same APIs and UI as the original demo
- Maintains clean architecture principles

This Koin demo app proves that the Jarvis Android SDK can be **easily integrated into any Android project regardless of the dependency injection framework used**.