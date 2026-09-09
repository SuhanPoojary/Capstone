package com.example.capstone.model

data class Hospital(
    val name: String,
    val type: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val contact: String = "",
    val emergencyServices: Boolean = true
)
