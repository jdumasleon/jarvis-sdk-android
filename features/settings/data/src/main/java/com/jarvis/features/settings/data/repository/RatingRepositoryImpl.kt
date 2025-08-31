package com.jarvis.features.settings.data.repository

import com.jarvis.core.data.helpers.requestFlow
import com.jarvis.features.settings.data.remote.RatingApiService
import com.jarvis.features.settings.data.remote.dto.toDomain
import com.jarvis.features.settings.data.remote.dto.toDto
import com.jarvis.features.settings.domain.entity.Rating
import com.jarvis.features.settings.domain.entity.RatingSubmissionResult
import com.jarvis.features.settings.domain.repository.RatingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of RatingRepository
 */
@Singleton
class RatingRepositoryImpl @Inject constructor(
    private val ratingApiService: RatingApiService,
) : RatingRepository {

    override suspend fun submitRating(rating: Rating): Flow<Result<RatingSubmissionResult>> =
        requestFlow {
            ratingApiService.submitRating(rating.toDto()).toDomain()
        }
}