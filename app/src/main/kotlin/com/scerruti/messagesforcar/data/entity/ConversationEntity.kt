package com.scerruti.messagesforcar.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a conversation in the database.
 * Optimized for automotive use with essential metadata for quick access.
 */
@Entity(
    tableName = "conversations",
    indices = [
        Index(value = ["last_message_timestamp"]),
        Index(value = ["is_archived"]),
        Index(value = ["unread_count"])
    ]
)
data class ConversationEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "participants")
    val participants: String, // JSON string of participant IDs
    
    @ColumnInfo(name = "participant_names")
    val participantNames: String, // JSON string of participant names
    
    @ColumnInfo(name = "last_message_id")
    val lastMessageId: String?,
    
    @ColumnInfo(name = "last_message_preview")
    val lastMessagePreview: String?,
    
    @ColumnInfo(name = "last_message_timestamp")
    val lastMessageTimestamp: Long,
    
    @ColumnInfo(name = "unread_count")
    val unreadCount: Int = 0,
    
    @ColumnInfo(name = "is_group")
    val isGroup: Boolean = false,
    
    @ColumnInfo(name = "is_archived")
    val isArchived: Boolean = false,
    
    @ColumnInfo(name = "is_muted")
    val isMuted: Boolean = false,
    
    @ColumnInfo(name = "conversation_type")
    val conversationType: ConversationType = ConversationType.SMS,
    
    @ColumnInfo(name = "avatar_uri")
    val avatarUri: String? = null,
    
    @ColumnInfo(name = "created_timestamp")
    val createdTimestamp: Long,
    
    @ColumnInfo(name = "updated_timestamp")
    val updatedTimestamp: Long
)

/**
 * Enum representing different types of conversations.
 * Supports various messaging platforms that might be integrated.
 */
enum class ConversationType {
    SMS,
    MMS,
    RCS,
    GOOGLE_MESSAGES,
    WHATSAPP,
    TELEGRAM,
    OTHER
}
