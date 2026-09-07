package com.bonfs.tcc.kmpchat.data.local

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

private const val PASSPHRASE_LENGTH_BYTES: Int = 32
private const val GCM_IV_LENGTH_BYTES: Int = 12
private const val GCM_TAG_LENGTH_BITS: Int = 128
private const val SALT_LENGTH_BYTES: Int = 16
private const val PBKDF2_ITERATIONS: Int = 100_000
private const val SERVICE_IDENTIFIER: String = "com.bonfs.tcc.kmpchat"

private object MacKeychainVault {
    fun readKey(alias: String): ByteArray? {
        return try {
            val process = ProcessBuilder(
                "/usr/bin/security",
                "find-generic-password",
                "-a", alias,
                "-s", SERVICE_IDENTIFIER,
                "-w"
            ).start()
            val output = process.inputStream.bufferedReader().readText().trim()
            if (process.waitFor() != 0 || output.isBlank()) null
            else Base64.getDecoder().decode(output)
        } catch (_: Exception) {
            null
        }
    }

    fun writeKey(alias: String, key: ByteArray): Boolean {
        return try {
            val encoded = Base64.getEncoder().encodeToString(key)
            val process = ProcessBuilder(
                "/usr/bin/security",
                "add-generic-password",
                "-a", alias,
                "-s", SERVICE_IDENTIFIER,
                "-w", encoded,
                "-U"
            ).start()
            process.waitFor() == 0
        } catch (_: Exception) {
            false
        }
    }

    fun deleteKey(alias: String): Boolean {
        return try {
            val process = ProcessBuilder(
                "/usr/bin/security",
                "delete-generic-password",
                "-a", alias,
                "-s", SERVICE_IDENTIFIER
            ).start()
            process.waitFor() == 0
        } catch (_: Exception) {
            false
        }
    }
}

private object LinuxSecretToolVault {
    fun isAvailable(): Boolean {
        return try {
            val process = ProcessBuilder("which", "secret-tool").start()
            process.waitFor() == 0
        } catch (_: Exception) {
            false
        }
    }

    fun readKey(alias: String): ByteArray? {
        return try {
            val process = ProcessBuilder(
                "secret-tool", "lookup",
                "service", SERVICE_IDENTIFIER,
                "account", alias
            ).start()
            val output = process.inputStream.bufferedReader().readText().trim()
            if (process.waitFor() != 0 || output.isBlank()) null
            else Base64.getDecoder().decode(output)
        } catch (_: Exception) {
            null
        }
    }

    fun writeKey(alias: String, key: ByteArray): Boolean {
        return try {
            val encoded = Base64.getEncoder().encodeToString(key)
            val process = ProcessBuilder(
                "secret-tool", "store",
                "--label=KmpChat Key",
                "service", SERVICE_IDENTIFIER,
                "account", alias
            ).start()
            process.outputStream.bufferedWriter().use { it.write(encoded) }
            process.waitFor() == 0
        } catch (_: Exception) {
            false
        }
    }

    fun deleteKey(alias: String): Boolean {
        return try {
            val process = ProcessBuilder(
                "secret-tool", "clear",
                "service", SERVICE_IDENTIFIER,
                "account", alias
            ).start()
            process.waitFor() == 0
        } catch (_: Exception) {
            false
        }
    }
}

private object WindowsDpapiVault {
    private fun getStorageDirectory(): File {
        val userHome = System.getProperty("user.home") ?: "."
        val dir = File(userHome, ".kmpchat/vault")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun getDpapiFile(alias: String): File {
        return File(getStorageDirectory(), "$alias.dpapi")
    }

    fun isAvailable(): Boolean {
        return try {
            val process = ProcessBuilder("powershell", "-NoProfile", "-Command", "echo ok").start()
            process.waitFor() == 0
        } catch (_: Exception) {
            false
        }
    }

    fun readKey(alias: String): ByteArray? {
        val file = getDpapiFile(alias)
        if (!file.exists()) return null
        return try {
            val escapedPath = file.absolutePath.replace("'", "''")
            val script = "Add-Type -AssemblyName System.Security; " +
                "\$enc = [System.IO.File]::ReadAllBytes('$escapedPath'); " +
                "\$dec = [System.Security.Cryptography.ProtectedData]::Unprotect(\$enc, \$null, [System.Security.Cryptography.DataProtectionScope]::CurrentUser); " +
                "[System.Convert]::ToBase64String(\$dec)"
            val process = ProcessBuilder("powershell", "-NoProfile", "-NonInteractive", "-Command", script).start()
            val output = process.inputStream.bufferedReader().readText().trim()
            if (process.waitFor() != 0 || output.isBlank()) null
            else Base64.getDecoder().decode(output)
        } catch (_: Exception) {
            null
        }
    }

    fun writeKey(alias: String, key: ByteArray): Boolean {
        return try {
            val file = getDpapiFile(alias)
            val base64Key = Base64.getEncoder().encodeToString(key)
            val escapedPath = file.absolutePath.replace("'", "''")
            val script = "Add-Type -AssemblyName System.Security; " +
                "\$raw = [System.Convert]::FromBase64String('$base64Key'); " +
                "\$enc = [System.Security.Cryptography.ProtectedData]::Protect(\$raw, \$null, [System.Security.Cryptography.DataProtectionScope]::CurrentUser); " +
                "[System.IO.File]::WriteAllBytes('$escapedPath', \$enc)"
            val process = ProcessBuilder("powershell", "-NoProfile", "-NonInteractive", "-Command", script).start()
            process.waitFor() == 0
        } catch (_: Exception) {
            false
        }
    }

    fun deleteKey(alias: String): Boolean {
        val file = getDpapiFile(alias)
        if (file.exists()) {
            file.writeBytes(ByteArray(file.length().toInt()))
            return file.delete()
        }
        return true
    }
}

private object EncryptedFileVault {
    private fun getStorageDirectory(): File {
        val userHome = System.getProperty("user.home") ?: "."
        val dir = File(userHome, ".kmpchat/vault")
        if (!dir.exists()) {
            dir.mkdirs()
            setStrictPermissions(dir)
        }
        return dir
    }

