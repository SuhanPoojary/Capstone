package com.example.capstone.data

import java.util.UUID

/**
 * Phase 5: Offline Disaster Mesh Network models.
 *
 * These models keep the mesh layer local-first and resilient.
 * Nearby Connections is the primary transport; RSSI is optional because
 * Nearby does not expose real signal strength like BLE does.
 */

enum class MeshMessageType {
    SOS,
    INFO,
    ALERT,
    ACK
}

enum class MeshSendStatus {
    PENDING,
    SENT,
    RELAYED,
    FAILED,
}

enum class MeshLocationSource {
    GPS,
    LAST_KNOWN,
    RSSI,
    MULTI_HOP,
    CITY_STATE,
    UNKNOWN
}

data class MeshLocation(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Float? = null,
    val source: MeshLocationSource = MeshLocationSource.UNKNOWN,
    val label: String? = null,
)

data class MeshMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderId: String,
    val senderName: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val type: MeshMessageType,
    val content: String,
    val location: MeshLocation? = null,
    val signalStrength: Int? = null,
    val ttl: Int = DEFAULT_TTL,
    val hopCount: Int = 0,
    val acknowledgedBy: Set<String> = emptySet(),
    val relayedBy: List<String> = emptyList(),
    val expiresAt: Long = timestamp + DEFAULT_TTL_MS,
    val sendStatus: MeshSendStatus = MeshSendStatus.PENDING,
    val retryCount: Int = 0,
    val lastAttemptAt: Long? = null,
) {
    fun isExpired(now: Long = System.currentTimeMillis()): Boolean = now >= expiresAt || ttl <= 0

    fun shouldRelay(): Boolean = ttl > 1 && !isExpired()

    fun nextRelay(relayId: String): MeshMessage {
        val nextTtl = (ttl - 1).coerceAtLeast(0)
        return copy(
            ttl = nextTtl,
            hopCount = hopCount + 1,
            relayedBy = relayedBy + relayId,
            signalStrength = signalStrength,
        )
    }

    companion object {
        const val DEFAULT_TTL = 12
        const val DEFAULT_TTL_MS = 30L * 60L * 1000L
    }
}

data class MeshDevice(
    val deviceId: String,
    val deviceName: String,
    val userId: String? = null,
    val lastSeen: Long = System.currentTimeMillis(),
    val signalStrength: Int? = null,
    val estimatedDistanceMeters: Float? = null,
    val isActive: Boolean = true,
)

data class MeshConnectionState(
    val isAdvertising: Boolean = false,
    val isDiscovering: Boolean = false,
    val connectedDevices: Int = 0,
    val nearbyDevices: Int = 0,
    val lastError: String? = null,
)

data class MeshBroadcastSummary(
    val totalMessages: Int = 0,
    val sosMessages: Int = 0,
    val alerts: Int = 0,
    val infos: Int = 0,
    val acknowledgements: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis(),
)

data class MeshLocationEstimate(
    val label: String,
    val confidencePercent: Int,
    val source: MeshLocationSource,
    val distanceMeters: Float? = null,
)

data class MeshTelemetryState(
    val sent: Int = 0,
    val relayed: Int = 0,
    val failed: Int = 0,
    val retried: Int = 0,
    val droppedDuplicate: Int = 0,
    val droppedExpired: Int = 0,
    val lastError: String? = null,
)
