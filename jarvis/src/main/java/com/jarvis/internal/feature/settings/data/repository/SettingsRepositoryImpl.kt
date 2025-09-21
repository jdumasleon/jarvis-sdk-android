package com.jarvis.internal.feature.settings.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.jarvis.core.common.di.CoroutineDispatcherModule
import com.jarvis.core.data.helpers.requestFlow
import com.jarvis.internal.feature.settings.domain.entity.AppInfo
import com.jarvis.internal.feature.settings.domain.entity.SettingsAppInfo
import com.jarvis.internal.feature.settings.domain.entity.SdkInfo
import com.jarvis.internal.feature.settings.domain.entity.HostAppInfo
import com.jarvis.internal.feature.settings.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Implementation of SettingsRepository
 */
class SettingsRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:CoroutineDispatcherModule.IoDispatcher
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

    override suspend fun getSdkInfo(): Flow<Result<SdkInfo>> = requestFlow {
        // Get SDK version from a hardcoded value or from resources
        val sdkVersion = "1.0.28" // This should ideally come from BuildConfig or resources
        SdkInfo(
            version = sdkVersion,
            buildNumber = extractBuildNumber(sdkVersion)
        )
    }

    override suspend fun getHostAppInfo(): Flow<Result<HostAppInfo>> = requestFlow {
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
            null
        }

        val applicationInfo = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getApplicationInfo(
                    context.packageName,
                    PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }

        val permissions = try {
            val permissionPackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
            }
            permissionPackageInfo.requestedPermissions?.toList() ?: emptyList<String>()
        } catch (e: PackageManager.NameNotFoundException) {
            emptyList<String>()
        }

        HostAppInfo(
            appName = getApplicationName(),
            version = packageInfo?.versionName ?: "Unknown",
            buildNumber = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo?.longVersionCode?.toString() ?: "Unknown"
            } else {
                @Suppress("DEPRECATION")
                packageInfo?.versionCode?.toString() ?: "Unknown"
            },
            packageName = context.packageName,
            minSdkVersion = applicationInfo?.minSdkVersion,
            targetSdkVersion = applicationInfo?.targetSdkVersion,
            permissions = permissions
        )
    }

    override suspend fun getSettingsAppInfo(): Flow<Result<SettingsAppInfo>> =
        combine(getSdkInfo(), getHostAppInfo()) { sdkResult, hostResult ->
            when {
                sdkResult.isFailure -> Result.failure(sdkResult.exceptionOrNull() ?: Exception("SDK info error"))
                hostResult.isFailure -> Result.failure(hostResult.exceptionOrNull() ?: Exception("Host app info error"))
                else -> Result.success(
                    SettingsAppInfo(
                        sdkInfo = sdkResult.getOrThrow(),
                        hostAppInfo = hostResult.getOrThrow()
                    )
                )
            }
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

    private fun extractBuildNumber(version: String): String {
        // Extract build number from version (e.g., "1.0.28" -> "28")
        return version.substringAfterLast(".").ifEmpty { "1" }
    }
}