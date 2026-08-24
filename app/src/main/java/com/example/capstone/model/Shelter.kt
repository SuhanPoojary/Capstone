package com.example.capstone.model

data class Shelter(
    val state: String,
    val district: String,
    val name: String,
    val disasterType: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val capacity: Int,
    val inmates: Int,
    val campType: String,
    val year: String,
    val source: String
)
