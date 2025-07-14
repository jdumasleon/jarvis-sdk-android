package com.jarvis.core.domain.usecase

import com.jarvis.core.domain.repository.PreferencesRepository
import javax.inject.Inject

class SetIsForcingFailRequestsUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(isForcingFail: Boolean) {
        preferencesRepository.setIsForcingFailRequests(isForcingFail)
    }
}