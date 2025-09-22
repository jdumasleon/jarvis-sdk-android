package com.jarvis.features.inspector.internal.data.local.entity

import androidx.annotation.RestrictTo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "network_transactions")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class NetworkTransactionEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "url")
    val url: String,
    
    @ColumnInfo(name = "method")
    val method: String,
    
    @ColumnInfo(name = "request_headers")
    val requestHeaders: String, // JSON serialized
    
    @ColumnInfo(name = "request_body")
    val requestBody: String?,
    
    @ColumnInfo(name = "request_content_type")
    val requestContentType: String?,
    
    @ColumnInfo(name = "request_body_size")
    val requestBodySize: Long,
    
    @ColumnInfo(name = "response_status_code")
    val responseStatusCode: Int?,
    
    @ColumnInfo(name = "response_status_message")
    val responseStatusMessage: String?,
    
    @ColumnInfo(name = "response_headers")
    val responseHeaders: String?, // JSON serialized
    
    @ColumnInfo(name = "response_body")
    val responseBody: String?,
    
    @ColumnInfo(name = "response_content_type")
    val responseContentType: String?,
    
    @ColumnInfo(name = "response_body_size")
    val responseBodySize: Long?,
    
    @ColumnInfo(name = "start_time")
    val startTime: Long,
    
    @ColumnInfo(name = "end_time")
    val endTime: Long?,
    
    @ColumnInfo(name = "status")
    val status: String,
    
    @ColumnInfo(name = "error")
    val error: String?,
    
    @ColumnInfo(name = "request_timestamp")
    val requestTimestamp: Long,
    
    @ColumnInfo(name = "response_timestamp")
    val responseTimestamp: Long?
)