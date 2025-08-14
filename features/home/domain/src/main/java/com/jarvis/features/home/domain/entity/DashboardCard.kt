package com.jarvis.features.home.domain.entity

/**
 * Dashboard card types that can be displayed and reordered
 */
enum class DashboardCardType(
    val id: String,
    val title: String,
    val description: String,
    val defaultOrder: Int
) {
    HEALTH_SUMMARY("health_summary", "Health Summary", "Overall app health score and key metrics", 0),
    PERFORMANCE_METRICS("performance_metrics", "Performance", "Real-time CPU, memory, and FPS metrics", 1),
    NETWORK_OVERVIEW("network_overview", "Network", "Network requests and response analytics", 2),
    PREFERENCES_OVERVIEW("preferences_overview", "Preferences", "App preferences and storage analytics", 3),
    NETWORK_TIMELINE("network_timeline", "Request Timeline", "Requests over time area chart", 4),
    HTTP_METHODS("http_methods", "HTTP Methods", "Request method distribution", 5),
    TOP_ENDPOINTS("top_endpoints", "Top Endpoints", "Most frequently used endpoints", 6),
    SLOW_ENDPOINTS("slow_endpoints", "Slowest Endpoints", "Performance bottlenecks", 7);
    
    companion object {
        fun getDefaultOrder(): List<DashboardCardType> = values().sortedBy { it.defaultOrder }
        
        fun fromId(id: String): DashboardCardType? = values().find { it.id == id }
    }
}

/**
 * Dashboard card configuration with positioning and visibility
 */
data class DashboardCard(
    val type: DashboardCardType,
    val order: Int,
    val isVisible: Boolean = true,
    val size: CardSize = CardSize.MEDIUM,
    val customTitle: String? = null
) {
    val displayTitle: String
        get() = customTitle ?: type.title
}

/**
 * Card size options for flexible layout
 */
enum class CardSize(val spanCount: Int, val heightDp: Int) {
    SMALL(1, 200),
    MEDIUM(1, 300),
    LARGE(2, 300),
    EXTRA_LARGE(2, 400)
}

/**
 * Dashboard layout configuration
 */
data class DashboardLayout(
    val cards: List<DashboardCard>,
    val sessionFilter: SessionFilter = SessionFilter.LAST_SESSION,
    val autoRefresh: Boolean = true,
    val refreshIntervalMs: Long = 5000L,
    val lastModified: Long = System.currentTimeMillis()
) {
    companion object {
        fun getDefault(): DashboardLayout = DashboardLayout(
            cards = DashboardCardType.getDefaultOrder().map { cardType ->
                DashboardCard(
                    type = cardType,
                    order = cardType.defaultOrder,
                    size = when (cardType) {
                        DashboardCardType.HEALTH_SUMMARY -> CardSize.LARGE
                        DashboardCardType.NETWORK_TIMELINE -> CardSize.LARGE
                        DashboardCardType.PERFORMANCE_METRICS -> CardSize.MEDIUM
                        else -> CardSize.MEDIUM
                    }
                )
            }
        )
    }
}

/**
 * Drag and drop state for reordering cards
 */
data class DragDropState(
    val isDragging: Boolean = false,
    val draggedCard: DashboardCard? = null,
    val targetIndex: Int? = null,
    val dragOffset: Pair<Float, Float> = 0f to 0f
)