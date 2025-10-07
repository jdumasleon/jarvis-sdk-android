package com.jarvis.example.koin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jarvis.api.JarvisSDK
import com.jarvis.integration.koin.createJarvisSDKWithKoin
import kotlinx.coroutines.launch

/**
 * Example Activity showing different ways to integrate Jarvis SDK with Koin
 */
class KoinExampleActivity : ComponentActivity() {

    // Method 1: Using extension function (Recommended)
    private val jarvisSDK: JarvisSDK by createJarvisSDKWithKoin()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize and attach Jarvis SDK
        initializeJarvisSDK()

        setContent {
            ExampleTheme {
                ExampleScreen()
            }
        }
    }

    private fun initializeJarvisSDK() {
        // Attach SDK to activity lifecycle
        jarvisSDK.attach(this)

        // Optional: Configure SDK
        lifecycleScope.launch {
            jarvisSDK.initialize(
                activity = this@KoinExampleActivity,
                configuration = createJarvisConfiguration()
            )
        }
    }

    private fun createJarvisConfiguration(): JarvisConfig {
        return JarvisConfig().apply {
            // Your configuration settings
            enableShakeToShow = true
            enableNetworkInspection = true
            enablePerformanceMonitoring = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Important: Detach SDK to prevent memory leaks
        jarvisSDK.detach()
    }
}

/**
 * Example Composable showing SDK interaction
 */
@Composable
fun ExampleScreen() {
    var sdkActive by remember { mutableStateOf(false) }
    var showInspector by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Jarvis SDK Koin Integration Example",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // SDK Status
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "SDK Status",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (sdkActive) "Active" else "Inactive",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (sdkActive)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Control Buttons
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "SDK Controls",
                    style = MaterialTheme.typography.titleMedium
                )

                Button(
                    onClick = {
                        sdkActive = !sdkActive
                        // In real implementation, you would call:
                        // jarvisSDK.setActive(sdkActive)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (sdkActive) "Deactivate SDK" else "Activate SDK")
                }

                Button(
                    onClick = {
                        showInspector = true
                        // In real implementation, you would call:
                        // jarvisSDK.showInspector()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = sdkActive
                ) {
                    Text("Show Network Inspector")
                }

                Button(
                    onClick = {
                        // In real implementation, you would call:
                        // jarvisSDK.showPreferences()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = sdkActive
                ) {
                    Text("Show Preferences")
                }
            }
        }

        // Information Card
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Integration Info",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "• SDK initialized with Koin dependency injection\n" +
                          "• All dependencies resolved from Koin container\n" +
                          "• Compatible with existing Koin setup\n" +
                          "• No Hilt dependencies required",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Alternative Activity implementation showing manual builder pattern
 */
class ManualBuilderExampleActivity : ComponentActivity() {

    private lateinit var jarvisSDK: JarvisSDK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Method 2: Manual builder with individual dependencies
        setupJarvisSDKWithManualBuilder()

        setContent {
            ExampleTheme {
                ExampleScreen()
            }
        }
    }

    private fun setupJarvisSDKWithManualBuilder() {
        // Get dependencies from Koin manually
        val configurationSynchronizer: ConfigurationSynchronizer by inject()
        val performanceManager: PerformanceManager by inject()
        val jarvisPlatform: JarvisPlatform by inject()
        val navigator: Navigator by inject(named("jarvis_sdk"))

        // Build SDK using builder pattern
        jarvisSDK = JarvisSDKBuilder.create(this)
            .withConfigurationSynchronizer(configurationSynchronizer)
            .withPerformanceManager(performanceManager)
            .withJarvisPlatform(jarvisPlatform)
            .withNavigator(navigator)
            .build()

        jarvisSDK.attach(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        jarvisSDK.detach()
    }
}

/**
 * Alternative Activity implementation showing context-based initialization
 */
class ContextBasedExampleActivity : ComponentActivity() {

    private lateinit var jarvisSDK: JarvisSDK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Method 3: Context-based creation (useful for non-Activity contexts)
        jarvisSDK = createJarvisSDKWithKoin(this)
        jarvisSDK.attach(this)

        setContent {
            ExampleTheme {
                ExampleScreen()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        jarvisSDK.detach()
    }
}

// Example theme (you would use your own)
@Composable
fun ExampleTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}