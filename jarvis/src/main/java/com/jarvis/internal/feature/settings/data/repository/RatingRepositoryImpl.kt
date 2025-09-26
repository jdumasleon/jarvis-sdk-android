package com.jarvis.internal.feature.settings.data.repository

import androidx.annotation.RestrictTo

import com.jarvis.core.internal.data.helpers.requestFlow
import com.jarvis.internal.feature.settings.data.remote.RatingApiService
import com.jarvis.internal.feature.settings.data.remote.dto.toDomain
import com.jarvis.internal.feature.settings.data.remote.dto.toDto
import com.jarvis.internal.feature.settings.domain.entity.Rating
import com.jarvis.internal.feature.settings.domain.entity.RatingSubmissionResult
import com.jarvis.internal.feature.settings.domain.repository.RatingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of RatingRepository
 */
@Singleton
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class RatingRepositoryImpl @Inject constructor(
    private val ratingApiService: RatingApiService,
) : RatingRepository {

    override suspend fun submitRating(rating: Rating): Flow<Result<RatingSubmissionResult>> =
        requestFlow {
            ratingApiService.submitRating(rating.toDto()).toDomain()
        }
}