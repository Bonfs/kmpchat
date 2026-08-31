package com.bonfs.tcc.kmpchat.data.repository

import de.connect2x.trixnity.clientserverapi.client.MatrixClientAuthProviderData
import de.connect2x.trixnity.clientserverapi.client.SyncState
import de.connect2x.trixnity.clientserverapi.client.classicLoginWithPassword
import de.connect2x.trixnity.clientserverapi.model.authentication.IdentifierType
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

import com.bonfs.tcc.kmpchat.data.client.MatrixClientManager
import com.bonfs.tcc.kmpchat.domain.model.HomeserverUnreachableException
import com.bonfs.tcc.kmpchat.domain.model.InvalidCredentialsException
import com.bonfs.tcc.kmpchat.domain.model.LoginCredentials
import com.bonfs.tcc.kmpchat.domain.model.UserSession
import com.bonfs.tcc.kmpchat.domain.repository.AuthRepository

private const val DATABASE_DIR_NAME = "matrix_data"
private const val MEDIA_DIR_NAME = "matrix_media"
private const val KEY_ALIAS_DATABASE = "matrix_db_master_key"

class MatrixAuthRepositoryImpl(
    private val clientManager: MatrixClientManager,
    private val basePath: String = "data"
) : AuthRepository {

    private val internalSession = MutableStateFlow<UserSession?>(null)

    override suspend fun login(credentials: LoginCredentials): UserSession {
        val serverUrl = parseServerUrl(credentials.homeserverUrl)
        val authData = authenticateAgainstServer(serverUrl, credentials)
        val client = clientManager.initializeClient(
            databaseDirectory = "$basePath/$DATABASE_DIR_NAME",
            mediaDirectory = "$basePath/$MEDIA_DIR_NAME",
            keyAlias = KEY_ALIAS_DATABASE,
            authProviderData = authData
        )
        val session = UserSession(
            userId = client.userId.full,
            deviceId = client.deviceId,
            homeserverUrl = credentials.homeserverUrl
        )
        internalSession.value = session
        clientManager.startSync()
        return session
    }

    override suspend fun logout() {
        try {
            clientManager.currentClient?.logout()
        } catch (_: Exception) {
            // Proceed with local wipe even if server revocation times out
        } finally {
            clientManager.wipeLocalData(
                databaseDirectory = "$basePath/$DATABASE_DIR_NAME",
                mediaDirectory = "$basePath/$MEDIA_DIR_NAME",
                keyAlias = KEY_ALIAS_DATABASE
            )
            internalSession.value = null
        }
    }

    override suspend fun getCurrentSession(): UserSession? {
        val client = clientManager.currentClient ?: return internalSession.value
        val session = UserSession(
            userId = client.userId.full,
            deviceId = client.deviceId,
            homeserverUrl = client.baseUrl.toString()
        )
        internalSession.value = session
        return session
    }

    override fun observeSession(): Flow<UserSession?> {
        return internalSession.asStateFlow()
    }

    override fun observeSyncState(): Flow<SyncState> {
        return clientManager.syncState
    }

    private fun parseServerUrl(rawUrl: String): Url {
        return try {
            val formatted = if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) {
                "https://$rawUrl"
            } else {
                rawUrl
            }
            Url(formatted)
        } catch (exception: Exception) {
            throw HomeserverUnreachableException("URL do homeserver inválida", exception)
        }
    }

    private suspend fun authenticateAgainstServer(
        url: Url,
        credentials: LoginCredentials
    ): MatrixClientAuthProviderData {
        return try {
            MatrixClientAuthProviderData.classicLoginWithPassword(
                baseUrl = url,
                identifier = IdentifierType.User(credentials.username),
                password = credentials.password
            ).getOrThrow()
        } catch (exception: Exception) {
            throw InvalidCredentialsException(
                "Falha ao autenticar com o servidor Matrix. Verifique suas credenciais."
            )
        }
    }
}
