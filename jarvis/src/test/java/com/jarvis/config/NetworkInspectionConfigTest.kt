package com.jarvis.config

import org.junit.Test
import org.junit.Assert.*

class NetworkInspectionConfigTest {

    @Test
    fun `default configuration has expected values`() {
        val config = NetworkInspectionConfig()

        assertTrue(config.enableNetworkLogging)
        assertEquals(1024 * 1024L, config.maxRequestBodySize)
        assertEquals(1024 * 1024L, config.maxResponseBodySize)
        assertTrue(config.enableRequestLogging)
        assertTrue(config.enableResponseLogging)
        assertTrue(config.excludeHosts.isEmpty())
        assertTrue(config.includeOnlyHosts.isEmpty())
    }

    @Test
    fun `builder creates config with custom values`() {
        val config = NetworkInspectionConfig.builder()
            .enableNetworkLogging(false)
            .maxRequestBodySize(2048)
            .maxResponseBodySize(4096)
            .enableRequestLogging(false)
            .enableResponseLogging(false)
            .excludeHosts("example.com", "test.com")
            .includeOnlyHosts("api.company.com")
            .build()

        assertFalse(config.enableNetworkLogging)
        assertEquals(2048L, config.maxRequestBodySize)
        assertEquals(4096L, config.maxResponseBodySize)
        assertFalse(config.enableRequestLogging)
        assertFalse(config.enableResponseLogging)
        assertEquals(listOf("example.com", "test.com"), config.excludeHosts)
        assertEquals(listOf("api.company.com"), config.includeOnlyHosts)
    }

    @Test
    fun `builder excludeHosts accepts vararg and list`() {
        val configVararg = NetworkInspectionConfig.builder()
            .excludeHosts("host1.com", "host2.com", "host3.com")
            .build()

        val configList = NetworkInspectionConfig.builder()
            .excludeHosts(listOf("host1.com", "host2.com", "host3.com"))
            .build()

        assertEquals(3, configVararg.excludeHosts.size)
        assertEquals(3, configList.excludeHosts.size)
        assertEquals(configVararg.excludeHosts, configList.excludeHosts)
    }

    @Test
    fun `builder includeOnlyHosts accepts vararg and list`() {
        val configVararg = NetworkInspectionConfig.builder()
            .includeOnlyHosts("api1.com", "api2.com")
            .build()

        val configList = NetworkInspectionConfig.builder()
            .includeOnlyHosts(listOf("api1.com", "api2.com"))
            .build()

        assertEquals(2, configVararg.includeOnlyHosts.size)
        assertEquals(2, configList.includeOnlyHosts.size)
        assertEquals(configVararg.includeOnlyHosts, configList.includeOnlyHosts)
    }

    @Test
    fun `builder methods return builder instance for chaining`() {
        val builder = NetworkInspectionConfig.builder()

        assertSame(builder, builder.enableNetworkLogging(true))
        assertSame(builder, builder.maxRequestBodySize(1024))
        assertSame(builder, builder.maxResponseBodySize(1024))
        assertSame(builder, builder.enableRequestLogging(true))
        assertSame(builder, builder.enableResponseLogging(true))
        assertSame(builder, builder.excludeHosts("test.com"))
        assertSame(builder, builder.includeOnlyHosts("api.com"))
    }

    @Test
    fun `companion object creates new builder instance`() {
        val builder1 = NetworkInspectionConfig.builder()
        val builder2 = NetworkInspectionConfig.builder()

        assertNotSame(builder1, builder2)
    }

    @Test
    fun `data class equality works correctly`() {
        val config1 = NetworkInspectionConfig(
            enableNetworkLogging = true,
            maxRequestBodySize = 1024,
            maxResponseBodySize = 2048
        )

        val config2 = NetworkInspectionConfig(
            enableNetworkLogging = true,
            maxRequestBodySize = 1024,
            maxResponseBodySize = 2048
        )

        val config3 = NetworkInspectionConfig(
            enableNetworkLogging = false,
            maxRequestBodySize = 1024,
            maxResponseBodySize = 2048
        )

        assertEquals(config1, config2)
        assertNotEquals(config1, config3)
        assertEquals(config1.hashCode(), config2.hashCode())
    }
}