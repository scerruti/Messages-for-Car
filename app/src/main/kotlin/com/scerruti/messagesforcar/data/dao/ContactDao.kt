package com.scerruti.messagesforcar.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.scerruti.messagesforcar.data.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Contact operations.
 * Provides efficient contact lookup for automotive messaging.
 */
@Dao
interface ContactDao {
    
    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>
    
    @Query("SELECT * FROM contacts WHERE id = :contactId")
    suspend fun getContactById(contactId: String): ContactEntity?
    
    @Query("SELECT * FROM contacts WHERE phone_number = :phoneNumber LIMIT 1")
    suspend fun getContactByPhoneNumber(phoneNumber: String): ContactEntity?
    
    @Query("SELECT * FROM contacts WHERE is_favorite = 1 ORDER BY name ASC")
    fun getFavoriteContacts(): Flow<List<ContactEntity>>
    
    @Query("SELECT * FROM contacts WHERE is_blocked = 0 ORDER BY name ASC")
    fun getNonBlockedContacts(): Flow<List<ContactEntity>>
    
    @Query("SELECT * FROM contacts WHERE name LIKE '%' || :query || '%' OR phone_number LIKE '%' || :query || '%' ORDER BY name ASC")
    suspend fun searchContacts(query: String): List<ContactEntity>
    
    @Query("UPDATE contacts SET is_favorite = :favorite WHERE id = :contactId")
    suspend fun setContactFavorite(contactId: String, favorite: Boolean)
    
    @Query("UPDATE contacts SET is_blocked = :blocked WHERE id = :contactId")
    suspend fun setContactBlocked(contactId: String, blocked: Boolean)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactEntity>)
    
    @Update
    suspend fun updateContact(contact: ContactEntity)
    
    @Delete
    suspend fun deleteContact(contact: ContactEntity)
    
    @Query("DELETE FROM contacts WHERE id = :contactId")
    suspend fun deleteContactById(contactId: String)
    
    // Automotive-specific queries
    @Query("SELECT * FROM contacts WHERE is_favorite = 1 AND is_blocked = 0 ORDER BY name ASC LIMIT :limit")
    suspend fun getQuickAccessContacts(limit: Int = 10): List<ContactEntity>
}
