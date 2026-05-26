package com.example.capstone.data.local.mesh

import androidx.room.TypeConverter
import com.example.capstone.data.MeshLocation
import com.example.capstone.data.MeshLocationSource
import com.example.capstone.data.MeshMessageType
import com.example.capstone.data.MeshSendStatus
import org.json.JSONObject

class MeshRoomConverters {
    @TypeConverter
    fun fromMessageType(type: MeshMessageType): String = type.name

    @TypeConverter
    fun toMessageType(value: String): MeshMessageType {
        return runCatching { MeshMessageType.valueOf(value) }.getOrDefault(MeshMessageType.INFO)
    }

    @TypeConverter
    fun fromSendStatus(status: MeshSendStatus): String = status.name

    @TypeConverter
    fun toSendStatus(value: String): MeshSendStatus {
        return runCatching { MeshSendStatus.valueOf(value) }.getOrDefault(MeshSendStatus.PENDING)
    }

    @TypeConverter
    fun fromStringSet(values: Set<String>): String = values.joinToString("\u001F")

    @TypeConverter
    fun toStringSet(value: String): Set<String> {
        if (value.isBlank()) return emptySet()
        return value.split("\u001F").map { it.trim() }.filter { it.isNotBlank() }.toSet()
    }

    @TypeConverter
    fun fromStringList(values: List<String>): String = values.joinToString("\u001F")

    @TypeConverter
    fun toStringList(value: String): List<String> {
        if (value.isBlank()) return emptyList()
        return value.split("\u001F").map { it.trim() }.filter { it.isNotBlank() }
    }

    @TypeConverter
    fun fromMeshLocation(location: MeshLocation?): String? {
        if (location == null) return null
        return JSONObject().apply {
            put("latitude", location.latitude)
            put("longitude", location.longitude)
            put("accuracyMeters", location.accuracyMeters)
            put("source", location.source.name)
            put("label", location.label)
        }.toString()
    }

    @TypeConverter
    fun toMeshLocation(value: String?): MeshLocation? {
        if (value.isNullOrBlank()) return null
        val json = runCatching { JSONObject(value) }.getOrNull() ?: return null
        return MeshLocation(
            latitude = if (json.isNull("latitude")) null else json.optDouble("latitude"),
            longitude = if (json.isNull("longitude")) null else json.optDouble("longitude"),
            accuracyMeters = if (json.isNull("accuracyMeters")) null else json.optDouble("accuracyMeters").toFloat(),
            source = runCatching {
                MeshLocationSource.valueOf(json.optString("source", MeshLocationSource.UNKNOWN.name))
            }.getOrDefault(MeshLocationSource.UNKNOWN),
            label = json.optString("label").ifBlank { null },
        )
    }
}

