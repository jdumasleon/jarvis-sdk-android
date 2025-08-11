package com.jarvis.config

/**
 * Global holder for Jarvis configuration
 * Used to bridge between static initialization and dependency injection
 */
object JarvisConfigHolder {
    
    @Volatile
    private var configuration = JarvisConfig()
    
    /**
     * Update the global configuration
     */
    fun updateConfiguration(config: JarvisConfig) {
        configuration = config
    }
    
    /**
     * Get the current global configuration
     */
    fun getConfiguration(): JarvisConfig {
        return configuration
    }
}