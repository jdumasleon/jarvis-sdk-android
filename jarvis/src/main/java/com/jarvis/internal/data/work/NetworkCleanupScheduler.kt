package com.jarvis.internal.data.work

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Scheduler for periodic network cleanup tasks
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class NetworkCleanupScheduler @Inject constructor(
    private val context: Context,
    private val workerFactory: androidx.work.WorkerFactory
) {

    /**
     * Schedule periodic cleanup of old network requests
     * Runs once per day, deleting requests older than 24 hours
     */
    fun scheduleCleanup() {
        // Ensure WorkManager is initialized with our WorkerFactory
        initializeWorkManager()

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true) // Only run when battery is not low
            .setRequiresDeviceIdle(false) // Can run while device is being used
            .setRequiresCharging(false) // Can run on battery
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // No network needed
            .build()

        val cleanupRequest = PeriodicWorkRequestBuilder<NetworkCleanupWorker>(
            repeatInterval = CLEANUP_INTERVAL_HOURS,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .addTag(TAG_NETWORK_CLEANUP)
            .build()

        // Use KEEP policy to avoid rescheduling if already scheduled
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            NetworkCleanupWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            cleanupRequest
        )
    }

    /**
     * Initialize WorkManager with custom WorkerFactory
     * Must be called before any WorkManager operations
     */
    private fun initializeWorkManager() {
        try {
            // Check if already initialized
            WorkManager.getInstance(context)
        } catch (e: IllegalStateException) {
            // WorkManager not initialized, initialize it with our worker factory
            val configuration = Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build()

            try {
                WorkManager.initialize(context, configuration)
                android.util.Log.d(TAG, "WorkManager initialized with custom WorkerFactory")
            } catch (e: IllegalStateException) {
                // Already initialized by another component
                android.util.Log.w(TAG, "WorkManager already initialized, cannot set custom WorkerFactory. " +
                        "Please ensure WorkManager is not auto-initialized in your AndroidManifest.xml")
            }
        }
    }

    /**
     * Cancel the scheduled cleanup work
     */
    fun cancelCleanup() {
        WorkManager.getInstance(context).cancelUniqueWork(NetworkCleanupWorker.WORK_NAME)
    }

    /**
     * Check if cleanup is currently scheduled
     */
    fun isCleanupScheduled(): Boolean {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(NetworkCleanupWorker.WORK_NAME)
            .get()

        return workInfos.isNotEmpty() && workInfos.any { !it.state.isFinished }
    }

    companion object {
        private const val TAG = "NetworkCleanupScheduler"
        private const val TAG_NETWORK_CLEANUP = "network_cleanup"

        // Run cleanup every 24 hours
        private const val CLEANUP_INTERVAL_HOURS = 24L
    }
}
