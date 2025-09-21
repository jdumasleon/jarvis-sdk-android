package com.jarvis.core.data.helpers

import com.jarvis.core.common.exceptions.HTTPException
import com.jarvis.core.common.exceptions.HttpStatusCodes
import com.jarvis.core.common.exceptions.NoInternetException
import com.jarvis.core.common.exceptions.NullResponseBodyException
import com.jarvis.core.common.exceptions.UnknownErrorException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import java.io.IOException

fun <T> requestFlow(
    call: suspend () -> T?
): Flow<Result<T>> = flow {
    try {
        val response = call.invoke()
        response?.let {
            emit(Result.success(it))
        } ?: emit(Result.failure(NullResponseBodyException()))

    } catch (e: Exception) {
        val errorResult: Result<T> = when (e) {
            is HttpException -> Result.failure(mapHttpError(e))
            is IOException -> Result.failure(NoInternetException("No internet connection"))
            else -> Result.failure(UnknownErrorException("An unexpected error occurred: ${e.message}"))
        }
        emit(errorResult)
    }
}.flowOn(Dispatchers.IO)

private fun mapHttpError(e: HttpException): HTTPException {
    return when (val statusCode = e.code()) {
        HttpStatusCodes.BadRequest.code -> HTTPException("Bad Request", e, statusCode)
        HttpStatusCodes.Unauthorized.code -> HTTPException("Unauthorized", e, statusCode)
        HttpStatusCodes.Forbidden.code -> HTTPException("Forbidden", e, statusCode)
        HttpStatusCodes.NotFound.code -> HTTPException("Not Found", e, statusCode)
        HttpStatusCodes.Conflict.code -> HTTPException("Conflict", e, statusCode)
        HttpStatusCodes.InternalServerError.code -> HTTPException("Internal Server Error", e, statusCode)
        HttpStatusCodes.BadGateway.code -> HTTPException("Bad Gateway", e, statusCode)
        HttpStatusCodes.ServiceUnavailable.code -> HTTPException("Service Unavailable", e, statusCode)
        HttpStatusCodes.GatewayTimeout.code -> HTTPException("Gateway Timeout", e, statusCode)
        HttpStatusCodes.OK.code,
        HttpStatusCodes.Created.code,
        HttpStatusCodes.Accepted.code,
        HttpStatusCodes.NoContent.code -> HTTPException("Unexpected success status", e, statusCode)
        else -> HTTPException("HTTP Error", e, statusCode)
    }
}