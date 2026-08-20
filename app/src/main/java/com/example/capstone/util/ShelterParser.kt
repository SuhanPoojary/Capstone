package com.example.capstone.util

import android.content.Context
import com.example.capstone.model.Shelter
import java.io.BufferedReader
import java.io.InputStreamReader

object ShelterParser {
    fun parseShelters(context: Context): List<Shelter> {
        val shelters = mutableListOf<Shelter>()
        try {
            val inputStream = context.assets.open("shelters.csv")
            val reader = BufferedReader(InputStreamReader(inputStream))
            // Skip header
            reader.readLine()
            
            var line: String? = reader.readLine()
            val regex = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()
            
            while (line != null) {
                val tokens = line.split(regex).map { it.trim().removeSurrounding("\"") }
                if (tokens.size >= 11) {
                    try {
                        val lat = tokens[5].toDoubleOrNull()
                        val lon = tokens[6].toDoubleOrNull()
                        
                        if (lat != null && lon != null) {
                            shelters.add(
                                Shelter(
                                    state = tokens[0],
                                    district = tokens[1],
                                    name = tokens[2],
                                    disasterType = tokens[3],
                                    address = tokens[4],
                                    latitude = lat,
                                    longitude = lon,
                                    capacity = tokens[7].toIntOrNull() ?: 0,
                                    inmates = tokens[8].toIntOrNull() ?: 0,
                                    campType = tokens[9],
                                    year = tokens[10]
                                )
                            )
                        }
                    } catch (e: Exception) {
                        // Skip malformed rows
                    }
                }
                line = reader.readLine()
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return shelters
    }
}
