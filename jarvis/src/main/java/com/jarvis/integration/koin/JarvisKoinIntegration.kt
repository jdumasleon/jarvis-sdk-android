package com.jarvis.integration.koin

import androidx.activity.ComponentActivity
import com.jarvis.api.JarvisSDK
import com.jarvis.config.JarvisConfig
import org.koin.android.ext.android.inject

/**
 * Extension function to inject JarvisSDK using Koin.
 *
 * Usage in Activity:
 * ```kotlin
 * class MainActivity : ComponentActivity() {
 *     private val jarvisSDK: JarvisSDK by inject()
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *
 *         // Initialize SDK with Koin-compatible method
 *         lifecycleScope.launch {
 *             jarvisSDK.initializeWithKoin(
 *                 config = JarvisConfig.builder()
 *                     .enableShakeDetection(true)
 *                     .build(),
 *                 hostActivity = this@MainActivity
 *             )
 *         }
 *     }
 * }
 * ```
 */
suspend fun JarvisSDK.initializeWithKoin(
    config: JarvisConfig = JarvisConfig(),
    hostActivity: ComponentActivity
) {
    // Get entry providers from Koin instead of Hilt
    val koin = org.koin.core.context.GlobalContext.get()
    val entryProviders = koin.get<Set<com.jarvis.core.internal.navigation.EntryProviderInstaller>>(
        org.koin.core.qualifier.named("jarvis_entry_providers")
    )

    // Initialize SDK with entry providers
    initializeWithEntryProviders(config, hostActivity, entryProviders)
}
