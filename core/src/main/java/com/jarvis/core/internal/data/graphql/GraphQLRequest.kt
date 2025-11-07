@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.core.internal.data.graphql

import androidx.annotation.RestrictTo
import com.google.gson.annotations.SerializedName

/**
 * Generic GraphQL request wrapper
 */
data class GraphQLRequest<T>(
    @SerializedName("query")
    val query: String,
    @SerializedName("variables")
    val variables: T
)
