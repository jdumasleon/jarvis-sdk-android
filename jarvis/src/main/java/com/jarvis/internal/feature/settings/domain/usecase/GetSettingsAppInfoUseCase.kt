package com.jarvis.internal.feature.settings.domain.usecase

import com.jarvis.internal.feature.settings.domain.entity.SettingsAppInfo
import com.jarvis.internal.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

/**
 * Use case to get settings application information
 */
class GetSettingsAppInfoUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    operator fun invoke(): Flow<Result<SettingsAppInfo>> = channelFlow {
        settingsRepository.getSettingsAppInfo().collectLatest { settingsAppInfoResult ->
            settingsAppInfoResult.fold(
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