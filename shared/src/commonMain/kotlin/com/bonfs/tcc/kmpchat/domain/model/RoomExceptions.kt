package com.bonfs.tcc.kmpchat.domain.model

open class MatrixRoomException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

class InvalidMatrixIdException(
    message: String = "ID Matrix inválido. Use o formato @usuario:servidor.org"
) : MatrixRoomException(message)

class RoomCreationException(
    message: String,
    cause: Throwable? = null
) : MatrixRoomException(message, cause)

class MessageSendException(
    message: String,
    cause: Throwable? = null
) : MatrixRoomException(message, cause)

class RoomNotFoundException(
    val roomId: String
) : MatrixRoomException("A sala $roomId não foi encontrada.")

class ClientNotInitializedException(
    message: String = "Cliente Matrix não está inicializado."
) : MatrixRoomException(message)
