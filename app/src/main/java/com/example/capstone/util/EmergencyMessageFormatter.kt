package com.example.capstone.util

object EmergencyMessageFormatter {
    fun manualSos(status: String?, locationLabel: String, coordinatesLabel: String, contacts: String): String {
        return buildMessage(
            reasonLabel = "Status",
            reasonValue = selectedStatusLabel(status),
            locationLabel = locationLabel,
            coordinatesLabel = coordinatesLabel,
            contacts = contacts,
        )
    }

    fun automaticSos(trigger: String, locationLabel: String, coordinatesLabel: String, contacts: String): String {
        return buildMessage(
            reasonLabel = "Trigger",
            reasonValue = trigger,
            locationLabel = locationLabel,
            coordinatesLabel = coordinatesLabel,
            contacts = contacts,
        )
    }

    private fun buildMessage(
        reasonLabel: String,
        reasonValue: String,
        locationLabel: String,
        coordinatesLabel: String,
        contacts: String,
    ): String = buildString {
        append("SOS: I am in danger. ")
        if (reasonValue.isNotBlank()) {
            append("$reasonLabel: $reasonValue. ")
        }
        append("Location: $locationLabel. ")
        
        // Add Google Maps link if coordinates are valid
        if (coordinatesLabel != "Unavailable" && coordinatesLabel.contains(",")) {
            val coords = coordinatesLabel.replace(" ", "")
            append("View location on Google Maps: https://www.google.com/maps/search/?api=1&query=$coords")
        } else {
            append("Coordinates: $coordinatesLabel.")
        }

        if (contacts.isNotBlank()) {
            append(" Emergency contacts: $contacts.")
        }
    }

    private fun selectedStatusLabel(status: String?): String {
        return status?.trim().orEmpty()
    }
}
