package com.jarvis.internal.data.work

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jarvis.features.inspector.internal.domain.repository.NetworkRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * WorkManager worker that periodically cleans up old network requests
 * to keep the datastore clean and prevent performance degradation.
 *
 * Deletes all network requests older than 24 hours.
 *
 * Supports both Hilt (via @HiltWorker) and manual DI (via constructor).
 * If WorkerFactory is not properly configured, worker will log a warning
 * and gracefully fail without crashing the app.
 */
@HiltWorker
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class NetworkCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    networkRepository: NetworkRepository?
) : CoroutineWorker(context, params) {

    private val networkRepository: NetworkRepository? = networkRepository

    /**
     * Fallback constructor for when WorkManager uses reflection
     * (happens when WorkerFactory is not properly configured)
     * This prevents crashes but the worker will fail gracefully
     */
    constructor(
        context: Context,
        params: WorkerParameters
    ) : this(context, params, null)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // Check if networkRepository is available
        if (networkRepository == null) {
            android.util.Log.e(TAG,
                "NetworkRepository not available! WorkManager was not initialized with custom WorkerFactory. " +
                "Please disable WorkManager auto-initialization in AndroidManifest.xml and let Jarvis SDK " +
                "initialize it, or ensure your Application class implements Configuration.Provider.")
            return@withContext Result.failure()
        }

        try {
            // Calculate timestamp for 24 hours ago
            val twentyFourHoursAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)

            // Delete all requests older than 24 hours
            networkRepository.deleteOldTransactions(beforeTimestamp = twentyFourHoursAgo)

            // Log success (in production, this could be sent to analytics)
            android.util.Log.d(TAG, "Successfully cleaned up network requests older than 24 hours")

            Result.success()
        } catch (e: Exception) {
            // Log error and retry
            android.util.Log.e(TAG, "Failed to cleanup old network requests", e)

            // Retry with exponential backoff
            if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        private const val TAG = "NetworkCleanupWorker"
        private const val MAX_RETRY_ATTEMPTS = 3

        // Work request name for uniqueness
        const val WORK_NAME = "network_cleanup_work"
    }
}
