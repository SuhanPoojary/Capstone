package com.example.capstone.util

import com.example.capstone.data.MeshDevice
import com.example.capstone.data.MeshLocationEstimate
import com.example.capstone.data.MeshLocationSource
import kotlin.math.pow

/**
 * Utility for approximate location estimation when GPS is unavailable.
 *
 * Phase 5 uses this in a conservative way:
 * - RSSI-based distance only when available
 * - Otherwise use last known city/state and device proximity ranking
 * - Multi-hop triangulation remains approximate by design
 */
object LocationEstimationHelper {

    /**
     * Estimate distance from signal strength using a common path-loss formula.
     *
     * Nearby Connections does not expose real RSSI values, so this is mainly
     * useful for BLE fallback or when a device adapter later provides signal data.
     */
    fun estimateDistanceMeters(
        rssi: Int,
        txPower: Int = DEFAULT_TX_POWER,
        pathLossExponent: Double = DEFAULT_PATH_LOSS_EXPONENT,
    ): Float {
        val exponent = (txPower - rssi) / (10.0 * pathLossExponent)
        return 10.0.pow(exponent).toFloat().coerceAtLeast(0.1f)
    }

    fun proximityLabel(distanceMeters: Float?): String {
        if (distanceMeters == null) return "Approximate proximity unavailable"
        return when {
            distanceMeters <= 5f -> "Very close"
            distanceMeters <= 20f -> "Nearby"
            distanceMeters <= 50f -> "In range"
            distanceMeters <= 150f -> "Nearby cluster"
            else -> "Far"
        }
    }

    fun estimateLocationLabel(
        lastKnownRegion: String?,
        devices: List<MeshDevice>,
    ): MeshLocationEstimate {
        val activeDevices = devices.filter { it.isActive }
        val bestSignalDevice = activeDevices.maxByOrNull { it.signalStrength ?: Int.MIN_VALUE }
        val estimatedDistance = bestSignalDevice?.estimatedDistanceMeters
            ?: bestSignalDevice?.signalStrength?.let { estimateDistanceMeters(it) }

        return when {
            estimatedDistance != null -> MeshLocationEstimate(
                label = proximityLabel(estimatedDistance),
                confidencePercent = if (estimatedDistance <= 20f) 70 else 45,
                source = if (bestSignalDevice?.signalStrength != null) MeshLocationSource.RSSI else MeshLocationSource.MULTI_HOP,
                distanceMeters = estimatedDistance,
            )
            !lastKnownRegion.isNullOrBlank() && activeDevices.isNotEmpty() -> MeshLocationEstimate(
                label = "$lastKnownRegion • device cluster nearby",
                confidencePercent = 55,
                source = MeshLocationSource.LAST_KNOWN,
                distanceMeters = null,
            )
            !lastKnownRegion.isNullOrBlank() -> MeshLocationEstimate(
                label = lastKnownRegion,
                confidencePercent = 35,
                source = MeshLocationSource.CITY_STATE,
                distanceMeters = null,
            )
            activeDevices.isNotEmpty() -> MeshLocationEstimate(
                label = "Nearby device cluster detected",
                confidencePercent = 40,
                source = MeshLocationSource.MULTI_HOP,
                distanceMeters = null,
            )
            else -> MeshLocationEstimate(
                label = "Location unavailable",
                confidencePercent = 0,
                source = MeshLocationSource.UNKNOWN,
                distanceMeters = null,
            )
        }
    }

    private const val DEFAULT_TX_POWER = -59
    private const val DEFAULT_PATH_LOSS_EXPONENT = 2.5
}

