package com.example.capstone.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shelters")
data class ShelterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val state: String,
    val district: String,
    val name: String,
    val disasterType: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val capacity: Int,
    val inmates: Int,
    val campType: String,
    val year: String,
    val source: String
)
