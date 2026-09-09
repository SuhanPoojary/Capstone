package com.example.capstone.service

import com.example.capstone.data.MeshDevice
import com.example.capstone.data.MeshLocationEstimate
import com.example.capstone.data.local.mesh.MeshDeviceDao
import com.example.capstone.data.local.mesh.MeshDeviceEntity
import com.example.capstone.data.local.mesh.MeshRoomMigrationPlan
import com.example.capstone.util.LocationEstimationHelper

/**
 * Tracks nearby devices and maintains a stable, sorted list for the mesh layer.
 *
 * Phase 8: Migrated to Room (MeshDeviceDao) for stabilization.
 */
class DeviceDiscoveryManager(
    private val deviceDao: MeshDeviceDao,
) {
    suspend fun onDeviceSeen(
        deviceId: String,
        deviceName: String,
        signalStrength: Int? = null,
    ): MeshDevice {
        val existing = deviceDao.getById(deviceId)
        val entity = MeshDeviceEntity(
            deviceId = deviceId,
            deviceName = deviceName,
            userId = existing?.userId,
            lastSeen = System.currentTimeMillis(),
            signalStrength = signalStrength ?: existing?.signalStrength,
            estimatedDistanceMeters = existing?.estimatedDistanceMeters,
            isActive = true
        )
        deviceDao.upsert(entity)
        return MeshRoomMigrationPlan.toDomain(entity)
    }

    suspend fun onDeviceLost(deviceId: String) {
        deviceDao.markInactive(deviceId)
    }

    suspend fun getNearbyDevices(): List<MeshDevice> {
        return deviceDao.getActive().map { MeshRoomMigrationPlan.toDomain(it) }.sortedWith(
            compareByDescending<MeshDevice> { it.signalStrength ?: Int.MIN_VALUE }
                .thenByDescending { it.lastSeen }
        )
    }

    suspend fun getBestEstimate(lastKnownRegion: String? = null): MeshLocationEstimate {
        return LocationEstimationHelper.estimateLocationLabel(
            lastKnownRegion = lastKnownRegion,
            devices = getNearbyDevices(),
        )
    }

    suspend fun clear() {
        deviceDao.clearAll()
    }
}
