package com.example.capstone.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.capstone.data.AuthResult
import com.example.capstone.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _authState = MutableLiveData<AuthResult>()
    val authState: LiveData<AuthResult> = _authState

    fun signUp(email: String, password: String, username: String, name: String, institution: String) {
        _authState.value = AuthResult.Loading
        viewModelScope.launch {
            val result = authRepository.signUp(email, password, username, name, institution)
            result.onSuccess {
                _authState.value = AuthResult.Success
            }.onFailure { e ->
                _authState.value = AuthResult.Error(e.message ?: "Signup failed")
            }
        }
    }

    fun logIn(email: String, password: String) {
        _authState.value = AuthResult.Loading
        viewModelScope.launch {
            val result = authRepository.logIn(email, password)
            result.onSuccess {
                _authState.value = AuthResult.Success
            }.onFailure { e ->
                _authState.value = AuthResult.Error(e.message ?: "Login failed")
            }
        }
    }

    fun logOut() {
        authRepository.logOut()
    }

    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()
}

class AuthViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
