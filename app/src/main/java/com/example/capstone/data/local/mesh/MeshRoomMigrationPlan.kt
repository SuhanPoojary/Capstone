package com.example.capstone.data.local.mesh

import com.example.capstone.data.MeshMessage
import com.example.capstone.data.MeshSendStatus

/**
 * Phase 5 migration path helper.
 *
 * This file defines a Room-ready storage contract and data mapping from the
 * current SharedPreferences cache. The app still runs on SharedPreferences for
 * now, but this keeps migration deterministic when Room is switched on.
 */
object MeshRoomMigrationPlan {
    const val DATABASE_NAME = "safeready_mesh.db"
    const val TABLE_MESSAGES = "mesh_messages"

    // Room-ready SQL shape used as migration reference.
    const val CREATE_MESSAGES_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS mesh_messages (
            id TEXT NOT NULL PRIMARY KEY,
            senderId TEXT NOT NULL,
            senderName TEXT,
            timestamp INTEGER NOT NULL,
            type TEXT NOT NULL,
            content TEXT NOT NULL,
            ttl INTEGER NOT NULL,
            hopCount INTEGER NOT NULL,
            expiresAt INTEGER NOT NULL,
            acknowledgedByJson TEXT NOT NULL,
            relayedByJson TEXT NOT NULL,
            locationJson TEXT,
            signalStrength INTEGER
        )
    """

    fun toEntities(messages: List<MeshMessage>): List<MeshMessageEntity> {
        return messages.map { message ->
            MeshMessageEntity(
                id = message.id,
                senderId = message.senderId,
                senderName = message.senderName,
                timestamp = message.timestamp,
                type = message.type,
                content = message.content,
                location = message.location,
                signalStrength = message.signalStrength,
                ttl = message.ttl,
                hopCount = message.hopCount,
                acknowledgedBy = message.acknowledgedBy,
                relayedBy = message.relayedBy,
                expiresAt = message.expiresAt,
                sendStatus = message.sendStatus,
                retryCount = message.retryCount,
                lastAttemptAt = message.lastAttemptAt,
            )
        }
    }

    fun toDeviceEntities(devices: List<com.example.capstone.data.MeshDevice>): List<MeshDeviceEntity> {
        return devices.map { device ->
            MeshDeviceEntity(
                deviceId = device.deviceId,
                deviceName = device.deviceName,
                userId = device.userId,
                lastSeen = device.lastSeen,
                signalStrength = device.signalStrength,
                estimatedDistanceMeters = device.estimatedDistanceMeters,
                isActive = device.isActive
            )
        }
    }

    fun fromEntities(rows: List<MeshMessageEntity>): List<MeshMessage> {
        return rows.map { entity ->
            MeshMessage(
                id = entity.id,
                senderId = entity.senderId,
                senderName = entity.senderName,
                timestamp = entity.timestamp,
                type = entity.type,
                content = entity.content,
                location = entity.location,
                signalStrength = entity.signalStrength,
                ttl = entity.ttl,
                hopCount = entity.hopCount,
                acknowledgedBy = entity.acknowledgedBy,
                relayedBy = entity.relayedBy,
                expiresAt = entity.expiresAt,
                sendStatus = entity.sendStatus,
                retryCount = entity.retryCount,
                lastAttemptAt = entity.lastAttemptAt,
            )
        }
    }

    fun asFailed(message: MeshMessage): MeshMessage {
        return message.copy(sendStatus = MeshSendStatus.FAILED)
    }

    fun toDomain(entity: MeshDeviceEntity): com.example.capstone.data.MeshDevice {
        return com.example.capstone.data.MeshDevice(
            deviceId = entity.deviceId,
            deviceName = entity.deviceName,
            userId = entity.userId,
            lastSeen = entity.lastSeen,
            signalStrength = entity.signalStrength,
            estimatedDistanceMeters = entity.estimatedDistanceMeters,
            isActive = entity.isActive
        )
    }
}

