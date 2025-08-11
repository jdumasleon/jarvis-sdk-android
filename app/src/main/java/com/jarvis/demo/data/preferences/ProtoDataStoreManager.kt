package com.jarvis.demo.data.preferences

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import com.jarvis.demo.data.preferences.proto.UserSettings
import com.jarvis.demo.presentation.preferences.PreferenceItem
import com.jarvis.demo.presentation.preferences.PreferenceType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

object UserSettingsSerializer : Serializer<UserSettings> {
    override val defaultValue: UserSettings = UserSettings.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): UserSettings {
        try {
            return UserSettings.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: UserSettings,
        output: OutputStream
    ) = t.writeTo(output)
}

private val Context.userSettingsDataStore: DataStore<UserSettings> by dataStore(
    fileName = "user_settings.pb",
    serializer = UserSettingsSerializer
)

@Singleton
class ProtoDataStoreManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val userSettingsDataStore = context.userSettingsDataStore

    /**
     * Get the DataStore instance for external access (e.g., Jarvis SDK registration)
     */
    fun getDataStore(): DataStore<UserSettings> = userSettingsDataStore

    fun getAllPreferencesFlow(): Flow<List<PreferenceItem>> {
        return userSettingsDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(UserSettings.getDefaultInstance())
                } else {
                    throw exception
                }
            }
            .map { userSettings ->
                buildList {
                    add(PreferenceItem("username", userSettings.username, PreferenceType.STRING))
                    add(PreferenceItem("is_premium", userSettings.isPremium.toString(), PreferenceType.BOOLEAN))
                    add(PreferenceItem("preferred_language", userSettings.preferredLanguage, PreferenceType.STRING))
                    add(PreferenceItem("email_notifications", userSettings.emailNotifications.toString(), PreferenceType.BOOLEAN))
                    add(PreferenceItem("push_notifications", userSettings.pushNotifications.toString(), PreferenceType.BOOLEAN))
                    add(PreferenceItem("marketing_notifications", userSettings.marketingNotifications.toString(), PreferenceType.BOOLEAN))
                    add(PreferenceItem("theme_preference", userSettings.themePreference, PreferenceType.STRING))
                    add(PreferenceItem("font_size_multiplier", userSettings.fontSizeMultiplier.toString(), PreferenceType.NUMBER))
                    add(PreferenceItem("dark_mode_enabled", userSettings.darkModeEnabled.toString(), PreferenceType.BOOLEAN))
                    add(PreferenceItem("analytics_enabled", userSettings.analyticsEnabled.toString(), PreferenceType.BOOLEAN))
                    add(PreferenceItem("crash_reporting_enabled", userSettings.crashReportingEnabled.toString(), PreferenceType.BOOLEAN))
                    add(PreferenceItem("location_sharing", userSettings.locationSharing.toString(), PreferenceType.BOOLEAN))
                    add(PreferenceItem("auto_sync", userSettings.autoSync.toString(), PreferenceType.BOOLEAN))
                    add(PreferenceItem("sync_interval_minutes", userSettings.syncIntervalMinutes.toString(), PreferenceType.NUMBER))
                    add(PreferenceItem("wifi_only_sync", userSettings.wifiOnlySync.toString(), PreferenceType.BOOLEAN))
                    add(PreferenceItem("max_cache_size_mb", userSettings.maxCacheSizeMb.toString(), PreferenceType.NUMBER))
                    add(PreferenceItem("cache_expiry_hours", userSettings.cacheExpiryHours.toString(), PreferenceType.NUMBER))
                    add(PreferenceItem("session_timeout_minutes", userSettings.sessionTimeoutMinutes.toString(), PreferenceType.NUMBER))
                    add(PreferenceItem("remember_login", userSettings.rememberLogin.toString(), PreferenceType.BOOLEAN))
                    add(PreferenceItem("beta_features_enabled", userSettings.betaFeaturesEnabled.toString(), PreferenceType.BOOLEAN))
                    add(PreferenceItem("developer_mode", userSettings.developerMode.toString(), PreferenceType.BOOLEAN))
                    add(PreferenceItem("login_count", userSettings.loginCount.toString(), PreferenceType.NUMBER))
                    add(PreferenceItem("last_login_timestamp", userSettings.lastLoginTimestamp.toString(), PreferenceType.NUMBER))
                    add(PreferenceItem("account_creation_timestamp", userSettings.accountCreationTimestamp.toString(), PreferenceType.NUMBER))
                    add(PreferenceItem("app_version", userSettings.appVersion, PreferenceType.STRING))
                    add(PreferenceItem("build_number", userSettings.buildNumber.toString(), PreferenceType.NUMBER))
                }.sortedBy { it.key }
            }
    }

    suspend fun updateUsername(username: String) {
        userSettingsDataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setUsername(username)
                .build()
        }
    }

    suspend fun updateIsPremium(isPremium: Boolean) {
        userSettingsDataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setIsPremium(isPremium)
                .build()
        }
    }

    suspend fun updatePreferredLanguage(language: String) {
        userSettingsDataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setPreferredLanguage(language)
                .build()
        }
    }

    suspend fun updateEmailNotifications(enabled: Boolean) {
        userSettingsDataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setEmailNotifications(enabled)
                .build()
        }
    }

    suspend fun updatePushNotifications(enabled: Boolean) {
        userSettingsDataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setPushNotifications(enabled)
                .build()
        }
    }

    suspend fun updateThemePreference(theme: String) {
        userSettingsDataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setThemePreference(theme)
                .build()
        }
    }

    suspend fun updateFontSizeMultiplier(multiplier: Float) {
        userSettingsDataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setFontSizeMultiplier(multiplier)
                .build()
        }
    }

    suspend fun updateAnalyticsEnabled(enabled: Boolean) {
        userSettingsDataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setAnalyticsEnabled(enabled)
                .build()
        }
    }

    suspend fun updateAutoSync(enabled: Boolean) {
        userSettingsDataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setAutoSync(enabled)
                .build()
        }
    }

    suspend fun clearAll() {
        userSettingsDataStore.updateData {
            UserSettings.getDefaultInstance()
        }
    }

    suspend fun generateSampleProtoPreferences() {
        userSettingsDataStore.updateData {
            UserSettings.newBuilder()
                .setUsername("proto_user_${(100..999).random()}")
                .setIsPremium(listOf(true, false).random())
                .setPreferredLanguage(listOf("en", "es", "fr", "de", "pt").random())
                .setEmailNotifications(listOf(true, false).random())
                .setPushNotifications(listOf(true, false).random())
                .setMarketingNotifications(listOf(true, false).random())
                .setThemePreference(listOf("light", "dark", "auto").random())
                .setFontSizeMultiplier(Random.nextDouble(0.8, 2.0).toFloat())
                .setDarkModeEnabled(listOf(true, false).random())
                .setAnalyticsEnabled(listOf(true, false).random())
                .setCrashReportingEnabled(listOf(true, false).random())
                .setLocationSharing(listOf(true, false).random())
                .setAutoSync(listOf(true, false).random())
                .setSyncIntervalMinutes(listOf(15, 30, 60, 120).random())
                .setWifiOnlySync(listOf(true, false).random())
                .setMaxCacheSizeMb((50..500).random())
                .setCacheExpiryHours((1..72).random().toLong())
                .setSessionTimeoutMinutes(listOf(5, 10, 15, 30, 60).random())
                .setRememberLogin(listOf(true, false).random())
                .setBetaFeaturesEnabled(listOf(true, false).random())
                .setDeveloperMode(listOf(true, false).random())
                .setLoginCount((1..100).random())
                .setLastLoginTimestamp(System.currentTimeMillis())
                .setAccountCreationTimestamp(System.currentTimeMillis() - (86400000L * (1..365).random()))
                .setAppVersion("1.${(0..9).random()}.${(0..9).random()}")
                .setBuildNumber((100..999).random())
                .build()
        }
    }

    suspend fun isEmpty(): Boolean {
        return try {
            val settings = userSettingsDataStore.data.map { it }.catch { 
                emit(UserSettings.getDefaultInstance()) 
            }.map { settings ->
                settings.username.isEmpty() && 
                settings.preferredLanguage.isEmpty() &&
                settings.themePreference.isEmpty() &&
                settings.appVersion.isEmpty()
            }
            settings.first()
        } catch (e: Exception) {
            true
        }
    }
}