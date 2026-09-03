package com.bonfs.tcc.kmpchat.data.local

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Room
import de.connect2x.trixnity.client.MediaStoreModule
import de.connect2x.trixnity.client.RepositoriesModule
import de.connect2x.trixnity.client.media.okio.okio
import de.connect2x.trixnity.client.store.repository.room.TrixnityRoomDatabase
import de.connect2x.trixnity.client.store.repository.room.room
import okio.Path.Companion.toPath
import java.io.File

actual class RoomDatabaseFactory actual constructor() {

    @SuppressLint("PrivateApi")
    private fun getApplicationContext(): Context {
        return runCatching {
            val activityThreadClass = Class.forName("android.app.ActivityThread")
            val method = activityThreadClass.getMethod("currentApplication")
            method.invoke(null) as Context
        }.getOrElse {
            throw IllegalStateException("Android Application Context could not be resolved automatically", it)
        }
    }

    private fun getDirectory(context: Context, directory: String): File {
        return if (directory.startsWith("/")) File(directory) else File(context.filesDir, directory)
    }

    private fun ensureDirectoryExists(context: Context, directory: String): File {
        val dir = getDirectory(context, directory)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    actual suspend fun createRepositoriesModule(
        databaseDirectory: String,
        passphrase: ByteArray
    ): RepositoriesModule {
        val context = getApplicationContext()
        val dir = ensureDirectoryExists(context, databaseDirectory)
        val databaseFile = File(dir, "matrix_room.db")
        val driver = SQLCipherNativeDriver(passphrase)
        val builder = Room.databaseBuilder(context, TrixnityRoomDatabase::class.java, databaseFile.absolutePath)
            .setDriver(driver)
        return RepositoriesModule.room(builder)
    }

    actual suspend fun createMediaStoreModule(
        mediaDirectory: String
    ): MediaStoreModule {
        val context = getApplicationContext()
        val dir = ensureDirectoryExists(context, mediaDirectory)
        return MediaStoreModule.okio(dir.absolutePath.toPath())
    }

    actual fun deleteStorage(
        databaseDirectory: String,
        mediaDirectory: String
    ) {
        val context = getApplicationContext()
        val dbDir = getDirectory(context, databaseDirectory)
        val mediaDir = getDirectory(context, mediaDirectory)
        dbDir.deleteRecursively()
        mediaDir.deleteRecursively()
    }
}
