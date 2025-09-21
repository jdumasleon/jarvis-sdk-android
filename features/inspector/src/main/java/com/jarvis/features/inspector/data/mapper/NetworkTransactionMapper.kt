package com.jarvis.features.inspector.data.mapper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jarvis.features.inspector.data.local.entity.NetworkTransactionEntity
import com.jarvis.features.inspector.domain.entity.HttpMethod
import com.jarvis.features.inspector.domain.entity.NetworkRequest
import com.jarvis.features.inspector.domain.entity.NetworkResponse
import com.jarvis.features.inspector.domain.entity.NetworkTransaction
import com.jarvis.features.inspector.domain.entity.TransactionStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkTransactionMapper @Inject constructor(
    private val gson: Gson
) {
    
    private val mapType = object : TypeToken<Map<String, String>>() {}.type
    
    fun toEntity(transaction: NetworkTransaction): NetworkTransactionEntity {
        return NetworkTransactionEntity(
            id = transaction.id,
            url = transaction.request.url,
            method = transaction.request.method.name,
            requestHeaders = gson.toJson(transaction.request.headers),
            requestBody = transaction.request.body,
            requestContentType = transaction.request.contentType,
            requestBodySize = transaction.request.bodySize,
            responseStatusCode = transaction.response?.statusCode,
            responseStatusMessage = transaction.response?.statusMessage,
            responseHeaders = transaction.response?.headers?.let { gson.toJson(it) },
            responseBody = transaction.response?.body,
            responseContentType = transaction.response?.contentType,
            responseBodySize = transaction.response?.bodySize,
            startTime = transaction.startTime,
            endTime = transaction.endTime,
            status = transaction.status.name,
            error = transaction.error,
            requestTimestamp = transaction.request.timestamp,
            responseTimestamp = transaction.response?.timestamp
        )
    }
    
    fun toDomain(entity: NetworkTransactionEntity): NetworkTransaction {
        val request = NetworkRequest(
            url = entity.url,
            method = HttpMethod.fromString(entity.method),
            headers = parseHeaders(entity.requestHeaders),
            body = entity.requestBody,
            contentType = entity.requestContentType,
            bodySize = entity.requestBodySize,
            timestamp = entity.requestTimestamp
        )
        
        val response = if (entity.responseStatusCode != null && entity.responseStatusMessage != null) {
            NetworkResponse(
                statusCode = entity.responseStatusCode,
                statusMessage = entity.responseStatusMessage,
                headers = entity.responseHeaders?.let { parseHeaders(it) } ?: emptyMap(),
                body = entity.responseBody,
                contentType = entity.responseContentType,
                bodySize = entity.responseBodySize ?: 0L,
                timestamp = entity.responseTimestamp ?: System.currentTimeMillis()
            )
        } else null
        
        return NetworkTransaction(
            id = entity.id,
            request = request,
            response = response,
            startTime = entity.startTime,
            endTime = entity.endTime,
            status = TransactionStatus.valueOf(entity.status),
            error = entity.error
        )
    }
    
    private fun parseHeaders(headersJson: String): Map<String, String> {
        return try {
            gson.fromJson(headersJson, mapType) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
}