package com.example.capstone.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.capstone.data.local.database.entity.HospitalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HospitalDao {
    @Query("SELECT * FROM hospitals")
    fun getAllHospitals(): Flow<List<HospitalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(hospitals: List<HospitalEntity>)

    @Query("SELECT COUNT(*) FROM hospitals")
    suspend fun getCount(): Int

    @Query("DELETE FROM hospitals")
    suspend fun deleteAll()
}
