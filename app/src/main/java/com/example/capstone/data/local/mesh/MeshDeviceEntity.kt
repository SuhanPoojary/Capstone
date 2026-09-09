package com.example.capstone.data.local.mesh

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mesh_devices")
data class MeshDeviceEntity(
    @PrimaryKey val deviceId: String,
    val deviceName: String,
    val userId: String?,
    val lastSeen: Long,
    val signalStrength: Int?,
    val estimatedDistanceMeters: Float?,
    val isActive: Boolean,
)
