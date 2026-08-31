package com.bonfs.tcc.kmpchat.data.local

import de.connect2x.trixnity.client.MediaStoreModule
import de.connect2x.trixnity.client.RepositoriesModule

expect class RoomDatabaseFactory() {
    suspend fun createRepositoriesModule(
        databaseDirectory: String,
        passphrase: ByteArray
    ): RepositoriesModule

    suspend fun createMediaStoreModule(
        mediaDirectory: String
    ): MediaStoreModule
}
