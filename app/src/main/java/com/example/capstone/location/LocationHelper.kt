package com.example.capstone.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import java.util.Locale

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    /**
     * Returns the most user-friendly city/place name available for the last known location.
     */
    @SuppressLint("MissingPermission")
    fun fetchCity(
        context: Context,
        onResult: (String?) -> Unit,
    ) {
        if (!hasLocationPermission(context)) {
            onResult(null)
            return
        }

        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (lm == null) {
            onResult(null)
            return
        }

        val providers = lm.getProviders(true)
        val location = providers
            .asSequence()
            .mapNotNull { provider ->
                try {
                    lm.getLastKnownLocation(provider)
                } catch (_: SecurityException) {
                    null
                }
            }
            .maxByOrNull { it.time }

        if (location == null) {
            onResult(null)
            return
        }

        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(location.latitude, location.longitude, 1) { list ->
                    val place = list.firstOrNull()?.locality
                        ?: list.firstOrNull()?.subAdminArea
                        ?: list.firstOrNull()?.adminArea
                    onResult(place)
                }
            } else {
                @Suppress("DEPRECATION")
                val list = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                @Suppress("DEPRECATION")
                val place = list?.firstOrNull()?.locality
                    ?: list?.firstOrNull()?.subAdminArea
                    ?: list?.firstOrNull()?.adminArea
                onResult(place)
            }
        } catch (_: Throwable) {
            onResult(null)
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchState(
        context: Context,
        onResult: (String?) -> Unit,
    ) {
        if (!hasLocationPermission(context)) {
            onResult(null)
            return
        }

        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (lm == null) {
            onResult(null)
            return
        }

        val providers = lm.getProviders(true)
        val location = providers
            .asSequence()
            .mapNotNull { provider ->
                try {
                    lm.getLastKnownLocation(provider)
                } catch (_: SecurityException) {
                    null
                }
            }
            .maxByOrNull { it.time }

        if (location == null) {
            onResult(null)
            return
        }

        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(location.latitude, location.longitude, 1) { list ->
                    val city = list.firstOrNull()?.locality
                        ?: list.firstOrNull()?.subAdminArea
                        ?: list.firstOrNull()?.adminArea
                    onResult(city)
                }
            } else {
                @Suppress("DEPRECATION")
                val list = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                @Suppress("DEPRECATION")
                val city = list?.firstOrNull()?.locality
                    ?: list?.firstOrNull()?.subAdminArea
                    ?: list?.firstOrNull()?.adminArea
                onResult(city)
            }
        } catch (_: Throwable) {
            onResult(null)
        }
    }
}
