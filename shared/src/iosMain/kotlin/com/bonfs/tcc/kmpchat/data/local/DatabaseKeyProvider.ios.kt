package com.bonfs.tcc.kmpchat.data.local

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecRandomDefault
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import platform.posix.memcpy

private const val KEYCHAIN_SERVICE: String = "com.bonfs.tcc.kmpchat"
private const val PASSPHRASE_LENGTH_BYTES: Int = 32

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual class DatabaseKeyProvider actual constructor() {

    actual fun getOrCreatePassphrase(alias: String): ByteArray {
        val existing = loadFromKeychain(alias)
        if (existing != null && existing.size == PASSPHRASE_LENGTH_BYTES) {
            return existing
        }
        val newKey = generateRandomBytes(PASSPHRASE_LENGTH_BYTES)
        saveToKeychain(alias, newKey)
        return newKey
    }

    actual fun clearKey(alias: String) {
        val query = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to KEYCHAIN_SERVICE,
            kSecAttrAccount to alias
        )
        val cfQuery = CFBridgingRetain(query) as CFDictionaryRef
        try {
            SecItemDelete(cfQuery)
        } finally {
            CFBridgingRelease(cfQuery)
        }
    }

    private fun loadFromKeychain(alias: String): ByteArray? {
        memScoped {
            val query = mapOf<Any?, Any?>(
                kSecClass to kSecClassGenericPassword,
                kSecAttrService to KEYCHAIN_SERVICE,
                kSecAttrAccount to alias,
                kSecReturnData to kCFBooleanTrue,
                kSecMatchLimit to kSecMatchLimitOne
            )
            val result = alloc<CFTypeRefVar>()
            val cfQuery = CFBridgingRetain(query) as CFDictionaryRef
            val status = try {
                SecItemCopyMatching(cfQuery, result.ptr)
            } finally {
                CFBridgingRelease(cfQuery)
            }
            if (status == errSecSuccess && result.value != null) {
                val nsData = CFBridgingRelease(result.value) as NSData
                return nsDataToByteArray(nsData)
            }
            return null
        }
    }

    private fun saveToKeychain(alias: String, key: ByteArray) {
        val nsData = byteArrayToNsData(key)
        val query = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to KEYCHAIN_SERVICE,
            kSecAttrAccount to alias,
            kSecValueData to nsData,
            kSecAttrAccessible to kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
        )
        val cfQuery = CFBridgingRetain(query) as CFDictionaryRef
        try {
            SecItemDelete(cfQuery)
            SecItemAdd(cfQuery, null)
        } finally {
            CFBridgingRelease(cfQuery)
        }
    }

    private fun generateRandomBytes(length: Int): ByteArray {
        val bytes = ByteArray(length)
        bytes.usePinned { pinned ->
            SecRandomCopyBytes(kSecRandomDefault, length.toULong(), pinned.addressOf(0))
        }
        return bytes
    }

    private fun nsDataToByteArray(data: NSData): ByteArray {
        val length = data.length.toInt()
        val bytes = ByteArray(length)
        if (length > 0) {
            bytes.usePinned { pinned ->
                memcpy(pinned.addressOf(0), data.bytes, data.length)
            }
        }
        return bytes
    }

    private fun byteArrayToNsData(bytes: ByteArray): NSData {
        if (bytes.isEmpty()) return NSData()
        bytes.usePinned { pinned ->
            return NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
        }
    }
}
