package com.example.capstone.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.capstone.data.repository.ShelterRepository
import com.example.capstone.data.repository.HospitalRepository
import kotlinx.coroutines.launch

class ShelterViewModel(
    private val shelterRepository: ShelterRepository,
    private val hospitalRepository: HospitalRepository
) : ViewModel() {

    val allShelters = shelterRepository.allShelters.asLiveData()
    val allHospitals = hospitalRepository.allHospitals.asLiveData()

    init {
        viewModelScope.launch {
            // Launch both refreshes in parallel
            launch { 
                try {
                    shelterRepository.refreshSheltersIfEmpty()
                } catch (e: Exception) {
                    android.util.Log.e("ShelterViewModel", "Shelter refresh failed", e)
                }
            }
            launch { 
                try {
                    hospitalRepository.refreshHospitalsIfEmpty()
                } catch (e: Exception) {
                    android.util.Log.e("ShelterViewModel", "Hospital refresh failed", e)
                }
            }
        }
    }

    fun fetchHospitalsInArea(minLat: Double, minLon: Double, maxLat: Double, maxLon: Double) {
        viewModelScope.launch {
            hospitalRepository.fetchHospitalsInBbox(minLat, minLon, maxLat, maxLon)
        }
    }

    class Factory(
        private val shelterRepository: ShelterRepository,
        private val hospitalRepository: HospitalRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ShelterViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ShelterViewModel(shelterRepository, hospitalRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
