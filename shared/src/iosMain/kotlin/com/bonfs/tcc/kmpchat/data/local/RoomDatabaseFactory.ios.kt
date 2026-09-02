package com.bonfs.tcc.kmpchat.data.local

import androidx.room.Room
import de.connect2x.trixnity.client.MediaStoreModule
import de.connect2x.trixnity.client.RepositoriesModule
import de.connect2x.trixnity.client.media.okio.okio
import de.connect2x.trixnity.client.store.repository.room.TrixnityRoomDatabase
import de.connect2x.trixnity.client.store.repository.room.room
import okio.FileSystem
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

actual class RoomDatabaseFactory actual constructor() {

    private fun resolveDirectory(directory: String): String {
        val root = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true).firstOrNull() as? String ?: ""
        val target = if (directory.startsWith("/")) directory else "$root/$directory"
        val okioPath = target.toPath()
        if (!FileSystem.SYSTEM.exists(okioPath)) {
            FileSystem.SYSTEM.createDirectories(okioPath)
        }
        return target
    }

    actual suspend fun createRepositoriesModule(
        databaseDirectory: String,
        passphrase: ByteArray
    ): RepositoriesModule {
        val resolvedDir = resolveDirectory(databaseDirectory)
        val databasePath = "$resolvedDir/matrix_room.db"
        val driver = SQLCipherNativeDriver(passphrase)
        val builder = Room.databaseBuilder<TrixnityRoomDatabase>(databasePath)
            .setDriver(driver)
        return RepositoriesModule.room(builder)
    }

    actual suspend fun createMediaStoreModule(
        mediaDirectory: String
    ): MediaStoreModule {
        val resolvedDir = resolveDirectory(mediaDirectory)
        return MediaStoreModule.okio(resolvedDir.toPath())
    }

    actual fun deleteStorage(
        databaseDirectory: String,
        mediaDirectory: String
    ) {
        val dbDir = resolveDirectory(databaseDirectory).toPath()
        val mediaDir = resolveDirectory(mediaDirectory).toPath()
        try {
            if (FileSystem.SYSTEM.exists(dbDir)) FileSystem.SYSTEM.deleteRecursively(dbDir)
            if (FileSystem.SYSTEM.exists(mediaDir)) FileSystem.SYSTEM.deleteRecursively(mediaDir)
        } catch (_: Exception) {
            // Best effort cleanup
        }
    }
}
