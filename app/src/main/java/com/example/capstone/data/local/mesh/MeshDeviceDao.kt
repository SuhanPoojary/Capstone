package com.example.capstone.data.local.mesh

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MeshDeviceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(device: MeshDeviceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(devices: List<MeshDeviceEntity>)

    @Query("SELECT * FROM mesh_devices ORDER BY lastSeen DESC")
    fun observeAll(): Flow<List<MeshDeviceEntity>>

    @Query("SELECT * FROM mesh_devices WHERE isActive = 1 ORDER BY signalStrength DESC")
    fun observeActive(): Flow<List<MeshDeviceEntity>>

    @Query("SELECT * FROM mesh_devices ORDER BY lastSeen DESC")
    suspend fun getAll(): List<MeshDeviceEntity>

    @Query("SELECT * FROM mesh_devices WHERE isActive = 1")
    suspend fun getActive(): List<MeshDeviceEntity>

    @Query("SELECT * FROM mesh_devices WHERE deviceId = :deviceId LIMIT 1")
    suspend fun getById(deviceId: String): MeshDeviceEntity?

    @Query("UPDATE mesh_devices SET isActive = 0 WHERE deviceId = :deviceId")
    suspend fun markInactive(deviceId: String)

    @Query("DELETE FROM mesh_devices")
    suspend fun clearAll()
}
