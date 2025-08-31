package com.jarvis.features.settings.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.jarvis.core.data.helpers.requestFlow
import com.jarvis.features.settings.domain.entity.AppInfo
import com.jarvis.features.settings.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.toString

/**
 * Implementation of SettingsRepository
 */
class SettingsRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:com.jarvis.core.common.di.CoroutineDispatcherModule.IoDispatcher
    private val ioDispatcher: CoroutineDispatcher
) : SettingsRepository {

    override suspend fun getAppInfo(): Flow<Result<AppInfo>> = requestFlow {
        val packageInfo = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            // Fallback for testing or unexpected scenarios
            null
        }

        AppInfo(
            appName = getApplicationName(),
            version = packageInfo?.versionName ?: "Unknown",
            buildNumber = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo?.longVersionCode?.toString() ?: "Unknown"
            } else {
                @Suppress("DEPRECATION")
                packageInfo?.versionCode?.toString() ?: "Unknown"
            },
            packageName = context.packageName
        )
    }

    private fun getApplicationName(): String {
        val applicationInfo = context.applicationInfo
        val stringId = applicationInfo.labelRes
        return if (stringId == 0) {
            applicationInfo.nonLocalizedLabel?.toString() ?: "Jarvis SDK"
        } else {
            context.getString(stringId)
        }
    }
}