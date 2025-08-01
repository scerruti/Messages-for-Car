package com.scerruti.messagesforcar.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a contact in the database.
 * Stores contact information for messaging participants.
 */
@Entity(
    tableName = "contacts",
    indices = [
        Index(value = ["phone_number"], unique = true),
        Index(value = ["name"]),
        Index(value = ["is_favorite"])
    ]
)
data class ContactEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "phone_number")
    val phoneNumber: String,
    
    @ColumnInfo(name = "email")
    val email: String? = null,
    
    @ColumnInfo(name = "avatar_uri")
    val avatarUri: String? = null,
    
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
    
    @ColumnInfo(name = "is_blocked")
    val isBlocked: Boolean = false,
    
    @ColumnInfo(name = "created_timestamp")
    val createdTimestamp: Long,
    
    @ColumnInfo(name = "updated_timestamp")
    val updatedTimestamp: Long
)
