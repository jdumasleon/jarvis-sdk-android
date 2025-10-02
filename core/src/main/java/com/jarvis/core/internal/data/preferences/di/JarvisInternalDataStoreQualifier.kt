package com.jarvis.core.internal.data.preferences.di

import androidx.annotation.RestrictTo
import javax.inject.Qualifier

/**
 * Qualifier for Jarvis internal DataStore
 * This distinguishes the internal SDK DataStore from other DataStore instances.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Qualifier
@Retention(AnnotationRetention.BINARY)
internal annotation class JarvisInternalDataStore