package com.jarvis.features.settings.domain.usecase

import com.jarvis.features.settings.domain.entity.AppInfo
import com.jarvis.features.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

/**
 * Use case to get application information
 */
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