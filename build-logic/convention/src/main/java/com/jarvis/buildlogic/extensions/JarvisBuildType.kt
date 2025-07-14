package com.jarvis.buildlogic.extensions

/**
 * This is shared between :app and :benchmarks module to provide configurations type safety.
 */
enum class JarvisBuildType(val applicationIdSuffix: String? = null) {
    DEBUG(".debug"),
    RELEASE,
}
