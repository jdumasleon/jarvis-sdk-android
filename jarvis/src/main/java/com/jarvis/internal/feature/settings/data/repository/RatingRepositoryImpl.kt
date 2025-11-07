package com.jarvis.internal.feature.settings.data.repository

import androidx.annotation.RestrictTo

import com.jarvis.core.internal.data.graphql.GraphQLRequest
import com.jarvis.core.internal.data.helpers.requestFlow
import com.jarvis.internal.feature.settings.data.remote.RatingApiService
import com.jarvis.internal.feature.settings.data.remote.dto.RatingMutationVariables
import com.jarvis.internal.feature.settings.data.remote.dto.RatingMutations
import com.jarvis.internal.feature.settings.data.remote.dto.toDomain
import com.jarvis.internal.feature.settings.data.remote.dto.toGraphQLInput
import com.jarvis.internal.feature.settings.domain.entity.Rating
import com.jarvis.internal.feature.settings.domain.entity.RatingSubmissionResult
import com.jarvis.internal.feature.settings.domain.repository.RatingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of RatingRepository using GraphQL
 */
@Singleton
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class RatingRepositoryImpl @Inject constructor(
    private val ratingApiService: RatingApiService,
) : RatingRepository {

    override suspend fun submitRating(rating: Rating): Flow<Result<RatingSubmissionResult>> =
        requestFlow {
            try {
                // Convert rating to GraphQL input (SDK version will be extracted from rating.version)
                val input = rating.toGraphQLInput()

                // Build GraphQL request
                val graphQLRequest = GraphQLRequest(
                    query = RatingMutations.SUBMIT_RATING,
                    variables = RatingMutationVariables(data = input)
                )

                // Execute GraphQL mutation
                val response = ratingApiService.submitRating(graphQLRequest)

                // Handle GraphQL errors
                val errors = response.errors
                if (errors != null && errors.isNotEmpty()) {
                    val errorMessage = errors.joinToString(", ") { it.message }
                    android.util.Log.e("RatingRepository", "GraphQL Errors: $errorMessage")
                    throw Exception("GraphQL Error: $errorMessage")
                }

                // Extract and convert response
                response.data?.submitRating?.toDomain()
                    ?: throw Exception("Empty response from rating API")
            } catch (e: Exception) {
                android.util.Log.e("RatingRepository", "Error submitting rating", e)
                throw e
            }
        }
}