package com.jarvis.features.preferences.internal.domain.entity

import org.junit.Test
import org.junit.Assert.*

class AppPreferenceTest {

    @Test
    fun `default app preference has expected values`() {
        val preference = AppPreference(
            key = "test_key",
            value = "test_value",
            type = PreferenceType.STRING,
            storageType = PreferenceStorageType.SHARED_PREFERENCES
        )

        assertEquals("test_key", preference.key)
        assertEquals("test_value", preference.value)
        assertEquals(PreferenceType.STRING, preference.type)
        assertEquals(PreferenceStorageType.SHARED_PREFERENCES, preference.storageType)
        assertEquals("test_key", preference.displayName) // defaults to key
        assertNull(preference.description)
        assertFalse(preference.isSystemPreference)
        assertTrue(preference.isEditable)
        assertNull(preference.filePath)
    }

    @Test
    fun `app preference can be customized`() {
        val preference = AppPreference(
            key = "custom_key",
            value = 42,
            type = PreferenceType.INTEGER,
            storageType = PreferenceStorageType.PREFERENCES_DATASTORE,
            displayName = "Custom Setting",
            description = "This is a custom preference",
            isSystemPreference = true,
            isEditable = false,
            filePath = "/data/preferences/custom.xml"
        )

        assertEquals("custom_key", preference.key)
        assertEquals(42, preference.value)
        assertEquals(PreferenceType.INTEGER, preference.type)
        assertEquals(PreferenceStorageType.PREFERENCES_DATASTORE, preference.storageType)
        assertEquals("Custom Setting", preference.displayName)
        assertEquals("This is a custom preference", preference.description)
        assertTrue(preference.isSystemPreference)
        assertFalse(preference.isEditable)
        assertEquals("/data/preferences/custom.xml", preference.filePath)
    }

    @Test
    fun `app preference supports different value types`() {
        val stringPreference = AppPreference(
            key = "string_pref", value = "text",
            type = PreferenceType.STRING,
            storageType = PreferenceStorageType.SHARED_PREFERENCES
        )
        val booleanPreference = AppPreference(
            key = "bool_pref", value = true,
            type = PreferenceType.BOOLEAN,
            storageType = PreferenceStorageType.SHARED_PREFERENCES
        )
        val intPreference = AppPreference(
            key = "int_pref", value = 123,
            type = PreferenceType.INTEGER,
            storageType = PreferenceStorageType.SHARED_PREFERENCES
        )
        val floatPreference = AppPreference(
            key = "float_pref", value = 3.14f,
            type = PreferenceType.FLOAT,
            storageType = PreferenceStorageType.SHARED_PREFERENCES
        )

        assertTrue(stringPreference.value is String)
        assertTrue(booleanPreference.value is Boolean)
        assertTrue(intPreference.value is Int)
        assertTrue(floatPreference.value is Float)
    }

    @Test
    fun `data class equality works correctly`() {
        val preference1 = AppPreference(
            key = "test", value = "value",
            type = PreferenceType.STRING,
            storageType = PreferenceStorageType.SHARED_PREFERENCES
        )
        val preference2 = AppPreference(
            key = "test", value = "value",
            type = PreferenceType.STRING,
            storageType = PreferenceStorageType.SHARED_PREFERENCES
        )
        val preference3 = AppPreference(
            key = "different", value = "value",
            type = PreferenceType.STRING,
            storageType = PreferenceStorageType.SHARED_PREFERENCES
        )

        assertEquals(preference1, preference2)
        assertNotEquals(preference1, preference3)
        assertEquals(preference1.hashCode(), preference2.hashCode())
    }
}

class PreferenceFilterTest {

    @Test
    fun `default filter has expected values`() {
        val filter = PreferenceFilter()

        assertEquals("", filter.searchQuery)
        assertNull(filter.typeFilter)
        assertNull(filter.storageTypeFilter)
        assertTrue(filter.showSystemPreferences)
    }

    @Test
    fun `filter can be customized`() {
        val filter = PreferenceFilter(
            searchQuery = "network",
            typeFilter = PreferenceType.BOOLEAN,
            storageTypeFilter = PreferenceStorageType.PROTO_DATASTORE,
            showSystemPreferences = false
        )

        assertEquals("network", filter.searchQuery)
        assertEquals(PreferenceType.BOOLEAN, filter.typeFilter)
        assertEquals(PreferenceStorageType.PROTO_DATASTORE, filter.storageTypeFilter)
        assertFalse(filter.showSystemPreferences)
    }

