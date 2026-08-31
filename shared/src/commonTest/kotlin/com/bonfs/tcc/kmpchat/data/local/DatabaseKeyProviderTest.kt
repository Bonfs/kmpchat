package com.bonfs.tcc.kmpchat.data.local

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class DatabaseKeyProviderTest {

    private val keyProvider = DatabaseKeyProvider()
    private val testAlias = "matrix_test_alias"
    private val secondaryAlias = "matrix_other_alias"

    @AfterTest
    fun tearDown() {
        keyProvider.clearKey(testAlias)
        keyProvider.clearKey(secondaryAlias)
    }

    @Test
    fun shouldGenerate256BitKey() {
        val key = keyProvider.getOrCreatePassphrase(testAlias)
        assertNotNull(key)
        assertEquals(32, key.size, "Database key must be exactly 256 bits (32 bytes)")
    }

    @Test
    fun shouldReturnSameKeyForSameAlias() {
        val firstKey = keyProvider.getOrCreatePassphrase(testAlias)
        val secondKey = keyProvider.getOrCreatePassphrase(testAlias)
        assertEquals(
            firstKey.toList(),
            secondKey.toList(),
            "Repeated retrieval for same alias must return identical key material"
        )
    }

    @Test
    fun shouldReturnDifferentKeyForDifferentAlias() {
        val firstKey = keyProvider.getOrCreatePassphrase(testAlias)
        val secondKey = keyProvider.getOrCreatePassphrase(secondaryAlias)
        assertFalse(
            firstKey.contentEquals(secondKey),
            "Different aliases must generate distinct cryptographic keys"
        )
    }

    @Test
    fun shouldClearKeyAndGenerateNewOne() {
        val originalKey = keyProvider.getOrCreatePassphrase(testAlias)
        keyProvider.clearKey(testAlias)
        val newKey = keyProvider.getOrCreatePassphrase(testAlias)
        assertEquals(32, newKey.size)
    }
}
