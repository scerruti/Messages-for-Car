package com.scerruti.messagesforcar.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.scerruti.messagesforcar.data.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Conversation operations.
 * Optimized for automotive use cases with quick access to conversation metadata.
 */
@Dao
interface ConversationDao {
    
    @Query("SELECT * FROM conversations WHERE is_archived = 0 ORDER BY last_message_timestamp DESC")
    fun getAllActiveConversations(): Flow<List<ConversationEntity>>
    
    @Query("SELECT * FROM conversations WHERE is_archived = 1 ORDER BY last_message_timestamp DESC")
    fun getArchivedConversations(): Flow<List<ConversationEntity>>
    
    @Query("SELECT * FROM conversations WHERE id = :conversationId")
    suspend fun getConversationById(conversationId: String): ConversationEntity?
    
    @Query("SELECT * FROM conversations WHERE id = :conversationId")
    fun getConversationByIdFlow(conversationId: String): Flow<ConversationEntity?>
    
    @Query("SELECT * FROM conversations WHERE unread_count > 0 AND is_archived = 0 ORDER BY last_message_timestamp DESC")
    fun getConversationsWithUnreadMessages(): Flow<List<ConversationEntity>>
    
    @Query("SELECT COUNT(*) FROM conversations WHERE unread_count > 0 AND is_archived = 0")
    suspend fun getTotalUnreadConversationCount(): Int
    
    @Query("SELECT SUM(unread_count) FROM conversations WHERE is_archived = 0")
    suspend fun getTotalUnreadMessageCount(): Int
    
    @Query("UPDATE conversations SET unread_count = :count WHERE id = :conversationId")
    suspend fun updateUnreadCount(conversationId: String, count: Int)
    
    @Query("UPDATE conversations SET last_message_id = :messageId, last_message_preview = :preview, last_message_timestamp = :timestamp, updated_timestamp = :updatedTimestamp WHERE id = :conversationId")
    suspend fun updateLastMessage(
        conversationId: String,
        messageId: String,
        preview: String,
        timestamp: Long,
        updatedTimestamp: Long
    )
    
    @Query("UPDATE conversations SET is_archived = :archived WHERE id = :conversationId")
    suspend fun setConversationArchived(conversationId: String, archived: Boolean)
    
    @Query("UPDATE conversations SET is_muted = :muted WHERE id = :conversationId")
    suspend fun setConversationMuted(conversationId: String, muted: Boolean)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(conversations: List<ConversationEntity>)
    
    @Update
    suspend fun updateConversation(conversation: ConversationEntity)
    
    @Delete
    suspend fun deleteConversation(conversation: ConversationEntity)
    
    @Query("DELETE FROM conversations WHERE id = :conversationId")
    suspend fun deleteConversationById(conversationId: String)
    
    // Automotive-specific queries
    @Query("SELECT * FROM conversations WHERE is_archived = 0 AND is_muted = 0 ORDER BY last_message_timestamp DESC LIMIT :limit")
    suspend fun getRecentActiveConversations(limit: Int = 5): List<ConversationEntity>
    
    @Query("SELECT * FROM conversations WHERE title LIKE '%' || :query || '%' OR participant_names LIKE '%' || :query || '%'")
    suspend fun searchConversations(query: String): List<ConversationEntity>
}