    @Test
    fun `filter data class equality works`() {
        val filter1 = PreferenceFilter(searchQuery = "test", typeFilter = PreferenceType.STRING)
        val filter2 = PreferenceFilter(searchQuery = "test", typeFilter = PreferenceType.STRING)
        val filter3 = PreferenceFilter(searchQuery = "different", typeFilter = PreferenceType.STRING)

        assertEquals(filter1, filter2)
        assertNotEquals(filter1, filter3)
        assertEquals(filter1.hashCode(), filter2.hashCode())
    }
}

class PreferenceGroupTest {

    @Test
    fun `default preference group has expected values`() {
        val group = PreferenceGroup(storageType = PreferenceStorageType.SHARED_PREFERENCES)

        assertEquals(PreferenceStorageType.SHARED_PREFERENCES, group.storageType)
        assertTrue(group.preferences.isEmpty())
        assertFalse(group.isLoading)
        assertNull(group.error)
    }

    @Test
    fun `preference group can contain preferences`() {
        val preferences = listOf(
            AppPreference(
                key = "pref1", value = "value1",
                type = PreferenceType.STRING,
                storageType = PreferenceStorageType.SHARED_PREFERENCES
            ),
            AppPreference(
                key = "pref2", value = true,
                type = PreferenceType.BOOLEAN,
                storageType = PreferenceStorageType.SHARED_PREFERENCES
            )
        )
        val group = PreferenceGroup(
            storageType = PreferenceStorageType.SHARED_PREFERENCES,
            preferences = preferences,
            isLoading = true,
            error = "Load failed"
        )

        assertEquals(PreferenceStorageType.SHARED_PREFERENCES, group.storageType)
        assertEquals(2, group.preferences.size)
        assertEquals("pref1", group.preferences[0].key)
        assertEquals("pref2", group.preferences[1].key)
        assertTrue(group.isLoading)
        assertEquals("Load failed", group.error)
    }

    @Test
    fun `preference group data class equality works`() {
        val group1 = PreferenceGroup(
            storageType = PreferenceStorageType.SHARED_PREFERENCES,
            isLoading = true
        )
        val group2 = PreferenceGroup(
            storageType = PreferenceStorageType.SHARED_PREFERENCES,
            isLoading = true
        )
        val group3 = PreferenceGroup(
            storageType = PreferenceStorageType.PROTO_DATASTORE,
            isLoading = true
        )

        assertEquals(group1, group2)
        assertNotEquals(group1, group3)
        assertEquals(group1.hashCode(), group2.hashCode())
    }
}

class PreferenceTypeTest {

    @Test
    fun `all preference types are defined`() {
        val expectedTypes = setOf(
            PreferenceType.STRING,
            PreferenceType.BOOLEAN,
            PreferenceType.INTEGER,
            PreferenceType.LONG,
            PreferenceType.FLOAT,
            PreferenceType.DOUBLE,
            PreferenceType.STRING_SET,
            PreferenceType.BYTES,
            PreferenceType.PROTO_MESSAGE
        )

        val actualTypes = PreferenceType.values().toSet()
        assertEquals(expectedTypes, actualTypes)
        assertEquals(9, PreferenceType.values().size)
    }

    @Test
    fun `preference type enum ordinals are consistent`() {
        assertEquals(0, PreferenceType.STRING.ordinal)
        assertEquals(1, PreferenceType.BOOLEAN.ordinal)
        assertEquals(2, PreferenceType.INTEGER.ordinal)
        assertEquals(3, PreferenceType.LONG.ordinal)
        assertEquals(4, PreferenceType.FLOAT.ordinal)
        assertEquals(5, PreferenceType.DOUBLE.ordinal)
        assertEquals(6, PreferenceType.STRING_SET.ordinal)
        assertEquals(7, PreferenceType.BYTES.ordinal)
        assertEquals(8, PreferenceType.PROTO_MESSAGE.ordinal)
    }
}

class PreferenceStorageTypeTest {

    @Test
    fun `all storage types are defined`() {
        val expectedTypes = setOf(
            PreferenceStorageType.SHARED_PREFERENCES,
            PreferenceStorageType.PREFERENCES_DATASTORE,
            PreferenceStorageType.PROTO_DATASTORE
        )

        val actualTypes = PreferenceStorageType.values().toSet()
        assertEquals(expectedTypes, actualTypes)
        assertEquals(3, PreferenceStorageType.values().size)
    }

    @Test
    fun `storage type enum ordinals are consistent`() {
        assertEquals(0, PreferenceStorageType.SHARED_PREFERENCES.ordinal)
        assertEquals(1, PreferenceStorageType.PREFERENCES_DATASTORE.ordinal)
        assertEquals(2, PreferenceStorageType.PROTO_DATASTORE.ordinal)
    }
}