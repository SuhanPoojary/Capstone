package com.example.capstone.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.capstone.data.EmergencyContact

object EmergencySmsHelper {
    fun sendToContacts(
        context: Context,
        contacts: List<EmergencyContact>,
        message: String,
    ): Int {
        if (contacts.isEmpty()) return 0
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.w("EmergencySmsHelper", "SEND_SMS permission missing; skipping SMS fallback")
            return 0
        }

        val smsManager = SmsManager.getDefault()
        var sentCount = 0
        contacts.forEach { contact ->
            runCatching {
                val parts = smsManager.divideMessage(message)
                if (parts.size > 1) {
                    smsManager.sendMultipartTextMessage(contact.phone, null, parts, null, null)
                } else {
                    smsManager.sendTextMessage(contact.phone, null, message, null, null)
                }
                sentCount += 1
            }.onFailure { error ->
                Log.e("EmergencySmsHelper", "SMS send failed for ${contact.phone}", error)
            }
        }
        return sentCount
    }
}
