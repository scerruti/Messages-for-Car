package com.scerruti.messagesforcar.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.scerruti.messagesforcar.data.entity.MessageEntity
import com.scerruti.messagesforcar.data.entity.MessageStatus
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Message operations.
 * Provides automotive-optimized queries for efficient message retrieval.
 */
@Dao
interface MessageDao {
    
    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>>
    
    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId ORDER BY timestamp ASC LIMIT :limit OFFSET :offset")
    suspend fun getMessagesForConversationPaged(conversationId: String, limit: Int, offset: Int): List<MessageEntity>
    
    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): MessageEntity?
    
    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessageForConversation(conversationId: String): MessageEntity?
    
    @Query("SELECT COUNT(*) FROM messages WHERE conversation_id = :conversationId AND status = :status")
    suspend fun getMessageCountByStatus(conversationId: String, status: MessageStatus): Int
    
    @Query("SELECT COUNT(*) FROM messages WHERE conversation_id = :conversationId AND is_from_me = 0 AND read_timestamp IS NULL")
    suspend fun getUnreadMessageCount(conversationId: String): Int
    
    @Query("UPDATE messages SET status = :status WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, status: MessageStatus)
    
    @Query("UPDATE messages SET read_timestamp = :timestamp WHERE conversation_id = :conversationId AND is_from_me = 0 AND read_timestamp IS NULL")
    suspend fun markConversationAsRead(conversationId: String, timestamp: Long)
    
    @Query("DELETE FROM messages WHERE conversation_id = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: String)
    
    @Query("DELETE FROM messages WHERE timestamp < :timestamp")
    suspend fun deleteOldMessages(timestamp: Long)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)
    
    @Update
    suspend fun updateMessage(message: MessageEntity)
    
    @Delete
    suspend fun deleteMessage(message: MessageEntity)
    
    // Automotive-specific queries for safety
    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId AND message_type = 'VOICE_MESSAGE' ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentVoiceMessages(conversationId: String, limit: Int = 10): List<MessageEntity>
    
    @Query("SELECT * FROM messages WHERE status = 'FAILED' ORDER BY timestamp DESC")
    suspend fun getFailedMessages(): List<MessageEntity>
}
