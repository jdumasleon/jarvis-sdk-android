package com.jarvis.internal.feature.settings.domain.usecase

import androidx.annotation.RestrictTo

import com.jarvis.internal.feature.settings.domain.entity.Rating
import com.jarvis.internal.feature.settings.domain.entity.RatingSubmissionResult
import com.jarvis.internal.feature.settings.domain.repository.RatingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

/**
 * Use case for submitting SDK rating
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class SubmitRatingUseCase @Inject constructor(
    private val ratingRepository: RatingRepository
) {
    operator fun invoke(rating: Rating): Flow<Result<RatingSubmissionResult>> = channelFlow {
        ratingRepository.submitRating(rating).collectLatest { ratingResult ->
            ratingResult.fold(
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