package com.bonfs.tcc.kmpchat.data.local

import androidx.room.Room
import de.connect2x.trixnity.client.MediaStoreModule
import de.connect2x.trixnity.client.RepositoriesModule
import de.connect2x.trixnity.client.media.okio.okio
import de.connect2x.trixnity.client.store.repository.room.TrixnityRoomDatabase
import de.connect2x.trixnity.client.store.repository.room.room
import okio.Path.Companion.toPath
import java.io.File

actual class RoomDatabaseFactory actual constructor() {

    private fun getDirectory(directory: String): File {
        return File(directory)
    }

    private fun ensureDirectoryExists(directory: String): File {
        val dir = getDirectory(directory)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    actual suspend fun createRepositoriesModule(
        databaseDirectory: String,
        passphrase: ByteArray
    ): RepositoriesModule {
        val dir = ensureDirectoryExists(databaseDirectory)
        val databaseFile = File(dir, "matrix_room.db")
        val driver = SQLCipherNativeDriver(passphrase)
        val builder = Room.databaseBuilder<TrixnityRoomDatabase>(databaseFile.absolutePath)
            .setDriver(driver)
        return RepositoriesModule.room(builder)
    }

    actual suspend fun createMediaStoreModule(
        mediaDirectory: String
    ): MediaStoreModule {
        val dir = ensureDirectoryExists(mediaDirectory)
        return MediaStoreModule.okio(dir.absolutePath.toPath())
    }

    actual fun deleteStorage(
        databaseDirectory: String,
        mediaDirectory: String
    ) {
        val dbDir = getDirectory(databaseDirectory)
        val mediaDir = getDirectory(mediaDirectory)
        dbDir.deleteRecursively()
        mediaDir.deleteRecursively()
    }
}
