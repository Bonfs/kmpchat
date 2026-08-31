package com.bonfs.tcc.kmpchat.data.client

import de.connect2x.trixnity.client.CryptoDriverModule
import de.connect2x.trixnity.client.MatrixClient
import de.connect2x.trixnity.client.create
import de.connect2x.trixnity.client.cryptodriver.vodozemac.vodozemac
import de.connect2x.trixnity.clientserverapi.client.MatrixClientAuthProviderData
import de.connect2x.trixnity.clientserverapi.client.SyncState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okio.FileSystem
import okio.Path.Companion.toPath

import com.bonfs.tcc.kmpchat.data.local.DatabaseKeyProvider
import com.bonfs.tcc.kmpchat.data.local.RoomDatabaseFactory

class MatrixClientManager(
    private val keyProvider: DatabaseKeyProvider,
    private val databaseFactory: RoomDatabaseFactory
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val internalClient = MutableStateFlow<MatrixClient?>(null)
    val clientState: StateFlow<MatrixClient?> = internalClient.asStateFlow()

    private val internalSyncState = MutableStateFlow(SyncState.STOPPED)
    val syncState: StateFlow<SyncState> = internalSyncState.asStateFlow()

    val currentClient: MatrixClient?
        get() = internalClient.value

    suspend fun initializeClient(
        databaseDirectory: String,
        mediaDirectory: String,
        keyAlias: String,
        authProviderData: MatrixClientAuthProviderData? = null
    ): MatrixClient {
        closeClient()
        val passphrase = keyProvider.getOrCreatePassphrase(keyAlias)
        val repositories = databaseFactory.createRepositoriesModule(databaseDirectory, passphrase)
        val mediaStore = databaseFactory.createMediaStoreModule(mediaDirectory)
        val cryptoDriver = CryptoDriverModule.vodozemac()

        val clientResult = if (authProviderData != null) {
            MatrixClient.create(
                repositoriesModule = repositories,
                mediaStoreModule = mediaStore,
                cryptoDriverModule = cryptoDriver,
                authProviderData = authProviderData
            )
        } else {
            MatrixClient.create(
                repositoriesModule = repositories,
                mediaStoreModule = mediaStore,
                cryptoDriverModule = cryptoDriver
            )
        }

        val client = clientResult.getOrThrow()
        internalClient.value = client
        observeClientSyncState(client)
        return client
    }

    fun startSync() {
        scope.launch {
            currentClient?.startSync()
        }
    }

    suspend fun stopSync() {
        currentClient?.stopSync()
    }

    suspend fun closeClient() {
        val client = internalClient.value ?: return
        try {
            client.stopSync()
            client.close()
        } finally {
            internalClient.value = null
            internalSyncState.value = SyncState.STOPPED
        }
    }

    suspend fun wipeLocalData(
        databaseDirectory: String,
        mediaDirectory: String,
        keyAlias: String
    ) {
        closeClient()
        keyProvider.clearKey(keyAlias)
        deleteDirectory(databaseDirectory)
        deleteDirectory(mediaDirectory)
    }

    private fun observeClientSyncState(client: MatrixClient) {
        scope.launch {
            client.syncState.collectLatest { state ->
                internalSyncState.value = state
            }
        }
    }

    private fun deleteDirectory(pathString: String) {
        val path = pathString.toPath()
        try {
            if (FileSystem.SYSTEM.exists(path)) {
                FileSystem.SYSTEM.deleteRecursively(path)
            }
        } catch (_: Exception) {
            // Best effort directory cleanup on wipe
        }
    }
}
