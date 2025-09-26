@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.features.inspector.internal.presentation.components

import androidx.annotation.RestrictTo

import com.jarvis.features.inspector.internal.domain.entity.NetworkTransaction
import java.text.SimpleDateFormat
import java.util.*

const val TODAY = "Today"
const val YESTERDAY = "Yesterday"

/**
 * Represents a group of network transactions by date
 */
data class NetworkTransactionGroup(
    val date: String,
    val timestamp: Long,
    val transactions: List<NetworkTransaction>
)

/**
 * Groups network transactions by date
 */
fun List<NetworkTransaction>.groupByDate(): List<NetworkTransactionGroup> {
    val dateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    val todayFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }
    
    val todayString = todayFormatter.format(today.time)
    val yesterdayString = todayFormatter.format(yesterday.time)
    
    return this
        .groupBy { transaction ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = transaction.startTime
            }
            val dateString = dateFormatter.format(calendar.time)
            
            when (dateString) {
                todayString -> TODAY
                yesterdayString -> YESTERDAY
                else -> dateString
            }
        }
        .map { (dateString, transactions) ->
            // Get the most recent timestamp from the group for sorting
            val mostRecentTimestamp = transactions.maxOfOrNull { it.startTime } ?: 0L
            NetworkTransactionGroup(
                date = dateString,
                timestamp = mostRecentTimestamp,
                transactions = transactions.sortedByDescending { it.startTime }
            )
        }
        .sortedByDescending { it.timestamp }
}

/**
 * Formats a timestamp to time string (HH:mm:ss)
 */
fun Long.formatTime(): String {
    val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return timeFormatter.format(Date(this))
}

/**
 * Formats a timestamp to relative time (e.g., "2 minutes ago")
 */
fun Long.formatRelativeTime(): String {
    val now = System.currentTimeMillis()
    val diff = now - this
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        diff < 604800_000 -> "${diff / 86400_000}d ago"
        else -> {
            val dateFormatter = SimpleDateFormat("MMM dd", Locale.getDefault())
            dateFormatter.format(Date(this))
        }
    }
}