package com.example.capstone

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.capstone.data.SafeReadyPreferences
import com.example.capstone.presentation.fragment.MeshDebugFragment

class MeshDebugActivity : AppCompatActivity() {
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        showFragmentIfNeeded()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!SafeReadyPreferences(this).getEmergencyModeEnabled()) {
            Toast.makeText(this, "Emergency Mode is required for mesh tools", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        requestMeshPermissionsIfNeeded()
    }

    private fun requestMeshPermissionsIfNeeded() {
        val permissions = buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
                add(Manifest.permission.BLUETOOTH_ADVERTISE)
            } else {
                add(Manifest.permission.BLUETOOTH)
                add(Manifest.permission.BLUETOOTH_ADMIN)
            }
        }

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isEmpty()) {
            showFragmentIfNeeded()
        } else {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }

    private fun showFragmentIfNeeded() {
        if (supportFragmentManager.findFragmentByTag("mesh_debug") != null) return
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, MeshDebugFragment(), "mesh_debug")
            .commit()
    }
}