    private fun setStrictPermissions(file: File) {
        try {
            val permissions: Set<PosixFilePermission> = PosixFilePermissions.fromString("rwx------")
            Files.setPosixFilePermissions(file.toPath(), permissions)
        } catch (_: UnsupportedOperationException) {
            file.setReadable(true, true)
            file.setWritable(true, true)
            file.setExecutable(true, true)
        }
    }

    private fun deriveSecretKey(salt: ByteArray): SecretKeySpec {
        val rawEntropy = (System.getProperty("user.name") ?: "matrix") +
            (System.getProperty("os.name") ?: "os") +
            (System.getProperty("user.home") ?: "home")
        val spec = PBEKeySpec(rawEntropy.toCharArray(), salt, PBKDF2_ITERATIONS, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    fun readKey(alias: String): ByteArray? {
        val file = File(getStorageDirectory(), "$alias.enc")
        if (!file.exists()) return null
        return try {
            val allBytes = file.readBytes()
            if (allBytes.size < SALT_LENGTH_BYTES + GCM_IV_LENGTH_BYTES + 1) return null
            val salt = allBytes.copyOfRange(0, SALT_LENGTH_BYTES)
            val iv = allBytes.copyOfRange(SALT_LENGTH_BYTES, SALT_LENGTH_BYTES + GCM_IV_LENGTH_BYTES)
            val ciphertext = allBytes.copyOfRange(SALT_LENGTH_BYTES + GCM_IV_LENGTH_BYTES, allBytes.size)
            val keySpec = deriveSecretKey(salt)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
            cipher.doFinal(ciphertext)
        } catch (_: Exception) {
            null
        }
    }

    fun writeKey(alias: String, key: ByteArray) {
        val file = File(getStorageDirectory(), "$alias.enc")
        val salt = ByteArray(SALT_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(GCM_IV_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val keySpec = deriveSecretKey(salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        val ciphertext = cipher.doFinal(key)
        val combined = salt + iv + ciphertext
        file.writeBytes(combined)
        setStrictPermissions(file)
    }

    fun deleteKey(alias: String) {
        val file = File(getStorageDirectory(), "$alias.enc")
        if (file.exists()) {
            file.writeBytes(ByteArray(file.length().toInt()))
            file.delete()
        }
    }
}

actual class DatabaseKeyProvider actual constructor() {

    private val osName: String = System.getProperty("os.name")?.lowercase() ?: ""

    actual fun getOrCreatePassphrase(alias: String): ByteArray {
        val existing = readFromSecureVault(alias)
        if (existing != null && existing.size == PASSPHRASE_LENGTH_BYTES) {
            return existing
        }
        val newKey = generateSecureRandomBytes(PASSPHRASE_LENGTH_BYTES)
        writeToSecureVault(alias, newKey)
        return newKey
    }

    actual fun clearKey(alias: String) {
        deleteFromSecureVault(alias)
        wipeLegacyKeyFile(alias)
    }

    private fun readFromSecureVault(alias: String): ByteArray? {
        return when {
            osName.contains("mac") -> MacKeychainVault.readKey(alias)
            osName.contains("win") && WindowsDpapiVault.isAvailable() -> WindowsDpapiVault.readKey(alias)
            osName.contains("linux") && LinuxSecretToolVault.isAvailable() -> LinuxSecretToolVault.readKey(alias)
            else -> EncryptedFileVault.readKey(alias)
        }
    }

    private fun writeToSecureVault(alias: String, key: ByteArray) {
        val success = when {
            osName.contains("mac") -> MacKeychainVault.writeKey(alias, key)
            osName.contains("win") && WindowsDpapiVault.isAvailable() -> WindowsDpapiVault.writeKey(alias, key)
            osName.contains("linux") && LinuxSecretToolVault.isAvailable() -> LinuxSecretToolVault.writeKey(alias, key)
            else -> false
        }
        if (!success) {
            EncryptedFileVault.writeKey(alias, key)
        }
    }

    private fun deleteFromSecureVault(alias: String) {
        if (osName.contains("mac")) {
            MacKeychainVault.deleteKey(alias)
        }
        if (osName.contains("win")) {
            WindowsDpapiVault.deleteKey(alias)
        }
        if (osName.contains("linux")) {
            LinuxSecretToolVault.deleteKey(alias)
        }
        EncryptedFileVault.deleteKey(alias)
    }

    private fun generateSecureRandomBytes(length: Int): ByteArray {
        val bytes = ByteArray(length)
        SecureRandom().nextBytes(bytes)
        return bytes
    }

    private fun getLegacyKeyFile(alias: String): File {
        val userHome = System.getProperty("user.home") ?: "."
        return File(userHome, ".kmpchat/keys/$alias.key")
    }

    private fun wipeLegacyKeyFile(alias: String) {
        val legacyFile = getLegacyKeyFile(alias)
        if (legacyFile.exists()) {
            legacyFile.writeBytes(ByteArray(legacyFile.length().toInt()))
            legacyFile.delete()
        }
    }
}
