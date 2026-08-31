package com.bonfs.tcc.kmpchat.data.local

/**
 * Platform-specific secure key vault provider.
 * Retrieves or generates a 256-bit (32 bytes) master key stored in hardware-backed keystores.
 */
expect class DatabaseKeyProvider() {
    fun getOrCreatePassphrase(alias: String): ByteArray
    fun clearKey(alias: String)
}
