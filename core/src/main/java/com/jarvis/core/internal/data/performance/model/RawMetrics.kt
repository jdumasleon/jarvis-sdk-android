package com.jarvis.core.internal.data.performance.model

import com.jarvis.core.internal.domain.performance.CpuMetrics
import com.jarvis.core.internal.domain.performance.FpsMetrics
import com.jarvis.core.internal.domain.performance.MemoryMetrics
import com.jarvis.core.internal.domain.performance.ModuleMetrics

data class RawMetrics(
    val cpu: CpuMetrics,
    val memory: MemoryMetrics,
    val fps: FpsMetrics,
    val modules: ModuleMetrics,
    val battery: Float,
)