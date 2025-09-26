package com.jarvis.core.internal.navigation

import androidx.annotation.RestrictTo

import javax.inject.Qualifier

/**
 * Qualifier annotation to distinguish the JarvisSDK Navigator from other Navigator instances
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
annotation class JarvisSDKNavigator