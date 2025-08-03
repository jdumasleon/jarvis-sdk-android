package com.jarvis.features.inspector.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jarvis.features.inspector.domain.entity.NetworkRequest
import com.jarvis.features.inspector.domain.entity.NetworkResponse
import com.jarvis.features.inspector.domain.entity.NetworkTransaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionOverviewTab(
    transaction: NetworkTransaction,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // General Information
        InfoCard(title = "General") {
            InfoRow("URL", transaction.request.url)
            InfoRow("Method", transaction.request.method.name)
            InfoRow("Protocol", transaction.request.protocol)
            InfoRow("Status", transaction.status.name)
            transaction.response?.let { response ->
                InfoRow("Status Code", "${response.statusCode} ${response.statusMessage}")
            }
            transaction.duration?.let { duration ->
                InfoRow("Duration", "${duration}ms")
            }
        }

        // Timing Information
        InfoCard(title = "Timing") {
            InfoRow("Start Time", formatTimestamp(transaction.startTime))
            transaction.endTime?.let { endTime ->
                InfoRow("End Time", formatTimestamp(endTime))
            }
            InfoRow("Request Timestamp", formatTimestamp(transaction.request.timestamp))
            transaction.response?.let { response ->
                InfoRow("Response Timestamp", formatTimestamp(response.timestamp))
            }
        }

        // Size Information
        InfoCard(title = "Size") {
            InfoRow("Request Body Size", "${transaction.request.bodySize} bytes")
            transaction.response?.let { response ->
                InfoRow("Response Body Size", "${response.bodySize} bytes")
            }
        }

        // Error Information
        transaction.error?.let { error ->
            InfoCard(title = "Error") {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun TransactionRequestTab(
    request: NetworkRequest,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Request Line
        InfoCard(title = "Request Line") {
            Text(
                text = "${request.method.name} ${request.path}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            InfoRow("Host", request.host)
            request.contentType?.let { contentType ->
                InfoRow("Content-Type", contentType)
            }
        }

        // Headers
        if (request.headers.isNotEmpty()) {
            InfoCard(title = "Headers") {
                request.headers.forEach { (key, value) ->
                    InfoRow(key, value)
                }
            }
        }

        // Query Parameters
        val url = request.url
        if (url.contains("?")) {
            InfoCard(title = "Query Parameters") {
                val queryString = url.substringAfter("?")
                queryString.split("&").forEach { param ->
                    val parts = param.split("=", limit = 2)
                    if (parts.size == 2) {
                        InfoRow(parts[0], parts[1])
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionResponseTab(
    response: NetworkResponse?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (response != null) {
            // Status Line
            InfoCard(title = "Status Line") {
                Text(
                    text = "${response.statusCode} ${response.statusMessage}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                response.contentType?.let { contentType ->
                    InfoRow("Content-Type", contentType)
                }
                InfoRow("Body Size", "${response.bodySize} bytes")
            }

            // Headers
            if (response.headers.isNotEmpty()) {
                InfoCard(title = "Headers") {
                    response.headers.forEach { (key, value) ->
                        InfoRow(key, value)
                    }
                }
            }

            // Response Metadata
            InfoCard(title = "Response Info") {
                InfoRow("Success", if (response.isSuccessful) "Yes" else "No")
                InfoRow("Content Type", response.contentType ?: "Unknown")
                InfoRow("Is JSON", if (response.isJson) "Yes" else "No")
                InfoRow("Is XML", if (response.isXml) "Yes" else "No")
                InfoRow("Is Image", if (response.isImage) "Yes" else "No")
            }
        } else {
            InfoCard(title = "Response") {
                Text(
                    text = "No response received",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TransactionBodyTab(
    transaction: NetworkTransaction,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Request Body
        InfoCard(title = "Request Body") {
            if (transaction.request.hasBody) {
                Text(
                    text = transaction.request.body ?: "No body content",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = "No request body",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Response Body
        InfoCard(title = "Response Body") {
            transaction.response?.let { response ->
                if (response.hasBody) {
                    Text(
                        text = response.body ?: "No body content",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "No response body",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } ?: run {
                Text(
                    text = "No response received",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(2f)
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm:ss.SSS", Locale.getDefault())
    return formatter.format(Date(timestamp))
}