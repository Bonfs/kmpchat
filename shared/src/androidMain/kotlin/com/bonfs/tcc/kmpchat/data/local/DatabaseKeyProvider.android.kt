package com.bonfs.tcc.kmpchat.data.local

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

private const val ANDROID_KEYSTORE_PROVIDER: String = "AndroidKeyStore"
private const val TRANSFORMATION_AES_GCM: String = "AES/GCM/NoPadding"
private const val GCM_TAG_LENGTH_BITS: Int = 128
private const val GCM_IV_LENGTH_BYTES: Int = 12
private const val PASSPHRASE_LENGTH_BYTES: Int = 32

actual class DatabaseKeyProvider actual constructor() {

    private val isAndroidKeyStoreSupported: Boolean
    private val keyStore: KeyStore?

    init {
        var store: KeyStore? = null
        var supported = false
        try {
            store = KeyStore.getInstance(ANDROID_KEYSTORE_PROVIDER).apply {
                load(null)
            }
            supported = true
        } catch (_: Exception) {
            store = null
            supported = false
        }
        keyStore = store
        isAndroidKeyStoreSupported = supported
    }

    actual fun getOrCreatePassphrase(alias: String): ByteArray {
        val secretKey = getOrCreateSecretKey(alias)
        val encryptedData = readEncryptedData(alias)
        if (encryptedData != null && encryptedData.size > GCM_IV_LENGTH_BYTES) {
            return decryptData(secretKey, encryptedData)
        }
        val newPassphrase = generateSecureRandomBytes(PASSPHRASE_LENGTH_BYTES)
        val newEncryptedData = encryptData(secretKey, newPassphrase)
        writeEncryptedData(alias, newEncryptedData)
        return newPassphrase
    }

    actual fun clearKey(alias: String) {
        if (keyStore != null && keyStore.containsAlias(alias)) {
            keyStore.deleteEntry(alias)
        }
        deleteEncryptedFile(alias)
        deleteRawKeyFile(alias)
    }

    private fun getOrCreateSecretKey(alias: String): SecretKey {
        if (keyStore != null && isAndroidKeyStoreSupported) {
            if (keyStore.containsAlias(alias)) {
                val entry = keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry
                if (entry != null) {
                    return entry.secretKey
                }
            }
            return generateAndroidKeyStoreKey(alias)
        }
        return getOrCreateSoftwareKey(alias)
    }

    private fun generateAndroidKeyStoreKey(alias: String): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE_PROVIDER
        )
        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private fun getOrCreateSoftwareKey(alias: String): SecretKey {
        val keyFile = getRawKeyFile(alias)
        if (keyFile.exists()) {
            val keyBytes = keyFile.readBytes()
            if (keyBytes.size == PASSPHRASE_LENGTH_BYTES) {
                return SecretKeySpec(keyBytes, "AES")
            }
        }
        val newBytes = generateSecureRandomBytes(PASSPHRASE_LENGTH_BYTES)
        keyFile.writeBytes(newBytes)
        return SecretKeySpec(newBytes, "AES")
    }

    private fun encryptData(secretKey: SecretKey, plaintext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION_AES_GCM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext)
        val result = ByteArray(iv.size + ciphertext.size)
        iv.copyInto(result, destinationOffset = 0)
        ciphertext.copyInto(result, destinationOffset = iv.size)
        return result
    }

    private fun decryptData(secretKey: SecretKey, payload: ByteArray): ByteArray {
        val iv = payload.copyOfRange(0, GCM_IV_LENGTH_BYTES)
        val ciphertext = payload.copyOfRange(GCM_IV_LENGTH_BYTES, payload.size)
        val cipher = Cipher.getInstance(TRANSFORMATION_AES_GCM)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        return cipher.doFinal(ciphertext)
    }

    private fun generateSecureRandomBytes(length: Int): ByteArray {
        val bytes = ByteArray(length)
        SecureRandom().nextBytes(bytes)
        return bytes
    }

    private fun getStorageDirectory(): File {
        val directory = File(System.getProperty("java.io.tmpdir") ?: ".", ".kmpchat_keys")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return directory
    }

    private fun getStorageFile(alias: String): File {
        return File(getStorageDirectory(), "$alias.enc")
    }

    private fun getRawKeyFile(alias: String): File {
        return File(getStorageDirectory(), "$alias.raw")
    }

    private fun readEncryptedData(alias: String): ByteArray? {
        val file = getStorageFile(alias)
        return if (file.exists()) file.readBytes() else null
    }

    private fun writeEncryptedData(alias: String, data: ByteArray) {
        val file = getStorageFile(alias)
        file.writeBytes(data)
    }

    private fun deleteEncryptedFile(alias: String) {
        val file = getStorageFile(alias)
        if (file.exists()) {
            file.delete()
        }
    }

    private fun deleteRawKeyFile(alias: String) {
        val file = getRawKeyFile(alias)
        if (file.exists()) {
            file.delete()
        }
    }
}
