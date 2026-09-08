package com.bonfs.tcc.kmpchat.domain.model

enum class RoomMembership {
    JOIN,
    INVITE,
    LEAVE,
    BAN
}

data class ChatRoom(
    val id: String,
    val name: String,
    val lastMessage: String?,
    val lastEventTimestamp: Long,
    val unreadCount: Int,
    val isDirect: Boolean,
    val isEncrypted: Boolean,
    val membership: RoomMembership
)

enum class MessageDeliveryStatus {
    SENDING,
    SENT,
    FAILED,
    RECEIVED
}

sealed class EventMessage {
    abstract val id: String
    abstract val roomId: String
    abstract val senderId: String
    abstract val senderName: String?
    abstract val timestamp: Long
    abstract val isOutgoing: Boolean
    abstract val isEncrypted: Boolean
    abstract val deliveryStatus: MessageDeliveryStatus
}

data class ChatMessage(
    override val id: String,
    override val roomId: String,
    override val senderId: String,
    override val senderName: String?,
    val content: String,
    override val timestamp: Long,
    override val isOutgoing: Boolean,
    override val isEncrypted: Boolean,
    override val deliveryStatus: MessageDeliveryStatus
) : EventMessage()

typealias TextEventMessage = ChatMessage

data class UnknownEventMessage(
    override val id: String,
    override val roomId: String,
    override val senderId: String,
    override val senderName: String?,
    val rawType: String? = null,
    override val timestamp: Long,
    override val isOutgoing: Boolean,
    override val isEncrypted: Boolean,
    override val deliveryStatus: MessageDeliveryStatus
) : EventMessage()

typealias UnkownEventMessage = UnknownEventMessage
