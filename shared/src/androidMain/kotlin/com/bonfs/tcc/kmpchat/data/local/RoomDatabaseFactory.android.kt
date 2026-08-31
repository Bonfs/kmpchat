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

    actual suspend fun createRepositoriesModule(
        databaseDirectory: String,
        passphrase: ByteArray
    ): RepositoriesModule {
        val context = getApplicationContext()
        val databasePath = "$databaseDirectory/matrix_room.db"
        val driver = SQLCipherNativeDriver(passphrase)
        val builder = Room.databaseBuilder(context, TrixnityRoomDatabase::class.java, databasePath)
            .setDriver(driver)
        return RepositoriesModule.room(builder)
    }

    actual suspend fun createMediaStoreModule(
        mediaDirectory: String
    ): MediaStoreModule {
        return MediaStoreModule.okio(mediaDirectory.toPath())
    }
}
