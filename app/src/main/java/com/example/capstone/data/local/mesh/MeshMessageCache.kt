package com.example.capstone.data.local.mesh

import android.content.Context
import androidx.core.content.edit
import com.example.capstone.data.MeshDevice
import com.example.capstone.data.MeshLocation
import com.example.capstone.data.MeshLocationSource
import com.example.capstone.data.MeshMessage
import com.example.capstone.data.MeshMessageType
import com.example.capstone.data.MeshSendStatus
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Lightweight local mesh cache backed by SharedPreferences.
 *
 * Phase 5 uses this as the first production slice to avoid overengineering.
 * Room can replace this implementation later without changing callers.
 */
class MeshMessageCache(context: Context) {
    private val messagePrefs = context.getSharedPreferences(MESSAGE_PREFS, Context.MODE_PRIVATE)
    private val devicePrefs = context.getSharedPreferences(DEVICE_PREFS, Context.MODE_PRIVATE)

    fun getOrCreateDeviceId(): String {
        val existing = devicePrefs.getString(KEY_DEVICE_ID, null)
        if (!existing.isNullOrBlank()) return existing

        val generated = UUID.randomUUID().toString()
        devicePrefs.edit { putString(KEY_DEVICE_ID, generated) }
        return generated
    }

    fun getDisplayName(fallback: String = "SafeReady User"): String {
        return devicePrefs.getString(KEY_DISPLAY_NAME, null).orEmpty().ifBlank { fallback }
    }

    fun setDisplayName(name: String) {
        devicePrefs.edit { putString(KEY_DISPLAY_NAME, name.trim()) }
    }

    fun saveMessage(message: MeshMessage) {
        val messages = getMessages().toMutableList()
        messages.removeAll { it.id == message.id }
        messages.add(0, message)
        trimMessageList(messages)
        messagePrefs.edit { putString(KEY_MESSAGES, serializeMessages(messages)) }
    }

    fun getMessages(): List<MeshMessage> {
        val raw = messagePrefs.getString(KEY_MESSAGES, null).orEmpty()
        if (raw.isBlank()) return emptyList()

        return runCatching { deserializeMessages(raw) }
            .getOrDefault(emptyList())
            .sortedByDescending { it.timestamp }
    }

    fun getRecentMessages(limit: Int = 50): List<MeshMessage> {
        return getMessages().take(limit)
    }

    fun hasMessage(messageId: String): Boolean {
        return getMessages().any { it.id == messageId }
    }

    fun acknowledgeMessage(messageId: String, deviceId: String) {
        val updated = getMessages().map { message ->
            if (message.id == messageId) {
                message.copy(acknowledgedBy = message.acknowledgedBy + deviceId)
            } else {
                message
            }
        }
        messagePrefs.edit { putString(KEY_MESSAGES, serializeMessages(updated)) }
    }

    fun deleteMessage(messageId: String) {
        val updated = getMessages().filterNot { it.id == messageId }
        messagePrefs.edit { putString(KEY_MESSAGES, serializeMessages(updated)) }
    }

    fun clearExpiredMessages(now: Long = System.currentTimeMillis()) {
        val updated = getMessages().filterNot { it.isExpired(now) }
        messagePrefs.edit { putString(KEY_MESSAGES, serializeMessages(updated)) }
    }

    fun saveDevice(device: MeshDevice) {
        val devices = getDevices().toMutableList()
        devices.removeAll { it.deviceId == device.deviceId }
        devices.add(device)
        trimDeviceList(devices)
        devicePrefs.edit { putString(KEY_DEVICES, serializeDevices(devices)) }
    }

    fun getDevices(): List<MeshDevice> {
        val raw = devicePrefs.getString(KEY_DEVICES, null).orEmpty()
        if (raw.isBlank()) return emptyList()

        return runCatching { deserializeDevices(raw) }
            .getOrDefault(emptyList())
            .sortedByDescending { it.lastSeen }
    }

    fun getActiveDevices(): List<MeshDevice> {
        return getDevices().filter { it.isActive }
    }

    fun markDeviceSeen(deviceId: String, deviceName: String, signalStrength: Int? = null) {
        val existing = getDevices().firstOrNull { it.deviceId == deviceId }
        val updated = MeshDevice(
            deviceId = deviceId,
            deviceName = deviceName,
            userId = existing?.userId,
            lastSeen = System.currentTimeMillis(),
            signalStrength = signalStrength ?: existing?.signalStrength,
            estimatedDistanceMeters = existing?.estimatedDistanceMeters,
            isActive = true,
        )
        saveDevice(updated)
    }

    fun markDeviceInactive(deviceId: String) {
        val updated = getDevices().map { device ->
            if (device.deviceId == deviceId) device.copy(isActive = false, lastSeen = System.currentTimeMillis()) else device
        }
        devicePrefs.edit { putString(KEY_DEVICES, serializeDevices(updated)) }
    }

    fun clearAll() {
        messagePrefs.edit { remove(KEY_MESSAGES) }
        devicePrefs.edit { remove(KEY_DEVICES) }
    }

