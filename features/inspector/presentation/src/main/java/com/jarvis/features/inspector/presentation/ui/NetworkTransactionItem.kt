package com.jarvis.features.inspector.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jarvis.features.inspector.domain.entity.NetworkTransaction
import com.jarvis.features.inspector.domain.entity.StatusCategory
import com.jarvis.features.inspector.domain.entity.TransactionStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NetworkTransactionItem(
    transaction: NetworkTransaction,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Method and URL
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = transaction.request.method.name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = getMethodColor(transaction.request.method.name)
                    )
                    Text(
                        text = transaction.request.url,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Status and timing
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    // Status code
                    transaction.response?.let { response ->
                        Text(
                            text = response.statusCode.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = getStatusColor(response.statusCategory),
                            fontWeight = FontWeight.Bold
                        )
                    } ?: run {
                        Text(
                            text = transaction.status.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = when (transaction.status) {
                                TransactionStatus.PENDING -> Color.Gray
                                TransactionStatus.FAILED -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }

                    // Duration
                    transaction.duration?.let { duration ->
                        Text(
                            text = "${duration}ms",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Timestamp
                    Text(
                        text = formatTimestamp(transaction.startTime),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Transaction",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun getMethodColor(method: String): Color {
    return when (method) {
        "GET" -> Color(0xFF4CAF50)
        "POST" -> Color(0xFF2196F3)
        "PUT" -> Color(0xFFFF9800)
        "DELETE" -> Color(0xFFF44336)
        "PATCH" -> Color(0xFF9C27B0)
        else -> MaterialTheme.colorScheme.onSurface
    }
}

@Composable
private fun getStatusColor(category: StatusCategory): Color {
    return when (category) {
        StatusCategory.SUCCESS -> Color(0xFF4CAF50)
        StatusCategory.REDIRECT -> Color(0xFFFF9800)
        StatusCategory.CLIENT_ERROR -> Color(0xFFF44336)
        StatusCategory.SERVER_ERROR -> Color(0xFF9C27B0)
        StatusCategory.INFORMATIONAL -> Color(0xFF2196F3)
        else -> MaterialTheme.colorScheme.onSurface
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(timestamp))
}