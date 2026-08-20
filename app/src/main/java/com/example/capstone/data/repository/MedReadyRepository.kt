package com.example.capstone.data.repository

import com.example.capstone.data.MedReadyScanResult
import com.example.capstone.data.SafeReadyPreferences
import com.example.capstone.data.remote.groq.GroqVisionDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MedReadyRepository(
    private val visionDataSource: GroqVisionDataSource,
    private val prefs: SafeReadyPreferences
) {
    suspend fun analyzeKit(imageBytes: ByteArray): MedReadyScanResult = withContext(Dispatchers.IO) {
        val result = visionDataSource.analyzeMedicineKit(imageBytes)
        prefs.saveMedReadyScan(result)
        result
    }

    fun getScanHistory(): List<MedReadyScanResult> = prefs.getMedReadyScans()
}
