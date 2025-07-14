package com.jarvis.core.data.source.local

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.map
import java.util.prefs.Preferences
import javax.inject.Inject

class PreferencesDataSourceImpl @Inject constructor(
    private val preferences: DataStore<Preferences>,
) : PreferencesDataSource {

    val userData = preferences.data
        .map {
            PreferencesData(
                bookmarkedNewsResources = it.bookmarkedNewsResourceIdsMap.keys,
                viewedNewsResources = it.viewedNewsResourceIdsMap.keys,
                followedTopics = it.followedTopicIdsMap.keys,
                themeBrand = when (it.themeBrand) {
                    null,
                    ThemeBrandProto.THEME_BRAND_UNSPECIFIED,
                    ThemeBrandProto.UNRECOGNIZED,
                    ThemeBrandProto.THEME_BRAND_DEFAULT,
                    -> ThemeBrand.DEFAULT
                    ThemeBrandProto.THEME_BRAND_ANDROID -> ThemeBrand.ANDROID
                },
                darkThemeConfig = when (it.darkThemeConfig) {
                    null,
                    DarkThemeConfigProto.DARK_THEME_CONFIG_UNSPECIFIED,
                    DarkThemeConfigProto.UNRECOGNIZED,
                    DarkThemeConfigProto.DARK_THEME_CONFIG_FOLLOW_SYSTEM,
                    ->
                        DarkThemeConfig.FOLLOW_SYSTEM
                    DarkThemeConfigProto.DARK_THEME_CONFIG_LIGHT ->
                        DarkThemeConfig.LIGHT
                    DarkThemeConfigProto.DARK_THEME_CONFIG_DARK -> DarkThemeConfig.DARK
                },
                useDynamicColor = it.useDynamicColor,
                shouldHideOnboarding = it.shouldHideOnboarding,
            )
        }

    override suspend fun setIsForcingFailRequests(isForcingFail: Boolean) {
        TODO("Not yet implemented")
    }

    override suspend fun setEnvironmentsList(environmentsList: List<Pair<String, String>>) {
        TODO("Not yet implemented")
    }

    override suspend fun setFirstEnvironment(firstEnvironment: Pair<String, String>) {
        TODO("Not yet implemented")
    }

}
