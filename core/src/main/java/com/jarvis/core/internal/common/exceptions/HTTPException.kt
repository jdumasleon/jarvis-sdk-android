@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)

package com.jarvis.core.internal.common.exceptions

import androidx.annotation.RestrictTo

class HTTPException(
    override val message: String,
    override val cause: Throwable,
    val statusCode: Int
) : RuntimeException("HTTP $statusCode $message", cause)

sealed class HttpStatusCodes(val code: Int) {

    // Successful Responses (200–299)
    data object OK : HttpStatusCodes(OK_CODE)
    data object Created : HttpStatusCodes(CREATED_CODE)
    data object Accepted : HttpStatusCodes(ACCEPTED_CODE)
    data object NoContent : HttpStatusCodes(NO_CONTENT_CODE)

    // Client Error Responses (400–499)
    data object BadRequest : HttpStatusCodes(BAD_REQUEST_CODE)
    data object Unauthorized : HttpStatusCodes(UNAUTHORIZED_CODE)
    data object Forbidden : HttpStatusCodes(FORBIDDEN_CODE)
    data object NotFound : HttpStatusCodes(NOT_FOUND_CODE)
    data object Conflict : HttpStatusCodes(CONFLICT_CODE)

    // Server Error Responses (500–599)
    data object InternalServerError : HttpStatusCodes(INTERNAL_SERVER_ERROR_CODE)
    data object BadGateway : HttpStatusCodes(BAD_GATEWAY_CODE)
    data object ServiceUnavailable : HttpStatusCodes(SERVICE_UNAVAILABLE_CODE)
    data object GatewayTimeout : HttpStatusCodes(GATEWAY_TIMEOUT_CODE)

    companion object {
        const val OK_CODE = 200
        const val CREATED_CODE = 201
        const val ACCEPTED_CODE = 202
        const val NO_CONTENT_CODE = 204
        const val BAD_REQUEST_CODE = 400
        const val UNAUTHORIZED_CODE = 401
        const val FORBIDDEN_CODE = 403
        const val NOT_FOUND_CODE = 404
        const val CONFLICT_CODE = 409
        const val INTERNAL_SERVER_ERROR_CODE = 500
        const val BAD_GATEWAY_CODE = 502
        const val SERVICE_UNAVAILABLE_CODE = 503
        const val GATEWAY_TIMEOUT_CODE = 504
    }
}
