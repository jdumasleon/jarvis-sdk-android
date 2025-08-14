package com.jarvis.features.inspector.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import com.jarvis.core.designsystem.component.DSCard
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jarvis.features.inspector.domain.entity.NetworkRequest
import com.jarvis.features.inspector.domain.entity.NetworkResponse
import com.jarvis.features.inspector.domain.entity.NetworkTransaction
import com.jarvis.features.inspector.presentation.ui.transactionsDetails.NetworkTransactionDetailUiData
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
            .padding(horizontal = DSJarvisTheme.spacing.m)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.l)
    ) {
        Spacer(modifier = Modifier.height(DSJarvisTheme.dimensions.s))

        // General Information
        InfoCard(title = "General") {
            InfoRow("URL:", transaction.request.url)
            HorizontalDivider(
                modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                color = DSJarvisTheme.colors.neutral.neutral0
            )
            InfoRow("Method:", transaction.request.method.name)
            HorizontalDivider(
                modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                color = DSJarvisTheme.colors.neutral.neutral0
            )
            InfoRow("Protocol:", transaction.request.protocol)
            HorizontalDivider(
                modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                color = DSJarvisTheme.colors.neutral.neutral0
            )
            InfoRow("Status:", transaction.status.name)
            transaction.response?.let { response ->
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                    color = DSJarvisTheme.colors.neutral.neutral0
                )
                InfoRow("Status Code:", "${response.statusCode} ${response.statusMessage}")
            }
            transaction.duration?.let { duration ->
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                    color = DSJarvisTheme.colors.neutral.neutral0
                )
                InfoRow("Duration:", "${duration}ms")
            }
        }

        // Timing Information
        InfoCard(title = "Timing") {
            InfoRow("Start Time:", formatTimestamp(transaction.startTime))
            transaction.endTime?.let { endTime ->
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                    color = DSJarvisTheme.colors.neutral.neutral0
                )
                InfoRow("End Time:", formatTimestamp(endTime))
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                color = DSJarvisTheme.colors.neutral.neutral0
            )
            InfoRow("Request tms:", formatTimestamp(transaction.request.timestamp))
            transaction.response?.let { response ->
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                    color = DSJarvisTheme.colors.neutral.neutral0
                )
                InfoRow("Response tms:", formatTimestamp(response.timestamp))
            }
        }

        // Size Information
        InfoCard(title = "Size") {
            InfoRow("Request Body Size:", "${transaction.request.bodySize} bytes")
            HorizontalDivider(
                modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                color = DSJarvisTheme.colors.neutral.neutral0
            )
            transaction.response?.let { response ->
                InfoRow("Response Body Size:", "${response.bodySize} bytes")
            }
        }

        // Error Information
        transaction.error?.let { error ->
            InfoCard(title = "Error") {
                DSText(
                    text = error,
                    style = DSJarvisTheme.typography.body.medium,
                    color = DSJarvisTheme.colors.error.error100
                )
            }
        }

        Spacer(modifier = Modifier.height(DSJarvisTheme.dimensions.m))
    }
}

