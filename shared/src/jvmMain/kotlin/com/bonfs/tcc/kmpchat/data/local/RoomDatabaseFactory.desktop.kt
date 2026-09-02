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

    private fun resolveDirectory(directory: String): File {
        val dir = File(directory)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    actual suspend fun createRepositoriesModule(
        databaseDirectory: String,
        passphrase: ByteArray
    ): RepositoriesModule {
        val dir = resolveDirectory(databaseDirectory)
        val databaseFile = File(dir, "matrix_room.db")
        val driver = SQLCipherNativeDriver(passphrase)
        val builder = Room.databaseBuilder<TrixnityRoomDatabase>(databaseFile.absolutePath)
            .setDriver(driver)
        return RepositoriesModule.room(builder)
    }

    actual suspend fun createMediaStoreModule(
        mediaDirectory: String
    ): MediaStoreModule {
        val dir = resolveDirectory(mediaDirectory)
        return MediaStoreModule.okio(dir.absolutePath.toPath())
    }

    actual fun deleteStorage(
        databaseDirectory: String,
        mediaDirectory: String
    ) {
        val dbDir = resolveDirectory(databaseDirectory)
        val mediaDir = resolveDirectory(mediaDirectory)
        dbDir.deleteRecursively()
        mediaDir.deleteRecursively()
    }
}
