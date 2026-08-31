package com.bonfs.tcc.kmpchat.data.local

import java.io.File
import java.security.SecureRandom
import java.util.Base64

private const val PASSPHRASE_LENGTH_BYTES: Int = 32

actual class DatabaseKeyProvider actual constructor() {

    actual fun getOrCreatePassphrase(alias: String): ByteArray {
        val existing = readKeyFile(alias)
        if (existing != null && existing.size == PASSPHRASE_LENGTH_BYTES) {
            return existing
        }
        val newKey = generateSecureRandomBytes(PASSPHRASE_LENGTH_BYTES)
        writeKeyFile(alias, newKey)
        return newKey
    }

    actual fun clearKey(alias: String) {
        val file = getKeyFile(alias)
        if (file.exists()) {
            file.writeBytes(ByteArray(file.length().toInt()))
            file.delete()
        }
    }

    private fun generateSecureRandomBytes(length: Int): ByteArray {
        val bytes = ByteArray(length)
        SecureRandom().nextBytes(bytes)
        return bytes
    }

    private fun getKeyDirectory(): File {
        val userHome = System.getProperty("user.home") ?: "."
        val keyDir = File(userHome, ".kmpchat/keys")
        if (!keyDir.exists()) {
            keyDir.mkdirs()
        }
        return keyDir
    }

    private fun getKeyFile(alias: String): File {
        return File(getKeyDirectory(), "$alias.key")
    }

    private fun readKeyFile(alias: String): ByteArray? {
        val file = getKeyFile(alias)
        if (!file.exists()) return null
        return try {
            Base64.getDecoder().decode(file.readText().trim())
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun writeKeyFile(alias: String, key: ByteArray) {
        val file = getKeyFile(alias)
        val encoded = Base64.getEncoder().encodeToString(key)
        file.writeText(encoded)
    }
}
