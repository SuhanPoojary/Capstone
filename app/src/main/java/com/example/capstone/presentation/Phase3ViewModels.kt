package com.example.capstone.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstone.data.AuthResult
import com.example.capstone.data.SyncStatus
import com.example.capstone.data.repository.AuthRepository
import com.example.capstone.data.SyncRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for managing authentication and cloud sync operations.
 * Handles login, signup, logout, and progress sync UI state.
 */
class AuthSyncViewModel(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
) : ViewModel() {

    private val _authState = MutableLiveData<AuthResult>()
    val authState: LiveData<AuthResult> = _authState

    private val _syncStatus = MutableLiveData<SyncStatus>()
    val syncStatus: LiveData<SyncStatus> = _syncStatus

    private val _isLoggedIn = MutableLiveData(authRepository.isLoggedIn())
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val _currentUserId = MutableLiveData(authRepository.getCurrentUserId())
    val currentUserId: LiveData<String?> = _currentUserId

    private val _currentUserEmail = MutableLiveData<String?>(authRepository.getCurrentUserEmail())
    val currentUserEmail: LiveData<String?> = _currentUserEmail

    /**
     * Attempt to sign up a new user.
     */
    fun signUp(email: String, password: String, username: String, name: String, institution: String) {
        viewModelScope.launch {
            _authState.value = AuthResult.Loading
            try {
                val result = authRepository.signUp(email, password, username, name, institution)
                result.onSuccess {
                    val userId = authRepository.getCurrentUserId()
                    val userEmail = authRepository.getCurrentUserEmail()
                    val isAnon = authRepository.isAnonymous()

                    _authState.value = AuthResult.Success
                    _isLoggedIn.value = true
                    _currentUserId.value = userId
                    _currentUserEmail.value = userEmail
                }.onFailure { e ->
                    _authState.value = AuthResult.Error(e.message ?: "Sign up failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthResult.Error("Sign up error: ${e.message}")
            }
        }
    }

    /**
     * Attempt to log in.
     */
    fun logIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthResult.Loading
            try {
                val result = authRepository.logIn(email, password)
                result.onSuccess {
                    val userId = authRepository.getCurrentUserId()
                    val userEmail = authRepository.getCurrentUserEmail()

                    _authState.value = AuthResult.Success
                    _isLoggedIn.value = true
                    _currentUserId.value = userId
                    _currentUserEmail.value = userEmail

                    // Pull progress from cloud after login
                    pullProgressFromCloud()
                }.onFailure { e ->
                    _authState.value = AuthResult.Error(e.message ?: "Log in failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthResult.Error("Log in error: ${e.message}")
            }
        }
    }

    /**
     * Log out the current user.
     */
    fun logOut() {
        viewModelScope.launch {
            try {
                authRepository.logOut()
                _isLoggedIn.value = false
                _currentUserId.value = null
                _currentUserEmail.value = null
                _authState.value = AuthResult.Success
            } catch (e: Exception) {
                _authState.value = AuthResult.Error("Log out error: ${e.message}")
            }
        }
    }

    /**
     * Sync progress to the cloud.
     */
    fun syncProgressToCloud() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            val status = syncRepository.syncProgressToCloud(userId)
            _syncStatus.value = status
        }
    }

    /**
     * Pull progress from the cloud.
     */
    fun pullProgressFromCloud() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            val status = syncRepository.pullProgressFromCloud(userId)
            _syncStatus.value = status
        }
    }

    /**
     * Perform a full bidirectional sync.
     */
    fun fullSync() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            val status = syncRepository.fullSync(userId)
            _syncStatus.value = status
        }
    }

    /**
     * Check if the current user is an anonymous user.
     */
    fun isAnonymousUser(): Boolean = authRepository.isAnonymous()
}
