package com.jarvis.config

/**
 * Main configuration class for Jarvis SDK initialization
 */
data class JarvisConfig(
    val preferences: PreferencesConfig = PreferencesConfig(),
    val networkInspection: NetworkInspectionConfig = NetworkInspectionConfig(),
    val enableDebugLogging: Boolean = false,
    val enableShakeDetection: Boolean = false,
    val enableInternalTracking: Boolean = true,
) {
    class Builder {
        private var preferences: PreferencesConfig = PreferencesConfig()
        private var networkInspection: NetworkInspectionConfig = NetworkInspectionConfig()
        private var enableDebugLogging: Boolean = false
        private var enableShakeDetection: Boolean = false
        private var enableInternalTracking: Boolean = true

        fun preferences(block: PreferencesConfig.Builder.() -> Unit): Builder {
            preferences = PreferencesConfig.Builder().apply(block).build()
            return this
        }

        fun networkInspection(block: NetworkInspectionConfig.Builder.() -> Unit): Builder {
            networkInspection = NetworkInspectionConfig.Builder().apply(block).build()
            return this
        }

        fun enableDebugLogging(enabled: Boolean): Builder {
            enableDebugLogging = enabled
            return this
        }

        fun enableShakeDetection(enabled: Boolean): Builder {
            enableShakeDetection = enabled
            return this
        }

        fun enableInternalTracking(enabled: Boolean): Builder {
            enableInternalTracking = enabled
            return this
        }

        fun build(): JarvisConfig = JarvisConfig(
            preferences = preferences,
            networkInspection = networkInspection,
            enableDebugLogging = enableDebugLogging,
            enableShakeDetection = enableShakeDetection,
            enableInternalTracking = enableInternalTracking,
        )
    }

    companion object {
        fun builder(): Builder = Builder()
    }
}