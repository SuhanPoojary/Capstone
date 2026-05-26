
package com.example.capstone.service

import android.content.Context
import com.example.capstone.data.MeshConnectionState
import com.example.capstone.data.MeshMessage
import com.example.capstone.data.MeshMessageType
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap

/**
 * Nearby Connections transport for SafeReady mesh messaging.
 *
 * Nearby Connections is preferred because it handles discovery and transport
 * without requiring manual BLE state management.
 */
class MeshService(private val context: Context) {

    interface Listener {
        fun onStateChanged(state: MeshConnectionState)
        fun onEndpointDiscovered(deviceId: String, deviceName: String, signalStrength: Int? = null)
        fun onEndpointLost(deviceId: String)
        fun onMessageReceived(message: MeshMessage)
        fun onConnectionError(message: String)
    }

    private val client: ConnectionsClient = Nearby.getConnectionsClient(context)
    private val connectedEndpoints = ConcurrentHashMap.newKeySet<String>()
    private val endpointNames = ConcurrentHashMap<String, String>()
    private var listener: Listener? = null
    private var localDeviceName: String = "SafeReady"
    private var serviceStarted = false

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type != Payload.Type.BYTES) return
            val bytes = payload.asBytes() ?: return
            runCatching { deserializeMessage(bytes) }
                .onSuccess { listener?.onMessageReceived(it) }
                .onFailure { error -> listener?.onConnectionError("Payload parse failed: ${error.message}") }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // No-op for MVP; useful later if we add transfer progress indicators.
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            client.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                connectedEndpoints.add(endpointId)
                emitState()
            } else {
                listener?.onConnectionError("Connection failed: ${result.status.statusCode}")
            }
        }

        override fun onDisconnected(endpointId: String) {
            connectedEndpoints.remove(endpointId)
            endpointNames.remove(endpointId)
            listener?.onEndpointLost(endpointId)
            emitState()
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: com.google.android.gms.nearby.connection.DiscoveredEndpointInfo) {
            endpointNames[endpointId] = info.endpointName
            listener?.onEndpointDiscovered(endpointId, info.endpointName, null)
            client.requestConnection(localDeviceName, endpointId, connectionLifecycleCallback)
        }

        override fun onEndpointLost(endpointId: String) {
            connectedEndpoints.remove(endpointId)
            endpointNames.remove(endpointId)
            listener?.onEndpointLost(endpointId)
            emitState()
        }
    }

    fun start(
        displayName: String,
        listener: Listener,
    ) {
        this.listener = listener
        this.localDeviceName = displayName.take(48).ifBlank { "SafeReady" }
        if (serviceStarted) {
            emitState()
            return
        }

        serviceStarted = true
        startAdvertising()
        startDiscovery()
        emitState()
    }

    fun stop() {
        runCatching { client.stopAdvertising() }
        runCatching { client.stopDiscovery() }
        runCatching { client.stopAllEndpoints() }
        connectedEndpoints.clear()
        endpointNames.clear()
        serviceStarted = false
        emitState()
    }

    fun broadcastMessage(message: MeshMessage): Boolean {
        if (!serviceStarted) {
            listener?.onConnectionError("Mesh service is not started")
            return false
        }

        val payload = Payload.fromBytes(serializeMessage(message))
        if (connectedEndpoints.isEmpty()) {
            listener?.onConnectionError("No nearby devices connected yet")
            return false
        }

        var attempted = false
        connectedEndpoints.forEach { endpointId ->
            attempted = true
            client.sendPayload(endpointId, payload)
                .addOnFailureListener {
                    listener?.onConnectionError("Send failed for ${message.id}: ${it.message}")
                }
        }
        return attempted
    }

    fun getConnectedCount(): Int = connectedEndpoints.size

    fun getConnectedDeviceNames(): List<String> {
        return connectedEndpoints.mapNotNull { endpointNames[it] }.sorted()
    }

    fun isRunning(): Boolean = serviceStarted

    private fun startAdvertising() {
        val options = AdvertisingOptions.Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .build()

        client.startAdvertising(
            localDeviceName,
            context.packageName,
            connectionLifecycleCallback,
            options,
        ).addOnFailureListener { listener?.onConnectionError("Advertising failed: ${it.message}") }
    }

    private fun startDiscovery() {
        val options = DiscoveryOptions.Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .build()

        client.startDiscovery(
            context.packageName,
            endpointDiscoveryCallback,
            options,
        ).addOnFailureListener { listener?.onConnectionError("Discovery failed: ${it.message}") }
    }

    private fun emitState(lastError: String? = null) {
        listener?.onStateChanged(
            MeshConnectionState(
                isAdvertising = serviceStarted,
                isDiscovering = serviceStarted,
                connectedDevices = connectedEndpoints.size,
                nearbyDevices = endpointNames.size,
                lastError = lastError,
            )
        )
    }

    private fun serializeMessage(message: MeshMessage): ByteArray {
        val json = JSONObject().apply {
            put("id", message.id)
            put("senderId", message.senderId)
            put("senderName", message.senderName)
            put("timestamp", message.timestamp)
            put("type", message.type.name)
            put("content", message.content)
            put("signalStrength", message.signalStrength)
            put("ttl", message.ttl)
            put("hopCount", message.hopCount)
            put("expiresAt", message.expiresAt)
            put("acknowledgedBy", JSONArray(message.acknowledgedBy.toList()))
            put("relayedBy", JSONArray(message.relayedBy))
            put("sendStatus", message.sendStatus.name)
            put("retryCount", message.retryCount)
            put("lastAttemptAt", message.lastAttemptAt)
            put("location", message.location?.let { location ->
                JSONObject().apply {
                    put("latitude", location.latitude)
                    put("longitude", location.longitude)
                    put("accuracyMeters", location.accuracyMeters)
                    put("source", location.source.name)
                    put("label", location.label)
                }
            })
        }
        return json.toString().toByteArray(StandardCharsets.UTF_8)
    }

    private fun deserializeMessage(bytes: ByteArray): MeshMessage {
        val json = JSONObject(String(bytes, StandardCharsets.UTF_8))
        val location = json.optJSONObject("location")?.let { locationJson ->
            com.example.capstone.data.MeshLocation(
                latitude = if (locationJson.isNull("latitude")) null else locationJson.optDouble("latitude"),
                longitude = if (locationJson.isNull("longitude")) null else locationJson.optDouble("longitude"),
                accuracyMeters = if (locationJson.isNull("accuracyMeters")) null else locationJson.optDouble("accuracyMeters").toFloat(),
                source = com.example.capstone.data.MeshLocationSource.valueOf(
                    locationJson.optString("source", com.example.capstone.data.MeshLocationSource.UNKNOWN.name)
                ),
                label = locationJson.optString("label").ifBlank { null },
            )
        }

        return MeshMessage(
            id = json.getString("id"),
            senderId = json.getString("senderId"),
            senderName = json.optString("senderName").ifBlank { null },
            timestamp = json.optLong("timestamp", System.currentTimeMillis()),
            type = MeshMessageType.valueOf(json.getString("type")),
            content = json.getString("content"),
            location = location,
            signalStrength = if (json.isNull("signalStrength")) null else json.optInt("signalStrength"),
            ttl = json.optInt("ttl", MeshMessage.DEFAULT_TTL),
            hopCount = json.optInt("hopCount", 0),
            acknowledgedBy = json.optJSONArray("acknowledgedBy")?.toStringList().orEmpty().toSet(),
            relayedBy = json.optJSONArray("relayedBy")?.toStringList().orEmpty(),
            expiresAt = json.optLong("expiresAt", System.currentTimeMillis() + MeshMessage.DEFAULT_TTL_MS),
            sendStatus = runCatching {
                com.example.capstone.data.MeshSendStatus.valueOf(json.optString("sendStatus", com.example.capstone.data.MeshSendStatus.PENDING.name))
            }.getOrDefault(com.example.capstone.data.MeshSendStatus.PENDING),
            retryCount = json.optInt("retryCount", 0),
            lastAttemptAt = if (json.isNull("lastAttemptAt")) null else json.optLong("lastAttemptAt"),
        )
    }

    private fun JSONArray.toStringList(): List<String> = buildList {
        for (index in 0 until length()) {
            add(optString(index))
        }
    }
}