@Composable
fun TransactionRequestTab(
    request: NetworkRequest,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = DSJarvisTheme.spacing.m),
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.l)
    ) {
        item { Spacer(modifier = Modifier.height(DSJarvisTheme.dimensions.s)) }

        item {
            InfoCard(title = "Request Line") {
                InfoRow("Method:", request.method.name)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                    color = DSJarvisTheme.colors.neutral.neutral0
                )
                InfoRow("Path:", request.path)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                    color = DSJarvisTheme.colors.neutral.neutral0
                )
                InfoRow("Protocol:", request.protocol)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                    color = DSJarvisTheme.colors.neutral.neutral0
                )
                InfoRow("Host:", request.host)
                request.contentType?.let {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                        color = DSJarvisTheme.colors.neutral.neutral0
                    )
                    InfoRow("Content-Type", it)
                }
            }
        }
        if (request.headers.isNotEmpty()) {
            item {
                InfoCard(title = "Headers") {
                    request.headers.onEachIndexed { index, (k, v) ->
                        InfoRow("$k: ", v)
                        if (index < request.headers.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                                color = DSJarvisTheme.colors.neutral.neutral0
                            )
                        }
                    }
                }
            }
        }
        val url = request.url
        if (url.contains("?")) {
            item {
                InfoCard(title = "Query Parameters") {
                    val queryString = url.substringAfter("?")
                    queryString.split("&").onEachIndexed { index, param ->
                        val parts = param.split("=", limit = 2)
                        if (parts.size == 2) {
                            InfoRow(parts[0], parts[1])
                            if (index < request.headers.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                                    color = DSJarvisTheme.colors.neutral.neutral0
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            EnhancedBodyViewer(
                title = "Request Body",
                body = if (request.hasBody) request.body else null,
                contentType = request.contentType
            )
        }

        item { Spacer(modifier = Modifier.height(DSJarvisTheme.dimensions.m)) }
    }
}

@Composable
fun TransactionResponseTab(
    response: NetworkResponse?,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = DSJarvisTheme.spacing.m),
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.l)
    ) {
        item { Spacer(modifier = Modifier.height(DSJarvisTheme.dimensions.s)) }

        if (response != null) {
            item {
                InfoCard(title = "Status Line") {
                    InfoRow("Status code", "${response.statusCode}")
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                        color = DSJarvisTheme.colors.neutral.neutral0
                    )
                    InfoRow("Status message", response.statusMessage)
                    response.contentType?.let {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                            color = DSJarvisTheme.colors.neutral.neutral0
                        )
                        InfoRow("Content-Type: ", it)
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                        color = DSJarvisTheme.colors.neutral.neutral0
                    )
                    InfoRow("Body Size: ", "${response.bodySize} bytes")
                }
            }
            if (response.headers.isNotEmpty()) {
                item {
                    InfoCard(title = "Headers") {
                        response.headers.onEachIndexed { index, (k, v) ->
                            InfoRow("$k: ", v)
                            if (index < response.headers.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                                    color = DSJarvisTheme.colors.neutral.neutral0
                                )
                            }
                        }
                    }
                }
            }
            item {
                InfoCard(title = "Response Info") {
                    InfoRow("Success: ", if (response.isSuccessful) "Yes" else "No")
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                        color = DSJarvisTheme.colors.neutral.neutral0
                    )
                    InfoRow("Content Type:", response.contentType ?: "Unknown")
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                        color = DSJarvisTheme.colors.neutral.neutral0
                    )
                    InfoRow("Is JSON: ", if (response.isJson) "Yes" else "No")
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                        color = DSJarvisTheme.colors.neutral.neutral0
                    )
                    InfoRow("Is XML: ", if (response.isXml) "Yes" else "No")
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                        color = DSJarvisTheme.colors.neutral.neutral0
                    )
                    InfoRow("Is Image: ", if (response.isImage) "Yes" else "No")
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                        color = DSJarvisTheme.colors.neutral.neutral0
                    )
                }
            }
            item {
                EnhancedBodyViewer(
                    title = "Response Body",
                    body = if (response.hasBody) response.body else null,
                    contentType = response.contentType
                )
            }
        } else {
            item {
                InfoCard(title = "Response") {
                    DSText(
                        text = "No response received",
                        style = DSJarvisTheme.typography.body.medium,
                        color = DSJarvisTheme.colors.neutral.neutral60
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(DSJarvisTheme.dimensions.m)) }
    }
}

@Composable
private fun InfoCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        DSText(
            text = title,
            style = DSJarvisTheme.typography.body.medium,
            fontWeight = FontWeight.Thin,
            color = DSJarvisTheme.colors.neutral.neutral100,
            modifier = Modifier.padding(start = DSJarvisTheme.spacing.m, bottom = DSJarvisTheme.spacing.s)
        )

        DSCard(
            modifier = Modifier.fillMaxWidth(),
            shape = DSJarvisTheme.shapes.s,
            elevation = DSJarvisTheme.elevations.level1
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DSJarvisTheme.spacing.s)
            ) {
                content()
            }
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
        DSText(
            text = label,
            style = DSJarvisTheme.typography.body.medium,
            fontWeight = FontWeight.Medium,

        )
        DSText(
            text = value,
            style = DSJarvisTheme.typography.body.medium,
            color = DSJarvisTheme.colors.neutral.neutral100
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

// =====================
// Previews
// =====================

@Preview(showBackground = true, name = "Overview – OK")
@Composable
private fun TransactionOverviewTabPreview() {
    DSJarvisTheme {
        TransactionOverviewTab(
            transaction = NetworkTransactionDetailUiData
                .mockNetworkTransactionDetailUiData.transaction
        )
    }
}

@Preview(showBackground = true, name = "Request – GET con query")
@Composable
private fun TransactionRequestTabPreview() {
    DSJarvisTheme {
        TransactionRequestTab(
            request = NetworkTransactionDetailUiData
                .mockNetworkTransactionDetailUiData.transaction.request
        )
    }
}

@Preview(showBackground = true, name = "Response – 200 OK")
@Composable
private fun TransactionResponseTabPreview() {
    DSJarvisTheme {
        TransactionResponseTab(
            response = NetworkTransactionDetailUiData
                .mockNetworkTransactionDetailUiData.transaction.response
        )
    }
}

@Preview(showBackground = true, name = "Response – Error 401")
@Composable
private fun TransactionResponseTabErrorPreview() {
    DSJarvisTheme {
        TransactionResponseTab(
            response = NetworkTransactionDetailUiData
                .mockErrorTransactionDetailUiData.transaction.response
        )
    }
}

@Preview(showBackground = true, name = "Response – Sin respuesta")
@Composable
private fun TransactionResponseTabNoResponsePreview() {
    DSJarvisTheme {
        TransactionResponseTab(
            response = null
        )
    }
}