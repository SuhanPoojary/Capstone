package com.example.capstone.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hospitals")
data class HospitalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // e.g., "District Hospital", "Medical College"
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val contact: String = "",
    val emergencyServices: Boolean = true
)
