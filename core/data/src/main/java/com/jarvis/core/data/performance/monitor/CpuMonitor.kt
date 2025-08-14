package com.jarvis.core.data.performance.monitor

import android.os.Build
import android.os.Process
import com.jarvis.core.domain.performance.CpuMetrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CpuMonitor @Inject constructor() {
    
    private var lastSystemCpuTime = 0L
    private var lastSystemIdleTime = 0L
    private var lastAppCpuTime = 0L
    
    fun getCpuMetricsFlow(intervalMs: Long = 1000): Flow<CpuMetrics> = flow {
        while (true) {
            val metrics = getCurrentCpuMetrics()
            emit(metrics)
            kotlinx.coroutines.delay(intervalMs)
        }
    }.flowOn(Dispatchers.IO)
    
    private fun getCurrentCpuMetrics(): CpuMetrics {
        val systemCpuUsage = getSystemCpuUsage()
        val appCpuUsage = getAppCpuUsage()
        val threadCount = getThreadCount()
        val cores = Runtime.getRuntime().availableProcessors()
        
        return CpuMetrics(
            cpuUsagePercent = maxOf(systemCpuUsage, appCpuUsage),
            appCpuUsagePercent = appCpuUsage,
            systemCpuUsagePercent = systemCpuUsage,
            cores = cores,
            threadCount = threadCount
        )
    }
    
    private fun getSystemCpuUsage(): Float {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getSystemCpuUsageApi26()
            } else {
                getSystemCpuUsageLegacy()
            }
        } catch (e: Exception) {
            0f
        }
    }
    
    private fun getSystemCpuUsageApi26(): Float {
        return try {
            val statFile = "/proc/stat"
            BufferedReader(FileReader(statFile)).use { reader ->
                val line = reader.readLine()
                if (line != null && line.startsWith("cpu ")) {
                    val times = line.split("\\s+".toRegex()).drop(1).map { it.toLongOrNull() ?: 0L }
                    if (times.size >= 4) {
                        val user = times[0]
                        val nice = times[1]
                        val system = times[2]
                        val idle = times[3]
                        
                        val totalCpuTime = user + nice + system + idle
                        val totalIdleTime = idle
                        
                        val cpuUsage = if (lastSystemCpuTime > 0) {
                            val cpuTimeDiff = totalCpuTime - lastSystemCpuTime
                            val idleTimeDiff = totalIdleTime - lastSystemIdleTime
                            
                            if (cpuTimeDiff > 0) {
                                ((cpuTimeDiff - idleTimeDiff).toFloat() / cpuTimeDiff.toFloat()) * 100f
                            } else 0f
                        } else 0f
                        
                        lastSystemCpuTime = totalCpuTime
                        lastSystemIdleTime = totalIdleTime
                        
                        cpuUsage.coerceIn(0f, 100f)
                    } else 0f
                } else 0f
            }
        } catch (e: IOException) {
            0f
        }
    }
    
    private fun getSystemCpuUsageLegacy(): Float {
        // Fallback for older Android versions
        return try {
            val runtime = Runtime.getRuntime()
            val availableProcessors = runtime.availableProcessors()
            // Simple approximation based on load average
            val loadAverage = getLoadAverage()
            ((loadAverage / availableProcessors) * 100f).coerceIn(0f, 100f)
        } catch (e: Exception) {
            0f
        }
    }
    
    private fun getLoadAverage(): Float {
        return try {
            BufferedReader(FileReader("/proc/loadavg")).use { reader ->
                val line = reader.readLine()
                line?.split(" ")?.get(0)?.toFloatOrNull() ?: 0f
            }
        } catch (e: IOException) {
            0f
        }
    }
    
    private fun getAppCpuUsage(): Float {
        return try {
            val pid = Process.myPid()
            val statFile = "/proc/$pid/stat"
            
            BufferedReader(FileReader(statFile)).use { reader ->
                val line = reader.readLine()
                if (line != null) {
                    val stats = line.split(" ")
                    if (stats.size >= 15) {
                        val utime = stats[13].toLongOrNull() ?: 0L
                        val stime = stats[14].toLongOrNull() ?: 0L
                        val totalAppTime = utime + stime
                        
                        val appCpuUsage = if (lastAppCpuTime > 0) {
                            val timeDiff = totalAppTime - lastAppCpuTime
                            // Convert from jiffies to percentage (rough approximation)
                            (timeDiff.toFloat() / 100f).coerceIn(0f, 100f)
                        } else 0f
                        
                        lastAppCpuTime = totalAppTime
                        appCpuUsage
                    } else 0f
                } else 0f
            }
        } catch (e: IOException) {
            0f
        }
    }
    
    private fun getThreadCount(): Int {
        return try {
            val pid = Process.myPid()
            val statusFile = "/proc/$pid/status"
            
            BufferedReader(FileReader(statusFile)).use { reader ->
                reader.useLines { lines ->
                    lines.find { it.startsWith("Threads:") }
                        ?.substringAfter("Threads:")
                        ?.trim()
                        ?.toIntOrNull() ?: 0
                }
            }
        } catch (e: IOException) {
            0
        }
    }
}