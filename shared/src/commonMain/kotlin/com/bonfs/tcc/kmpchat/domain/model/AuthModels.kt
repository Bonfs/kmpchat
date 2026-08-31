package com.bonfs.tcc.kmpchat.domain.model

data class UserSession(
    val userId: String,
    val deviceId: String,
    val homeserverUrl: String
)

data class LoginCredentials(
    val homeserverUrl: String,
    val username: String,
    val password: String
)
