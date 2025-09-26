@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.features.inspector.internal.presentation.components

import androidx.annotation.RestrictTo

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
import com.jarvis.core.internal.designsystem.component.DSCard
import com.jarvis.core.internal.designsystem.component.DSText
import com.jarvis.core.internal.designsystem.theme.DSJarvisTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jarvis.features.inspector.internal.domain.entity.NetworkRequest
import com.jarvis.features.inspector.internal.domain.entity.NetworkResponse
import com.jarvis.features.inspector.internal.domain.entity.NetworkTransaction
import com.jarvis.features.inspector.R
import com.jarvis.features.inspector.internal.presentation.transactionsDetails.NetworkTransactionDetailUiData
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
        InfoCard(title = stringResource(R.string.features_inspector_presentation_general)) {
            InfoRow(stringResource(R.string.features_inspector_presentation_url), transaction.request.url)
            HorizontalDivider(
                modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                color = DSJarvisTheme.colors.neutral.neutral0
            )
            InfoRow(stringResource(R.string.features_inspector_presentation_method), transaction.request.method.name)
            HorizontalDivider(
                modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                color = DSJarvisTheme.colors.neutral.neutral0
            )
            InfoRow(stringResource(R.string.features_inspector_presentation_protocol), transaction.request.protocol)
            HorizontalDivider(
                modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                color = DSJarvisTheme.colors.neutral.neutral0
            )
            InfoRow(stringResource(R.string.features_inspector_detail_presentation_status), transaction.status.name)
            transaction.response?.let { response ->
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                    color = DSJarvisTheme.colors.neutral.neutral0
                )
                InfoRow(stringResource(R.string.features_inspector_presentation_status_code), "${response.statusCode} ${response.statusMessage}")
            }
            transaction.duration?.let { duration ->
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                    color = DSJarvisTheme.colors.neutral.neutral0
                )
                InfoRow(stringResource(R.string.features_inspector_presentation_duration), "${duration}ms")
            }
        }

        // Timing Information
        InfoCard(title = stringResource(R.string.features_inspector_presentation_timing)) {
            InfoRow(stringResource(R.string.features_inspector_presentation_start_time), formatTimestamp(transaction.startTime))
            transaction.endTime?.let { endTime ->
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                    color = DSJarvisTheme.colors.neutral.neutral0
                )
                InfoRow(stringResource(R.string.features_inspector_presentation_end_time), formatTimestamp(endTime))
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                color = DSJarvisTheme.colors.neutral.neutral0
            )
            InfoRow(stringResource(R.string.features_inspector_presentation_request_tms), formatTimestamp(transaction.request.timestamp))
            transaction.response?.let { response ->
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                    color = DSJarvisTheme.colors.neutral.neutral0
                )
                InfoRow(stringResource(R.string.features_inspector_presentation_response_tms), formatTimestamp(response.timestamp))
            }
        }

        // Size Information
        InfoCard(title = stringResource(R.string.features_inspector_presentation_size)) {
            InfoRow(stringResource(R.string.features_inspector_presentation_request_body_size), "${transaction.request.bodySize} bytes")
            HorizontalDivider(
                modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                color = DSJarvisTheme.colors.neutral.neutral0
            )
            transaction.response?.let { response ->
                InfoRow(stringResource(R.string.features_inspector_presentation_response_body_size), "${response.bodySize} bytes")
            }
        }

        // Error Information
        transaction.error?.let { error ->
            InfoCard(title = stringResource(R.string.features_inspector_presentation_error)) {
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
            InfoCard(title = stringResource(R.string.features_inspector_presentation_request_line)) {
                InfoRow(stringResource(R.string.features_inspector_presentation_method), request.method.name)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                    color = DSJarvisTheme.colors.neutral.neutral0
                )
                InfoRow(stringResource(R.string.features_inspector_presentation_path), request.path)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                    color = DSJarvisTheme.colors.neutral.neutral0
                )
                InfoRow(stringResource(R.string.features_inspector_presentation_protocol), request.protocol)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                    color = DSJarvisTheme.colors.neutral.neutral0
                )
                InfoRow(stringResource(R.string.features_inspector_presentation_host), request.host)
                request.contentType?.let {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                        color = DSJarvisTheme.colors.neutral.neutral0
                    )
                    InfoRow(stringResource(R.string.features_inspector_presentation_content_type), it)
                }
            }
        }
        if (request.headers.isNotEmpty()) {
            item {
                InfoCard(title = stringResource(R.string.features_inspector_presentation_headers)) {
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
                InfoCard(title = stringResource(R.string.features_inspector_presentation_query_parameters).uppercase()) {
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
                title = stringResource(R.string.features_inspector_presentation_request_body).uppercase(),
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
                InfoCard(title = stringResource(R.string.features_inspector_presentation_status_line)) {
                    InfoRow(stringResource(R.string.features_inspector_presentation_status_code), "${response.statusCode}")
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                        color = DSJarvisTheme.colors.neutral.neutral0
                    )
                    InfoRow(stringResource(R.string.features_inspector_presentation_status_message), response.statusMessage)
                    response.contentType?.let {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                            color = DSJarvisTheme.colors.neutral.neutral0
                        )
                        InfoRow(stringResource(R.string.features_inspector_presentation_content_type), it)
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                        color = DSJarvisTheme.colors.neutral.neutral0
                    )
                    InfoRow(stringResource(R.string.features_inspector_presentation_body_size), "${response.bodySize} bytes")
                }
            }
            if (response.headers.isNotEmpty()) {
                item {
                    InfoCard(title = stringResource(R.string.features_inspector_presentation_headers)) {
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
                InfoCard(title = stringResource(R.string.features_inspector_presentation_response_info)) {
                    InfoRow(stringResource(R.string.features_inspector_presentation_success), if (response.isSuccessful) "Yes" else "No")
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                        color = DSJarvisTheme.colors.neutral.neutral0
                    )
                    InfoRow(stringResource(R.string.features_inspector_presentation_content_type), response.contentType ?: "Unknown")
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                        color = DSJarvisTheme.colors.neutral.neutral0
                    )
                    InfoRow(stringResource(R.string.features_inspector_presentation_is_json), if (response.isJson) "Yes" else "No")
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                        color = DSJarvisTheme.colors.neutral.neutral0
                    )
                    InfoRow(stringResource(R.string.features_inspector_presentation_is_xml), if (response.isXml) "Yes" else "No")
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                        color = DSJarvisTheme.colors.neutral.neutral0
                    )
                    InfoRow(stringResource(R.string.features_inspector_presentation_is_image), if (response.isImage) "Yes" else "No")
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                        color = DSJarvisTheme.colors.neutral.neutral0
                    )
                }
            }
            item {
                EnhancedBodyViewer(
                    title = stringResource(R.string.features_inspector_presentation_response_body).uppercase(),
                    body = if (response.hasBody) response.body else null,
                    contentType = response.contentType
                )
            }
        } else {
            item {
                InfoCard(title = stringResource(R.string.features_inspector_presentation_response)) {
                    DSText(
                        text = stringResource(R.string.features_inspector_presentation_no_response_received),
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
            text = title.uppercase(),
            style = DSJarvisTheme.typography.body.medium,
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
            transaction = NetworkTransactionDetailUiData.Companion
                .mockNetworkTransactionDetailUiData.transaction
        )
    }
}

@Preview(showBackground = true, name = "Request – GET con query")
@Composable
private fun TransactionRequestTabPreview() {
    DSJarvisTheme {
        TransactionRequestTab(
            request = NetworkTransactionDetailUiData.Companion
                .mockNetworkTransactionDetailUiData.transaction.request
        )
    }
}

@Preview(showBackground = true, name = "Response – 200 OK")
@Composable
private fun TransactionResponseTabPreview() {
    DSJarvisTheme {
        TransactionResponseTab(
            response = NetworkTransactionDetailUiData.Companion
                .mockNetworkTransactionDetailUiData.transaction.response
        )
    }
}

@Preview(showBackground = true, name = "Response – Error 401")
@Composable
private fun TransactionResponseTabErrorPreview() {
    DSJarvisTheme {
        TransactionResponseTab(
            response = NetworkTransactionDetailUiData.Companion
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