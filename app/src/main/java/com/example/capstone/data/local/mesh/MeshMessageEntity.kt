package com.example.capstone.data.local.mesh

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.capstone.data.MeshLocation
import com.example.capstone.data.MeshMessageType
import com.example.capstone.data.MeshSendStatus

@Entity(tableName = "mesh_messages")
data class MeshMessageEntity(
    @PrimaryKey val id: String,
    val senderId: String,
    val senderName: String?,
    val timestamp: Long,
    val type: MeshMessageType,
    val content: String,
    val location: MeshLocation?,
    val signalStrength: Int?,
    val ttl: Int,
    val hopCount: Int,
    val acknowledgedBy: Set<String>,
    val relayedBy: List<String>,
    val expiresAt: Long,
    val sendStatus: MeshSendStatus,
    val retryCount: Int,
    val lastAttemptAt: Long?,
)

