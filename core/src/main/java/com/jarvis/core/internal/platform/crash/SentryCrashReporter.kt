@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.core.internal.platform.crash

import androidx.annotation.RestrictTo

import com.jarvis.core.internal.common.di.CoroutineDispatcherModule
import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SentryLevel
import io.sentry.protocol.User
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sentry implementation of CrashReporter
 */
@Singleton
class SentryCrashReporter @Inject constructor(
    @param:CoroutineDispatcherModule.IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CrashReporter {

    override suspend fun initialize() = withContext(ioDispatcher) {
        // Sentry is automatically initialized from AndroidManifest.xml in the Jarvis SDK
        // Set up additional SDK-specific configuration
        Sentry.configureScope { scope ->
            scope.setTag("sdk", "jarvis-android")
            scope.setTag("version", "1.0.0")
            scope.setTag("platform", "android")
        }
    }

    override suspend fun recordException(
        throwable: Throwable,
        tags: Map<String, String>
    ) {
        withContext(ioDispatcher) {
            Sentry.configureScope { scope ->
                tags.forEach { (key, value) ->
                    scope.setTag(key, value)
                }
            }
            Sentry.captureException(throwable)
        }
    }

    override suspend fun log(message: String, level: LogLevel) {
        withContext(ioDispatcher) {
            val sentryLevel = when (level) {
                LogLevel.DEBUG -> SentryLevel.DEBUG
                LogLevel.INFO -> SentryLevel.INFO
                LogLevel.WARNING -> SentryLevel.WARNING
                LogLevel.ERROR -> SentryLevel.ERROR
                LogLevel.FATAL -> SentryLevel.FATAL
            }
            Sentry.captureMessage(message, sentryLevel)
        }
    }

    override suspend fun setUser(
        userId: String,
        email: String?,
        username: String?
    ) = withContext(ioDispatcher) {
        Sentry.configureScope { scope ->
            scope.user = User().apply {
                id = userId
                this.email = email
                this.username = username
            }
        }
    }

    override suspend fun setTag(key: String, value: String) = withContext(ioDispatcher) {
        Sentry.configureScope { scope ->
            scope.setTag(key, value)
        }
    }

    override suspend fun setTags(tags: Map<String, String>) = withContext(ioDispatcher) {
        Sentry.configureScope { scope ->
            tags.forEach { (key, value) ->
                scope.setTag(key, value)
            }
        }
    }

    override suspend fun addBreadcrumb(
        message: String,
        category: String,
        level: LogLevel
    ) = withContext(ioDispatcher) {
        val sentryLevel = when (level) {
            LogLevel.DEBUG -> SentryLevel.DEBUG
            LogLevel.INFO -> SentryLevel.INFO
            LogLevel.WARNING -> SentryLevel.WARNING
            LogLevel.ERROR -> SentryLevel.ERROR
            LogLevel.FATAL -> SentryLevel.FATAL
        }
        
        val breadcrumb = Breadcrumb().apply {
            this.message = message
            this.category = category
            this.level = sentryLevel
        }
        
        Sentry.addBreadcrumb(breadcrumb)
    }

    override suspend fun setContext(
        key: String,
        context: Map<String, Any>
    ) = withContext(ioDispatcher) {
        Sentry.configureScope { scope ->
            scope.setContexts(key, context)
        }
    }

    override suspend fun setEnabled(enabled: Boolean) = withContext(ioDispatcher) {
        // Sentry doesn't have a direct way to disable at runtime
        // This would typically be handled during initialization
        if (enabled) {
            Sentry.configureScope { scope ->
                scope.clear()
            }
        }
    }
}