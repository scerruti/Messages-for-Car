package com.scerruti.messagesforcar.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import android.content.Context
import com.scerruti.messagesforcar.data.dao.ContactDao
import com.scerruti.messagesforcar.data.dao.ConversationDao
import com.scerruti.messagesforcar.data.dao.MessageDao
import com.scerruti.messagesforcar.data.entity.ContactEntity
import com.scerruti.messagesforcar.data.entity.ConversationEntity
import com.scerruti.messagesforcar.data.entity.MessageEntity
import com.scerruti.messagesforcar.data.entity.MessageStatus
import com.scerruti.messagesforcar.data.entity.MessageType
import com.scerruti.messagesforcar.data.entity.ConversationType

/**
 * Main Room database for the Messages for Car application.
 * Designed for automotive use with efficient querying and caching.
 */
@Database(
    entities = [
        MessageEntity::class,
        ConversationEntity::class,
        ContactEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MessagesDatabase : RoomDatabase() {
    
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao
    abstract fun contactDao(): ContactDao
    
    companion object {
        @Volatile
        private var INSTANCE: MessagesDatabase? = null
        
        fun getDatabase(context: Context): MessagesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MessagesDatabase::class.java,
                    "messages_database"
                )
                    .enableMultiInstanceInvalidation()
                    .fallbackToDestructiveMigration() // For development - remove in production
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

/**
 * Type converters for Room database to handle enums and complex types.
 */
class Converters {
    
    @TypeConverter
    fun fromMessageType(type: MessageType): String {
        return type.name
    }
    
    @TypeConverter
    fun toMessageType(type: String): MessageType {
        return MessageType.valueOf(type)
    }
    
    @TypeConverter
    fun fromMessageStatus(status: MessageStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toMessageStatus(status: String): MessageStatus {
        return MessageStatus.valueOf(status)
    }
    
    @TypeConverter
    fun fromConversationType(type: ConversationType): String {
        return type.name
    }
    
    @TypeConverter
    fun toConversationType(type: String): ConversationType {
        return ConversationType.valueOf(type)
    }
}
