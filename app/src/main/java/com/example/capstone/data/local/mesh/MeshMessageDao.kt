package com.example.capstone.data.local.mesh

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MeshMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(message: MeshMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(messages: List<MeshMessageEntity>)

    @Query("SELECT * FROM mesh_messages ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int = 100): Flow<List<MeshMessageEntity>>

    @Query("SELECT * FROM mesh_messages ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int = 100): List<MeshMessageEntity>

    @Query("SELECT COUNT(*) FROM mesh_messages")
    suspend fun countMessages(): Int

    @Query("SELECT EXISTS(SELECT 1 FROM mesh_messages WHERE id = :messageId)")
    suspend fun hasMessage(messageId: String): Boolean

    @Query("SELECT * FROM mesh_messages WHERE id = :messageId LIMIT 1")
    suspend fun getById(messageId: String): MeshMessageEntity?

    @Query("SELECT * FROM mesh_messages WHERE sendStatus = 'FAILED' ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestFailed(): MeshMessageEntity?

    @Query("DELETE FROM mesh_messages WHERE expiresAt <= :now OR ttl <= 0")
    suspend fun deleteExpired(now: Long)

    @Query("DELETE FROM mesh_messages")
    suspend fun clearAll()
}

