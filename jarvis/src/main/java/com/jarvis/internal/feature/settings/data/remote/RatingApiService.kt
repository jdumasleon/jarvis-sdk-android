package com.jarvis.internal.feature.settings.data.remote

import com.jarvis.internal.feature.settings.data.remote.dto.RatingDto
import com.jarvis.internal.feature.settings.data.remote.dto.RatingSubmissionResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * API service for rating operations
 */
interface RatingApiService {
    @POST("ratings")
    suspend fun submitRating(@Body rating: RatingDto): RatingSubmissionResponseDto
}