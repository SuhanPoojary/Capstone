package com.example.capstone

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.capstone.data.MeshMessage
import com.example.capstone.data.MeshMessageType
import com.example.capstone.data.SafeReadyPreferences
import com.example.capstone.data.EmergencyRepository
import com.example.capstone.data.repository.MeshRepository
import com.example.capstone.presentation.viewmodel.MeshViewModel
import android.view.animation.AnimationUtils
import com.example.capstone.util.EmergencyMessageFormatter
import com.example.capstone.location.LocationHelper
import com.example.capstone.presentation.adapter.MeshMessageAdapter
import com.example.capstone.service.EmergencyService
import com.example.capstone.util.EmergencySmsHelper
import com.google.android.material.card.MaterialCardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class EmergencyActivity : AppCompatActivity() {
    private lateinit var meshViewModel: MeshViewModel
    private lateinit var emergencyRepository: EmergencyRepository
    private lateinit var prefs: SafeReadyPreferences
    private lateinit var meshAdapter: MeshMessageAdapter
    private var selectedStatus: String? = null
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var cameraManager: CameraManager
    private var torchEnabled = false
    private var torchCameraId: String? = null

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            refreshNetworkStatus()
        }

        override fun onLost(network: Network) {
            refreshNetworkStatus()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            refreshNetworkStatus()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Toast.makeText(this, "Permissions granted. Ready for mesh.", Toast.LENGTH_SHORT).show()
            // We'll call updateUi later in onCreate or just let it handle things
        } else {
            Toast.makeText(this, "Permissions required for SOS Mesh features.", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.SEND_SMS
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            requestPermissionLauncher.launch(missing.toTypedArray())
        }
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level != -1 && scale != -1) {
                val batteryPct = (level / scale.toFloat() * 100).toInt()
                findViewById<TextView>(R.id.batteryStatusValue)?.text = "$batteryPct%"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency)

        checkAndRequestPermissions()

        meshViewModel = ViewModelProvider(this, defaultViewModelProviderFactory)[MeshViewModel::class.java]
        prefs = SafeReadyPreferences(this)
        emergencyRepository = EmergencyRepository(this, prefs, MeshRepository(this))
        prefs.setEmergencyModeEnabled(true)
        connectivityManager = getSystemService(ConnectivityManager::class.java)
        cameraManager = getSystemService(CameraManager::class.java)

        val lastAction = findViewById<TextView>(R.id.emergencyLastAction)
        val sosBtn = findViewById<FrameLayout>(R.id.btnSendSosEmergency)
        val exitBtn = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnExitEmergency)
        
        val meshStatusValue = findViewById<TextView>(R.id.meshStatusValue)
        val gpsStatusValue = findViewById<TextView>(R.id.gpsStatusValue)
        val meshConnectedCountText = findViewById<TextView>(R.id.meshConnectedCountText)
        val rvNearbyDevices = findViewById<RecyclerView>(R.id.rvNearbyDevices)
        val flashlightCard = findViewById<MaterialCardView>(R.id.emergencyFlashlightCard)
        val sheltersCard = findViewById<MaterialCardView>(R.id.emergencySheltersCard)
        val contactsCard = findViewById<MaterialCardView>(R.id.emergencyContactsCard)
        val checklistCard = findViewById<MaterialCardView>(R.id.emergencyChecklistCard)
        
        meshAdapter = MeshMessageAdapter()
        rvNearbyDevices.apply {
            layoutManager = LinearLayoutManager(this@EmergencyActivity)
            adapter = meshAdapter
        }

        // Status cards
        val cardSafe = findViewById<MaterialCardView>(R.id.cardStatusSafe)
        val cardInjured = findViewById<MaterialCardView>(R.id.cardStatusInjured)
        val cardTrapped = findViewById<MaterialCardView>(R.id.cardStatusTrapped)
        val cardFood = findViewById<MaterialCardView>(R.id.cardStatusFood)
        val cardMedical = findViewById<MaterialCardView>(R.id.cardStatusMedical)
        val cardWater = findViewById<MaterialCardView>(R.id.cardStatusWater)

        val statusCards = listOf(cardSafe, cardInjured, cardTrapped, cardFood, cardMedical, cardWater)

        fun selectStatus(card: MaterialCardView, status: String) {
            statusCards.forEach { 
                it.setCardBackgroundColor(getColor(R.color.color_navy_600))
                it.strokeWidth = 0
            }
            card.setCardBackgroundColor(getColor(R.color.color_green_500))
            selectedStatus = status
        }

        cardSafe.setOnClickListener { selectStatus(cardSafe, "Safe") }
        cardInjured.setOnClickListener { selectStatus(cardInjured, "Injured") }
        cardTrapped.setOnClickListener { selectStatus(cardTrapped, "Trapped") }
        cardFood.setOnClickListener { selectStatus(cardFood, "Need Food") }
        cardMedical.setOnClickListener { selectStatus(cardMedical, "Medical Help") }
        cardWater.setOnClickListener { selectStatus(cardWater, "Need Water") }
        flashlightCard.setOnClickListener { toggleFlashlight() }
        sheltersCard.setOnClickListener {
            startActivity(Intent(this, OfflineMapActivity::class.java))
        }
        contactsCard.setOnClickListener {
            startActivity(Intent(this, EmergencyContactsHostActivity::class.java))
        }
        checklistCard.setOnClickListener { showChecklistDialog() }

        fun updateUi() {
            val enabled = prefs.getEmergencyModeEnabled()
            sosBtn.isEnabled = enabled
            
            if (enabled) {
                startService(Intent(this, EmergencyService::class.java))
                meshViewModel.start(prefs.getUserProfile().name)
            } else {
                stopService(Intent(this, EmergencyService::class.java))
                meshViewModel.stop()
            }
        }

        sosBtn.setOnClickListener {
            if (!prefs.getEmergencyModeEnabled()) {
                Toast.makeText(this, "Enable Emergency Mode first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val statusText = selectedStatus?.takeIf { it.isNotBlank() }
            AlertDialog.Builder(this)
                .setTitle("Confirm SOS")
                .setMessage(
                    if (statusText == null) {
                        "Broadcast a danger alert with your last known location to nearby devices?"
                    } else {
                        "Broadcast status '$statusText' with your last known location to nearby devices?"
                    }
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Send") { _, _ ->
                    emergencyRepository.triggerSos(
                        status = selectedStatus,
                        isAutomatic = false
                    ) { sentCount ->
                        if (sentCount > 0) {
                            lastAction.text = "SOS sent to $sentCount saved contacts"
                        }
                    }
                    Toast.makeText(this, "SOS queued", Toast.LENGTH_SHORT).show()
                }
                .show()
        }

        // Exit button closes emergency activity
        exitBtn.setOnClickListener {
            prefs.setEmergencyModeEnabled(false)
            stopService(Intent(this, EmergencyService::class.java))
            finish()
        }

        // start a subtle pulse animation on the large SOS button to indicate broadcasting
        val pulse = AnimationUtils.loadAnimation(this, R.anim.sos_pulse)
        sosBtn.startAnimation(pulse)

        lifecycleScope.launch {
            meshViewModel.actionMessage.collectLatest { m ->
                if (!m.isNullOrBlank()) {
                    lastAction.text = m
                }
            }
        }

        lifecycleScope.launch {
            meshViewModel.telemetry.collectLatest { t ->
                meshStatusValue.text = t.sent.toString()
            }
        }

        lifecycleScope.launch {
            meshViewModel.connectionState.collectLatest { state ->
                meshConnectedCountText.text = "🟢 ${state.connectedDevices} connected"
            }
        }

        lifecycleScope.launch {
            meshViewModel.messages.collectLatest { messages ->
                meshAdapter.submitList(messages)
            }
        }

        refreshNetworkStatus()
        gpsStatusValue.text = if (LocationHelper.hasLocationPermission(this)) "On" else "Off"

        updateUi()
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        runCatching { connectivityManager.registerDefaultNetworkCallback(networkCallback) }
    }

    private fun refreshNetworkStatus() {
        val statusView = findViewById<TextView>(R.id.networkStatusValue)
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        val label = when {
            network == null || capabilities == null -> "Offline"
            else -> {
                val transport = when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> "Bluetooth"
                    else -> "Connected"
                }
                if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) transport else "$transport (no internet)"
            }
        }
        statusView.text = label
    }

    private fun toggleFlashlight() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Toast.makeText(this, "Flashlight is not supported on this device", Toast.LENGTH_SHORT).show()
            return
        }

        val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true &&
                characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
        } ?: cameraManager.cameraIdList.firstOrNull { id ->
            cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }

        if (cameraId == null) {
            Toast.makeText(this, "No flashlight found", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            torchEnabled = !torchEnabled
            torchCameraId = cameraId
            cameraManager.setTorchMode(cameraId, torchEnabled)
            Toast.makeText(this, if (torchEnabled) "Flashlight on" else "Flashlight off", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            torchEnabled = false
            Log.e("EmergencyActivity", "Flashlight toggle failed", e)
            Toast.makeText(this, "Unable to control flashlight", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showChecklistDialog() {
        AlertDialog.Builder(this)
            .setTitle("Emergency checklist")
            .setMessage(
                "1. Move to safety.\n" +
                    "2. Send SOS with your status.\n" +
                    "3. Keep the flashlight ready.\n" +
                    "4. Check emergency contacts."
            )
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(batteryReceiver)
        } catch (_: Exception) {}
        runCatching { torchCameraId?.let { cameraManager.setTorchMode(it, false) } }
        runCatching { connectivityManager.unregisterNetworkCallback(networkCallback) }
    }
}
