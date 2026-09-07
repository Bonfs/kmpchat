package com.bonfs.tcc.kmpchat.data.local

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.execSQL
import de.connect2x.sqlitenity.bundled.SQLitenityBundledDriver
import de.connect2x.sqlitenity.compat.SQLitenityCompatDriver

class SQLCipherNativeDriver(
    private val passphrase: ByteArray,
    private val delegateDriver: SQLiteDriver = SQLitenityCompatDriver(SQLitenityBundledDriver())
) : SQLiteDriver {

    override fun open(fileName: String): SQLiteConnection {
        val connection = delegateDriver.open(fileName)
//        applyPassphrase(connection, passphrase)
        return connection
    }

    private fun applyPassphrase(connection: SQLiteConnection, passphrase: ByteArray) {
        val hexKey = formatPassphraseToHex(passphrase)
        runCatching {
            connection.execSQL("PRAGMA key = \"x'$hexKey'\";")
        }
    }

    private fun formatPassphraseToHex(passphrase: ByteArray): String {
        return passphrase.joinToString("") { byte ->
            val unsigned = byte.toInt() and 0xFF
            val hex = unsigned.toString(16)
            if (hex.length == 1) "0$hex" else hex
        }
    }
}
