package com.scerruti.messagesforcar.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a message in the database.
 * Follows automotive safety guidelines by storing essential message data locally.
 */
@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversation_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["conversation_id"]),
        Index(value = ["timestamp"]),
        Index(value = ["status"])
    ]
)
data class MessageEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "conversation_id")
    val conversationId: String,
    
    @ColumnInfo(name = "content")
    val content: String,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "sender_id")
    val senderId: String?,
    
    @ColumnInfo(name = "sender_name")
    val senderName: String?,
    
    @ColumnInfo(name = "message_type")
    val messageType: MessageType,
    
    @ColumnInfo(name = "status")
    val status: MessageStatus,
    
    @ColumnInfo(name = "media_uri")
    val mediaUri: String? = null,
    
    @ColumnInfo(name = "media_type")
    val mediaType: String? = null,
    
    @ColumnInfo(name = "is_from_me")
    val isFromMe: Boolean,
    
    @ColumnInfo(name = "read_timestamp")
    val readTimestamp: Long? = null,
    
    @ColumnInfo(name = "delivered_timestamp")
    val deliveredTimestamp: Long? = null
)

/**
 * Enum representing different types of messages.
 * Supports automotive use cases including voice messages.
 */
enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    VOICE_MESSAGE,
    FILE,
    LOCATION,
    CONTACT
}

/**
 * Enum representing message delivery status.
 * Important for automotive scenarios where connectivity may be intermittent.
 */
enum class MessageStatus {
    PENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED
}
