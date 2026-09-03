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

        val clientResult = createClientInstance(repositories, mediaStore, cryptoDriver, authProviderData)
        val client = clientResult.getOrElse { error ->
            if (authProviderData != null) {
                recoverFromStaleSession(databaseDirectory, mediaDirectory, keyAlias, authProviderData)
            } else {
                throw error
            }
        }

        internalClient.value = client
        observeClientSyncState(client)
        return client
    }

    private suspend fun recoverFromStaleSession(
        databaseDirectory: String,
        mediaDirectory: String,
        keyAlias: String,
        authData: MatrixClientAuthProviderData
    ): MatrixClient {
        databaseFactory.deleteStorage(databaseDirectory, mediaDirectory)
        keyProvider.clearKey(keyAlias)
        val freshPassphrase = keyProvider.getOrCreatePassphrase(keyAlias)
        val freshRepos = databaseFactory.createRepositoriesModule(databaseDirectory, freshPassphrase)
        val freshMediaStore = databaseFactory.createMediaStoreModule(mediaDirectory)
        val cryptoDriver = CryptoDriverModule.vodozemac()
        return createClientInstance(freshRepos, freshMediaStore, cryptoDriver, authData).getOrThrow()
    }

    private suspend fun createClientInstance(
        repositories: de.connect2x.trixnity.client.RepositoriesModule,
        mediaStore: de.connect2x.trixnity.client.MediaStoreModule,
        crypto: CryptoDriverModule,
        authData: MatrixClientAuthProviderData?
    ): Result<MatrixClient> {
        return if (authData != null) {
            MatrixClient.create(repositories, mediaStore, crypto, authProviderData = authData)
        } else {
            MatrixClient.create(repositories, mediaStore, crypto)
        }
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
        databaseFactory.deleteStorage(databaseDirectory, mediaDirectory)
    }

    private fun observeClientSyncState(client: MatrixClient) {
        scope.launch {
            client.syncState.collectLatest { state ->
                internalSyncState.value = state
            }
        }
    }
}
