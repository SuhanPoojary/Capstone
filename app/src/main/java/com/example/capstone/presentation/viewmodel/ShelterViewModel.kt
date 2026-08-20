package com.example.capstone.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.capstone.data.repository.ShelterRepository
import kotlinx.coroutines.launch

class ShelterViewModel(private val repository: ShelterRepository) : ViewModel() {

    val allShelters = repository.allShelters.asLiveData()

    init {
        viewModelScope.launch {
            repository.refreshSheltersIfEmpty()
        }
    }

    class Factory(private val repository: ShelterRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ShelterViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ShelterViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
