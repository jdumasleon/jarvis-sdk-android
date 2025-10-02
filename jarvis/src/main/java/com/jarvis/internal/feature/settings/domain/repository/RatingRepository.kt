package com.jarvis.internal.feature.settings.domain.repository

import androidx.annotation.RestrictTo

import com.jarvis.internal.feature.settings.domain.entity.Rating
import com.jarvis.internal.feature.settings.domain.entity.RatingSubmissionResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for rating operations
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface RatingRepository {
    /**
     * Submit a rating for the SDK
     * @param rating The rating to submit
     * @return Result containing submission result
     */
    suspend fun submitRating(rating: Rating): Flow<Result<RatingSubmissionResult>>
}