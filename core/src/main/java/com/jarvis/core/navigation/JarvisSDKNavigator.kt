package com.jarvis.core.navigation

import javax.inject.Qualifier

/**
 * Qualifier annotation to distinguish the JarvisSDK Navigator from other Navigator instances
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class JarvisSDKNavigator