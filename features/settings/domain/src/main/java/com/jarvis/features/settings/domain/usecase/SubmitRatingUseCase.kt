package com.jarvis.features.settings.domain.usecase

import com.jarvis.features.settings.domain.entity.Rating
import com.jarvis.features.settings.domain.entity.RatingSubmissionResult
import com.jarvis.features.settings.domain.repository.RatingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

/**
 * Use case for submitting SDK rating
 */
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