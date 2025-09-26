package com.jarvis.internal.feature.settings.domain.usecase

import androidx.annotation.RestrictTo

import com.jarvis.internal.feature.settings.domain.entity.AppInfo
import com.jarvis.internal.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

/**
 * Use case to get application information
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class GetAppInfoUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    
    operator fun invoke(): Flow<Result<AppInfo>> = channelFlow {
        settingsRepository.getAppInfo().collectLatest { appInfoResult ->
            appInfoResult.fold(
                onFailure = {
                    send(Result.failure(it))
                },
                onSuccess = {
                    send(Result.success(it))
                }
            )
        }
    }
}