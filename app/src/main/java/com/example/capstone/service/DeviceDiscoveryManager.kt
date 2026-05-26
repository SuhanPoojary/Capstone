package com.example.capstone.service

import com.example.capstone.data.MeshDevice
import com.example.capstone.data.MeshLocationEstimate
import com.example.capstone.data.local.mesh.MeshMessageCache
import com.example.capstone.util.LocationEstimationHelper

/**
 * Tracks nearby devices and maintains a stable, sorted list for the mesh layer.
 *
 * Phase 5 keeps this intentionally light: device discovery state + proximity ranking.
 */
class DeviceDiscoveryManager(
    private val cache: MeshMessageCache,
) {
    fun onDeviceSeen(
        deviceId: String,
        deviceName: String,
        signalStrength: Int? = null,
    ): MeshDevice {
        cache.markDeviceSeen(deviceId, deviceName, signalStrength)
        return cache.getDevices().first { it.deviceId == deviceId }
    }

    fun onDeviceLost(deviceId: String) {
        cache.markDeviceInactive(deviceId)
    }

    fun getNearbyDevices(): List<MeshDevice> {
        return cache.getActiveDevices().sortedWith(
            compareByDescending<MeshDevice> { it.signalStrength ?: Int.MIN_VALUE }
                .thenByDescending { it.lastSeen }
        )
    }

    fun getBestEstimate(lastKnownRegion: String? = null): MeshLocationEstimate {
        return LocationEstimationHelper.estimateLocationLabel(
            lastKnownRegion = lastKnownRegion,
            devices = getNearbyDevices(),
        )
    }

    fun clear() {
        cache.clearAll()
    }
}

