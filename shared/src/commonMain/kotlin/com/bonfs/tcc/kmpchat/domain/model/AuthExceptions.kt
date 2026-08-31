package com.bonfs.tcc.kmpchat.domain.model

open class MatrixAuthException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

class InvalidCredentialsException(
    message: String = "Credenciais inválidas. Verifique seu usuário e senha."
) : MatrixAuthException(message)

class HomeserverUnreachableException(
    message: String = "Não foi possível conectar ao servidor Matrix especificado.",
    cause: Throwable? = null
) : MatrixAuthException(message, cause)

class SessionExpiredException(
    message: String = "Sua sessão expirou. Faça login novamente."
) : MatrixAuthException(message)

class EmptyFieldException(
    val fieldName: String
) : MatrixAuthException("O campo $fieldName não pode estar vazio.")