    private fun trimMessageList(messages: MutableList<MeshMessage>, maxItems: Int = 100) {
        while (messages.size > maxItems) {
            messages.removeAt(messages.lastIndex)
        }
    }

    private fun trimDeviceList(devices: MutableList<MeshDevice>, maxItems: Int = 50) {
        while (devices.size > maxItems) {
            devices.removeAt(devices.lastIndex)
        }
    }

    private fun serializeMessages(messages: List<MeshMessage>): String {
        val array = JSONArray()
        messages.forEach { array.put(messageToJson(it)) }
        return array.toString()
    }

    private fun deserializeMessages(json: String): List<MeshMessage> {
        val array = JSONArray(json)
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(jsonToMessage(item))
            }
        }
    }

    private fun serializeDevices(devices: List<MeshDevice>): String {
        val array = JSONArray()
        devices.forEach { array.put(deviceToJson(it)) }
        return array.toString()
    }

    private fun deserializeDevices(json: String): List<MeshDevice> {
        val array = JSONArray(json)
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(jsonToDevice(item))
            }
        }
    }

    private fun messageToJson(message: MeshMessage): JSONObject = JSONObject().apply {
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
        put("location", message.location?.let { locationToJson(it) })
    }

    private fun jsonToMessage(item: JSONObject): MeshMessage {
        val location = item.optJSONObject("location")?.let { jsonToLocation(it) }
        return MeshMessage(
            id = item.getString("id"),
            senderId = item.getString("senderId"),
            senderName = item.optString("senderName").ifBlank { null },
            timestamp = item.optLong("timestamp", System.currentTimeMillis()),
            type = MeshMessageType.valueOf(item.getString("type")),
            content = item.getString("content"),
            location = location,
            signalStrength = if (item.isNull("signalStrength")) null else item.optInt("signalStrength"),
            ttl = item.optInt("ttl", MeshMessage.DEFAULT_TTL),
            hopCount = item.optInt("hopCount", 0),
            acknowledgedBy = item.optJSONArray("acknowledgedBy")?.toStringList().orEmpty().toSet(),
            relayedBy = item.optJSONArray("relayedBy")?.toStringList().orEmpty(),
            expiresAt = item.optLong("expiresAt", System.currentTimeMillis() + MeshMessage.DEFAULT_TTL_MS),
            sendStatus = runCatching {
                MeshSendStatus.valueOf(item.optString("sendStatus", MeshSendStatus.PENDING.name))
            }.getOrDefault(MeshSendStatus.PENDING),
            retryCount = item.optInt("retryCount", 0),
            lastAttemptAt = if (item.isNull("lastAttemptAt")) null else item.optLong("lastAttemptAt"),
        )
    }

    private fun deviceToJson(device: MeshDevice): JSONObject = JSONObject().apply {
        put("deviceId", device.deviceId)
        put("deviceName", device.deviceName)
        put("userId", device.userId)
        put("lastSeen", device.lastSeen)
        put("signalStrength", device.signalStrength)
        put("estimatedDistanceMeters", device.estimatedDistanceMeters)
        put("isActive", device.isActive)
    }

    private fun jsonToDevice(item: JSONObject): MeshDevice {
        return MeshDevice(
            deviceId = item.getString("deviceId"),
            deviceName = item.getString("deviceName"),
            userId = item.optString("userId").ifBlank { null },
            lastSeen = item.optLong("lastSeen", System.currentTimeMillis()),
            signalStrength = if (item.isNull("signalStrength")) null else item.optInt("signalStrength"),
            estimatedDistanceMeters = if (item.isNull("estimatedDistanceMeters")) null else item.optDouble("estimatedDistanceMeters").toFloat(),
            isActive = item.optBoolean("isActive", true),
        )
    }

    private fun locationToJson(location: MeshLocation): JSONObject = JSONObject().apply {
        put("latitude", location.latitude)
        put("longitude", location.longitude)
        put("accuracyMeters", location.accuracyMeters)
        put("source", location.source.name)
        put("label", location.label)
    }

    private fun jsonToLocation(item: JSONObject): MeshLocation {
        return MeshLocation(
            latitude = if (item.isNull("latitude")) null else item.optDouble("latitude"),
            longitude = if (item.isNull("longitude")) null else item.optDouble("longitude"),
            accuracyMeters = if (item.isNull("accuracyMeters")) null else item.optDouble("accuracyMeters").toFloat(),
            source = MeshLocationSource.valueOf(item.optString("source", MeshLocationSource.UNKNOWN.name)),
            label = item.optString("label").ifBlank { null },
        )
    }

    private fun JSONArray.toStringList(): List<String> = buildList {
        for (index in 0 until length()) {
            add(optString(index))
        }
    }

    companion object {
        private const val MESSAGE_PREFS = "mesh_message_prefs"
        private const val DEVICE_PREFS = "mesh_device_prefs"
        private const val KEY_MESSAGES = "mesh_messages"
        private const val KEY_DEVICES = "mesh_devices"
        private const val KEY_DEVICE_ID = "mesh_device_id"
        private const val KEY_DISPLAY_NAME = "mesh_display_name"
    }
}

