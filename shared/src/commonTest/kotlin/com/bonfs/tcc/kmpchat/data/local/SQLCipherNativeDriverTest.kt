package com.bonfs.tcc.kmpchat.data.local

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.SQLiteStatement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private class FakeSQLiteConnection : SQLiteConnection {
    val executedSqlStatements = mutableListOf<String>()
    var isClosed = false

    override fun prepare(sql: String): SQLiteStatement {
        executedSqlStatements.add(sql)
        return object : SQLiteStatement {
            override fun bindBlob(index: Int, value: ByteArray) {}
            override fun bindDouble(index: Int, value: Double) {}
            override fun bindLong(index: Int, value: Long) {}
            override fun bindNull(index: Int) {}
            override fun bindText(index: Int, value: String) {}
            override fun clearBindings() {}
            override fun close() {}
            override fun getBlob(index: Int): ByteArray = ByteArray(0)
            override fun getColumnCount(): Int = 0
            override fun getColumnName(index: Int): String = ""
            override fun getColumnType(index: Int): Int = 0
            override fun getDouble(index: Int): Double = 0.0
            override fun getLong(index: Int): Long = 0L
            override fun getText(index: Int): String = ""
            override fun isNull(index: Int): Boolean = true
            override fun reset() {}
            override fun step(): Boolean = false
        }
    }

    override fun close() {
        isClosed = true
    }
}

private class FakeSQLiteDriver : SQLiteDriver {
    val openedConnections = mutableListOf<FakeSQLiteConnection>()

    override fun open(fileName: String): SQLiteConnection {
        val connection = FakeSQLiteConnection()
        openedConnections.add(connection)
        return connection
    }
}

class SQLCipherNativeDriverTest {

    @Test
    fun shouldInitializeDriverWithPassphrase() {
        val passphrase = ByteArray(32) { it.toByte() }
        val driver = SQLCipherNativeDriver(passphrase)
        assertNotNull(driver, "SQLCipherNativeDriver should instantiate cleanly with 32-byte passphrase")
    }

    @Test
    fun shouldExecutePragmaKeyOnOpen() {
        val fakeDriver = FakeSQLiteDriver()
        val passphrase = ByteArray(32) { 0x0A.toByte() }
        val cipherDriver = SQLCipherNativeDriver(passphrase, fakeDriver)

        val connection = cipherDriver.open("matrix_encrypted.db")
        assertNotNull(connection)
        assertEquals(1, fakeDriver.openedConnections.size)

        val executedStatements = (connection as FakeSQLiteConnection).executedSqlStatements
        assertTrue(
            executedStatements.any { it.startsWith("PRAGMA key = \"x'") },
            "Driver must execute PRAGMA key upon opening connection"
        )
    }
}
