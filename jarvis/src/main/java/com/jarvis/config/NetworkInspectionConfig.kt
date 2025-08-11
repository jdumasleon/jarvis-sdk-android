package com.jarvis.config

/**
 * Configuration for network inspection features
 */
data class NetworkInspectionConfig(
    val enableNetworkLogging: Boolean = true,
    val maxRequestBodySize: Long = 1024 * 1024, // 1MB
    val maxResponseBodySize: Long = 1024 * 1024, // 1MB
    val enableRequestLogging: Boolean = true,
    val enableResponseLogging: Boolean = true,
    val excludeHosts: List<String> = emptyList(),
    val includeOnlyHosts: List<String> = emptyList()
) {
    class Builder {
        private var enableNetworkLogging: Boolean = true
        private var maxRequestBodySize: Long = 1024 * 1024
        private var maxResponseBodySize: Long = 1024 * 1024
        private var enableRequestLogging: Boolean = true
        private var enableResponseLogging: Boolean = true
        private var excludeHosts: List<String> = emptyList()
        private var includeOnlyHosts: List<String> = emptyList()

        fun enableNetworkLogging(enabled: Boolean): Builder {
            enableNetworkLogging = enabled
            return this
        }

        fun maxRequestBodySize(sizeInBytes: Long): Builder {
            maxRequestBodySize = sizeInBytes
            return this
        }

        fun maxResponseBodySize(sizeInBytes: Long): Builder {
            maxResponseBodySize = sizeInBytes
            return this
        }

        fun enableRequestLogging(enabled: Boolean): Builder {
            enableRequestLogging = enabled
            return this
        }

        fun enableResponseLogging(enabled: Boolean): Builder {
            enableResponseLogging = enabled
            return this
        }

        fun excludeHosts(vararg hosts: String): Builder {
            excludeHosts = hosts.toList()
            return this
        }

        fun excludeHosts(hosts: List<String>): Builder {
            excludeHosts = hosts
            return this
        }

        fun includeOnlyHosts(vararg hosts: String): Builder {
            includeOnlyHosts = hosts.toList()
            return this
        }

        fun includeOnlyHosts(hosts: List<String>): Builder {
            includeOnlyHosts = hosts
            return this
        }

        fun build(): NetworkInspectionConfig = NetworkInspectionConfig(
            enableNetworkLogging = enableNetworkLogging,
            maxRequestBodySize = maxRequestBodySize,
            maxResponseBodySize = maxResponseBodySize,
            enableRequestLogging = enableRequestLogging,
            enableResponseLogging = enableResponseLogging,
            excludeHosts = excludeHosts,
            includeOnlyHosts = includeOnlyHosts
        )
    }

    companion object {
        fun builder(): Builder = Builder()
    }
}