package com.example.capstone.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.capstone.data.local.database.entity.ShelterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShelterDao {
    @Query("SELECT * FROM shelters")
    fun getAllShelters(): Flow<List<ShelterEntity>>

    @Query("SELECT * FROM shelters WHERE state = :state")
    fun getSheltersByState(state: String): Flow<List<ShelterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(shelters: List<ShelterEntity>)

    @Query("SELECT COUNT(*) FROM shelters")
    suspend fun getCount(): Int

    @Query("DELETE FROM shelters")
    suspend fun deleteAll()
}
