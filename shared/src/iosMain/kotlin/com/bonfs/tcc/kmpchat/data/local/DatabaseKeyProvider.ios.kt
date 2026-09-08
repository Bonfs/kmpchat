package com.bonfs.tcc.kmpchat.data.local

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
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

    private val inMemoryFallback: MutableMap<String, ByteArray> = mutableMapOf()

    actual fun getOrCreatePassphrase(alias: String): ByteArray {
        val existing = loadFromKeychain(alias) ?: inMemoryFallback[alias]
        if (existing != null && existing.size == PASSPHRASE_LENGTH_BYTES) {
            return existing
        }
        val newKey = generateRandomBytes(PASSPHRASE_LENGTH_BYTES)
        saveToKeychain(alias, newKey)
        inMemoryFallback[alias] = newKey
        return newKey
    }

    actual fun clearKey(alias: String) {
        inMemoryFallback.remove(alias)
        val cfQuery = CFDictionaryCreateMutable(null, 0, null, null)
        val serviceStr = CFBridgingRetain(KEYCHAIN_SERVICE)
        val accountStr = CFBridgingRetain(alias)
        try {
            CFDictionarySetValue(cfQuery, kSecClass, kSecClassGenericPassword)
            CFDictionarySetValue(cfQuery, kSecAttrService, serviceStr)
            CFDictionarySetValue(cfQuery, kSecAttrAccount, accountStr)
            SecItemDelete(cfQuery)
        } finally {
            CFBridgingRelease(serviceStr)
            CFBridgingRelease(accountStr)
            CFRelease(cfQuery)
        }
    }

    private fun loadFromKeychain(alias: String): ByteArray? {
        memScoped {
            val cfQuery = CFDictionaryCreateMutable(null, 0, null, null)
            val serviceStr = CFBridgingRetain(KEYCHAIN_SERVICE)
            val accountStr = CFBridgingRetain(alias)
            val result = alloc<CFTypeRefVar>()
            val status = try {
                CFDictionarySetValue(cfQuery, kSecClass, kSecClassGenericPassword)
                CFDictionarySetValue(cfQuery, kSecAttrService, serviceStr)
                CFDictionarySetValue(cfQuery, kSecAttrAccount, accountStr)
                CFDictionarySetValue(cfQuery, kSecReturnData, kCFBooleanTrue)
                CFDictionarySetValue(cfQuery, kSecMatchLimit, kSecMatchLimitOne)
                SecItemCopyMatching(cfQuery, result.ptr)
            } finally {
                CFBridgingRelease(serviceStr)
                CFBridgingRelease(accountStr)
                CFRelease(cfQuery)
            }
            if (status == errSecSuccess && result.value != null) {
                val nsData = CFBridgingRelease(result.value) as NSData
                return nsDataToByteArray(nsData)
            }
            return null
        }
    }

    private fun saveToKeychain(alias: String, key: ByteArray) {
        clearKey(alias)
        val nsData = byteArrayToNsData(key)
        val cfQuery = CFDictionaryCreateMutable(null, 0, null, null)
        val serviceStr = CFBridgingRetain(KEYCHAIN_SERVICE)
        val accountStr = CFBridgingRetain(alias)
        val dataRef = CFBridgingRetain(nsData)
        try {
            CFDictionarySetValue(cfQuery, kSecClass, kSecClassGenericPassword)
            CFDictionarySetValue(cfQuery, kSecAttrService, serviceStr)
            CFDictionarySetValue(cfQuery, kSecAttrAccount, accountStr)
            CFDictionarySetValue(cfQuery, kSecValueData, dataRef)
            CFDictionarySetValue(cfQuery, kSecAttrAccessible, kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly)
            SecItemAdd(cfQuery, null)
        } finally {
            CFBridgingRelease(serviceStr)
            CFBridgingRelease(accountStr)
            CFBridgingRelease(dataRef)
            CFRelease(cfQuery)
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
