@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.core.internal.platform.analytics

import androidx.annotation.RestrictTo

import android.content.Context
import com.jarvis.core.internal.common.di.CoroutineDispatcherModule
import com.jarvis.core.BuildConfig
import com.posthog.PostHog
import com.posthog.android.PostHogAndroid
import com.posthog.android.PostHogAndroidConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PostHog implementation of Analytics
 */
@Singleton
class PostHogAnalytics @Inject constructor(
    @ApplicationContext private val context: Context,
    @param:CoroutineDispatcherModule.IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : Analytics {

    private var isInitialized = false

    private fun ensureInitialized() {
        if (!isInitialized) {
            val config = PostHogAndroidConfig(
                apiKey = BuildConfig.POSTHOG_API_KEY,
                host = "https://eu.i.posthog.com"
            ).apply {
                debug = true
                captureApplicationLifecycleEvents = true
                captureDeepLinks = true
                captureScreenViews = true
            }
            
            PostHogAndroid.setup(context, config)
            isInitialized = true
        }
    }

    override suspend fun track(event: AnalyticsEvent) = withContext(ioDispatcher) {
        ensureInitialized()
        
        val properties = event.properties.toMutableMap().apply {
            put("timestamp", event.timestamp)
            event.userId?.let { put("user_id", it) }
            event.sessionId?.let { put("session_id", it) }
        }
        
        PostHog.capture(
            event = event.name,
            properties = properties
        )
    }

    override suspend fun track(
        eventName: String,
        properties: Map<String, Any>
    ) = withContext(ioDispatcher) {
        ensureInitialized()
        PostHog.capture(event = eventName, properties = properties)
    }

    override suspend fun identify(userProfile: UserProfile) = withContext(ioDispatcher) {
        ensureInitialized()
        
        val properties = userProfile.properties.toMutableMap().apply {
            userProfile.email?.let { put("email", it) }
            userProfile.name?.let { put("name", it) }
        }
        
        PostHog.identify(userProfile.userId, properties)
    }

    override suspend fun setUserProperties(userProfile: UserProfile, properties: Map<String, Any>) = withContext(ioDispatcher) {
        ensureInitialized()
        PostHog.identify(distinctId =  userProfile.userId, userProperties = properties)
    }

    override suspend fun setEnabled(enabled: Boolean) = withContext(ioDispatcher) {
        ensureInitialized()
        if (enabled) {
            PostHog.optIn()
        } else {
            PostHog.optOut()
        }
    }

    override suspend fun reset() = withContext(ioDispatcher) {
        ensureInitialized()
        PostHog.reset()
    }

    override suspend fun flush() = withContext(ioDispatcher) {
        ensureInitialized()
        PostHog.flush()
    }
}