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

data class ChatMessage(
    val id: String,
    val roomId: String,
    val senderId: String,
    val senderName: String?,
    val content: String,
    val timestamp: Long,
    val isOutgoing: Boolean,
    val isEncrypted: Boolean,
    val deliveryStatus: MessageDeliveryStatus
)
