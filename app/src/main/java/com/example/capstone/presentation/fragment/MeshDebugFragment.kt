package com.example.capstone.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.capstone.R
import com.example.capstone.data.MeshMessage
import com.example.capstone.data.MeshMessageType
import com.example.capstone.presentation.viewmodel.MeshViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MeshDebugFragment : Fragment() {
    private val vm: MeshViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_mesh_debug, container, false)
        val startBtn = root.findViewById<Button>(R.id.btn_start_mesh)
        val stopBtn = root.findViewById<Button>(R.id.btn_stop_mesh)
        val sendBtn = root.findViewById<Button>(R.id.btn_send_sos)
        val clearTelemetryBtn = root.findViewById<Button>(R.id.btn_clear_telemetry)
        val status = root.findViewById<TextView>(R.id.txt_mesh_status)
        val badge = root.findViewById<TextView>(R.id.txt_mesh_badge)
        val summary = root.findViewById<TextView>(R.id.txt_mesh_summary)
        val messageCount = root.findViewById<TextView>(R.id.txt_mesh_messages)
        val telemetry = root.findViewById<TextView>(R.id.txt_mesh_telemetry)
        val migrationPreviewCount = vm.getRoomMigrationPreviewCount()
        summary.text = "Status: idle • Room migration rows: $migrationPreviewCount"

        startBtn.setOnClickListener {
            vm.start(requireContext().getString(R.string.app_name))
            status.text = "Started"
            badge.text = "Starting"
            summary.text = "Status: starting transport"
        }

        stopBtn.setOnClickListener {
            vm.stop()
            status.text = "Stopped"
            badge.text = "Stopped"
            summary.text = "Status: mesh stopped"
        }

        sendBtn.setOnClickListener {
            val pkg = requireContext().packageName
            val msg = MeshMessage(
                senderId = pkg,
                type = MeshMessageType.SOS,
                content = "Test SOS from debug",
            )
            vm.broadcast(msg)
            badge.text = "Broadcasted"
            summary.text = "Status: SOS queued"
        }

        clearTelemetryBtn.setOnClickListener {
            vm.clearTelemetry()
            summary.text = "Status: telemetry cleared"
        }

        root.findViewById<Button>(R.id.btn_resend_failed).setOnClickListener {
            badge.text = "Retrying"
            summary.text = "Status: checking for failed mesh messages..."
            vm.resendLatestFailed()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.connectionState.collect { state ->
                        status.text = if (state.isAdvertising || state.isDiscovering) "Running" else "Idle"
                        badge.text = when {
                            !state.lastError.isNullOrBlank() -> "Error"
                            state.isAdvertising || state.isDiscovering -> "Active"
                            else -> "Idle"
                        }
                        summary.text = buildString {
                            append("Status: ")
                            append(if (state.isAdvertising || state.isDiscovering) "active" else "idle")
                            if (!state.lastError.isNullOrBlank()) {
                                append(" • Error: ")
                                append(state.lastError)
                            }
                            append("\nConnected devices: ")
                            append(state.connectedDevices)
                            append(" • Nearby: ")
                            append(state.nearbyDevices)
                        }
                    }
                }

                launch {
                    vm.messages.collect { messages ->
                        messageCount.text = "Messages: ${messages.size}"
                    }
                }

                launch {
                    vm.telemetry.collect { state ->
                        telemetry.text = "Sent: ${state.sent} • Relayed: ${state.relayed} • Failed: ${state.failed} • Retries: ${state.retried}"
                        if (!state.lastError.isNullOrBlank()) {
                            summary.text = "Status: error observed • ${state.lastError}"
                        }
                    }
                }

                launch {
                    vm.actionMessage.collect { message ->
                        if (!message.isNullOrBlank()) {
                            summary.text = "Status: $message"
                            badge.text = if (message.startsWith("No failed")) "Idle" else "Retry"
                            vm.clearActionMessage()
                        }
                    }
                }
            }
        }

        return root
    }
}


