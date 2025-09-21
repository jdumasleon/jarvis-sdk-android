package com.jarvis.core.common.errorHandling

import com.jarvis.core.common.exceptions.HTTPException
import com.jarvis.core.common.exceptions.HttpStatusCodes
import com.jarvis.core.common.exceptions.NoInternetException
import com.jarvis.core.common.exceptions.UnknownErrorException

open class ErrorType {
    data object NoData : ErrorType()
    data object NoInternetConnection : ErrorType()
    data object UnknownError : ErrorType()
    data object PrintError : ErrorType()
    data object ExportEventsError : ErrorType()
    data object ServerError : ErrorType()
    data object ClientError : ErrorType()
    data object Forbidden : ErrorType()
    data object NotFound : ErrorType()
    data object Conflict : ErrorType()
    data object Unauthorized : ErrorType()
    data object InvalidDataFormat : ErrorType()
    data class GenericError(val message: Int) : ErrorType()
}

fun handleErrorType(throwable: Throwable): ErrorType {
    return when (throwable) {
        is HTTPException -> {
            when (throwable.statusCode) {
                HttpStatusCodes.INTERNAL_SERVER_ERROR_CODE -> ErrorType.ServerError
                HttpStatusCodes.BAD_REQUEST_CODE -> ErrorType.ClientError
                HttpStatusCodes.FORBIDDEN_CODE -> ErrorType.Forbidden
                HttpStatusCodes.NOT_FOUND_CODE -> ErrorType.NotFound
                HttpStatusCodes.CONFLICT_CODE -> ErrorType.Conflict
                HttpStatusCodes.UNAUTHORIZED_CODE -> ErrorType.Conflict
                else -> ErrorType.UnknownError
            }
        }

        is NoInternetException -> ErrorType.NoInternetConnection
        is UnknownErrorException -> ErrorType.UnknownError
        else -> ErrorType.UnknownError
    }
}