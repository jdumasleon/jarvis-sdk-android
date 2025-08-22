package com.jarvis.demo.presentation.inspector

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textview.MaterialTextView
import com.jarvis.demo.R
import com.jarvis.demo.data.repository.ApiCallResult
import com.jarvis.demo.presentation.utlis.setRoundedBgM3
import java.text.SimpleDateFormat
import java.util.*

class ApiCallsAdapter(
    private val onItemClick: (ApiCallResult) -> Unit
) : ListAdapter<ApiCallResult, ApiCallsAdapter.ApiCallViewHolder>(ApiCallDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApiCallViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_api_call, parent, false)
        return ApiCallViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ApiCallViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ApiCallViewHolder(
        itemView: View,
        private val onItemClick: (ApiCallResult) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val urlText: MaterialTextView = itemView.findViewById(R.id.url_text)
        private val hostText: MaterialTextView = itemView.findViewById(R.id.host_text)
        private val statusIndicator: View = itemView.findViewById(R.id.status_indicator)
        private val statusCodeText: MaterialTextView = itemView.findViewById(R.id.status_code_text)
        private val methodBadge: MaterialTextView = itemView.findViewById(R.id.method_badge)
        private val timestampText: MaterialTextView = itemView.findViewById(R.id.timestamp_text)
        private val durationText: MaterialTextView = itemView.findViewById(R.id.duration_text)
        private val durationProgress: LinearProgressIndicator = itemView.findViewById(R.id.duration_progress)
        private val errorText: MaterialTextView = itemView.findViewById(R.id.error_text)

        private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        fun bind(apiCall: ApiCallResult) {
            // URL and Host
            urlText.text = apiCall.url
            hostText.text = apiCall.host

            // Status indicator
            val statusColor = if (apiCall.isSuccess) {
                ContextCompat.getColor(itemView.context, R.color.success_color)
            } else {
                ContextCompat.getColor(itemView.context, R.color.error_color)
            }
            val rSmall = itemView.resources.getDimension(R.dimen.radius_xl)
            statusIndicator.setBackgroundColor(statusColor)
            statusIndicator.setRoundedBgM3(
                color = statusColor,
                topStart = rSmall, topEnd = rSmall, bottomEnd = rSmall, bottomStart = rSmall
            )

            // Status code
            if (apiCall.statusCode > 0) {
                statusCodeText.text = apiCall.statusCode.toString()
                statusCodeText.setTextColor(statusColor)
                statusCodeText.visibility = View.VISIBLE
            } else {
                statusCodeText.visibility = View.GONE
            }

            // HTTP Method badge
            methodBadge.text = apiCall.method
            val methodColor = getMethodColor(apiCall.method)
            methodBadge.setBackgroundColor(methodColor)

            val rBadge = itemView.resources.getDimension(R.dimen.radius_m)
            methodBadge.setRoundedBgM3(methodColor, rBadge)

            // Timestamp
            timestampText.text = timeFormat.format(Date(apiCall.startTime))

            // Duration
            durationText.text = "${apiCall.duration}ms"

            // Progress bar for duration visualization
            if (apiCall.duration > 0) {
                val maxDuration = 2000f // 2 seconds max for visualization
                val progress = ((apiCall.duration / maxDuration) * 100).coerceAtMost(100f).toInt()
                
                durationProgress.progress = progress
                durationProgress.setIndicatorColor(statusColor)
                durationProgress.visibility = View.VISIBLE
            } else {
                durationProgress.visibility = View.GONE
            }

            // Error message
            if (!apiCall.error.isNullOrBlank()) {
                errorText.text = apiCall.error
                errorText.visibility = View.VISIBLE
            } else {
                errorText.visibility = View.GONE
            }

            // Click listener
            itemView.setOnClickListener {
                onItemClick(apiCall)
            }
        }

        private fun getMethodColor(method: String): Int {
            return when (method.uppercase()) {
                "GET" -> ContextCompat.getColor(itemView.context, R.color.method_get_color)
                "POST" -> ContextCompat.getColor(itemView.context, R.color.method_post_color)
                "PUT" -> ContextCompat.getColor(itemView.context, R.color.method_put_color)
                "PATCH" -> ContextCompat.getColor(itemView.context, R.color.method_patch_color)
                "DELETE" -> ContextCompat.getColor(itemView.context, R.color.method_delete_color)
                else -> ContextCompat.getColor(itemView.context, R.color.method_default_color)
            }
        }
    }

    private class ApiCallDiffCallback : DiffUtil.ItemCallback<ApiCallResult>() {
        override fun areItemsTheSame(oldItem: ApiCallResult, newItem: ApiCallResult): Boolean {
            // Use combination of URL and start time as unique identifier
            return oldItem.url == newItem.url && oldItem.startTime == newItem.startTime
        }

        override fun areContentsTheSame(oldItem: ApiCallResult, newItem: ApiCallResult): Boolean {
            return oldItem == newItem
        }
    }
}