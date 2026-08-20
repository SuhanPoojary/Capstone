package com.example.capstone.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.capstone.data.MedReadyScanResult
import com.example.capstone.data.repository.MedReadyRepository
import kotlinx.coroutines.launch

class MedReadyViewModel(private val repository: MedReadyRepository) : ViewModel() {

    private val _scanResult = MutableLiveData<MedReadyScanResult?>()
    val scanResult: LiveData<MedReadyScanResult?> = _scanResult

    private val _scanHistory = MutableLiveData<List<MedReadyScanResult>>()
    val scanHistory: LiveData<List<MedReadyScanResult>> = _scanHistory

    init {
        refreshHistory()
    }

    fun refreshHistory() {
        _scanHistory.value = repository.getScanHistory()
    }

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun analyzeKit(imageBytes: ByteArray) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = repository.analyzeKit(imageBytes)
                _scanResult.value = result
                _scanHistory.value = repository.getScanHistory()
            } catch (e: Exception) {
                _error.value = e.message ?: "Analysis failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearResult() {
        _scanResult.value = null
    }
}

class MedReadyViewModelFactory(private val repository: MedReadyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedReadyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MedReadyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
