package com.example.capstone.presentation.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.capstone.data.GlobalLeaderboardEntry
import com.example.capstone.data.LeaderboardType
import com.example.capstone.data.RegionalLeaderboardEntry
import com.example.capstone.data.FriendLeaderboardEntry
import com.example.capstone.data.repository.GlobalLeaderboardRepository
import com.example.capstone.data.repository.RegionalLeaderboardRepository
import com.example.capstone.data.repository.FriendLeaderboardRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for managing leaderboard state and data.
 * 
 * Phase 4B: Real-Time Collaboration
 * 
 * Provides:
 * - Global leaderboard rankings
 * - Regional leaderboard rankings
 * - Friends-only leaderboard rankings
 * - User's rank on various leaderboards
 * - Real-time updates via LiveData
 */
class LeaderboardViewModel(
    application: Application,
    private val globalRepo: GlobalLeaderboardRepository,
    private val regionalRepo: RegionalLeaderboardRepository,
    private val friendRepo: FriendLeaderboardRepository
) : AndroidViewModel(application) {

    private val TAG = "LeaderboardViewModel"
    
    // Global leaderboard
    private val _globalLeaderboard = MutableLiveData<List<GlobalLeaderboardEntry>>()
    val globalLeaderboard: LiveData<List<GlobalLeaderboardEntry>> = _globalLeaderboard
    
    private val _globalRank = MutableLiveData<Int>()
    val globalRank: LiveData<Int> = _globalRank
    
    private val _globalLoading = MutableLiveData(false)
    val globalLoading: LiveData<Boolean> = _globalLoading
    
    // Regional leaderboard
    private val _regionalLeaderboard = MutableLiveData<List<RegionalLeaderboardEntry>>()
    val regionalLeaderboard: LiveData<List<RegionalLeaderboardEntry>> = _regionalLeaderboard
    
    private val _regionalRank = MutableLiveData<Int>()
    val regionalRank: LiveData<Int> = _regionalRank
    
    // Friends leaderboard
    private val _friendsLeaderboard = MutableLiveData<List<FriendLeaderboardEntry>>()
    val friendsLeaderboard: LiveData<List<FriendLeaderboardEntry>> = _friendsLeaderboard
    
    private val _friendsRank = MutableLiveData<Int>()
    val friendsRank: LiveData<Int> = _friendsRank
    
    // Current leaderboard type
    private val _currentType = MutableLiveData(LeaderboardType.GLOBAL_ALL_TIME)
    val currentType: LiveData<LeaderboardType> = _currentType
    
    // Error handling
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /**
     * Load global leaderboard.
     */
    fun loadGlobalLeaderboard(limit: Int = 100) {
        _globalLoading.value = true
        viewModelScope.launch {
            try {
                val result = globalRepo.getGlobalLeaderboard(limit)
                result.onSuccess { entries ->
                    _globalLeaderboard.postValue(entries)
                    _globalLoading.postValue(false)
                    Log.d(TAG, "Loaded global leaderboard: ${entries.size} entries")
                }.onFailure { e ->
                    _error.postValue("Failed to load global leaderboard: ${e.message}")
                    _globalLoading.postValue(false)
                    Log.e(TAG, "Failed to load global leaderboard", e)
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
                _globalLoading.postValue(false)
                Log.e(TAG, "Exception loading global leaderboard", e)
            }
        }
    }

    /**
     * Observe global leaderboard for real-time updates.
     */
    fun observeGlobalLeaderboard(limit: Int = 50) {
        _currentType.value = LeaderboardType.GLOBAL_ALL_TIME
        globalRepo.observeGlobalLeaderboard(limit).observeForever { entries ->
            _globalLeaderboard.postValue(entries)
            Log.d(TAG, "Global leaderboard updated: ${entries.size} entries")
        }
    }

    /**
     * Load user's global rank.
     */
    fun loadUserGlobalRank(userId: String) {
        viewModelScope.launch {
            try {
                val result = globalRepo.getUserGlobalRank(userId)
                result.onSuccess { rank ->
                    _globalRank.postValue(rank)
                    Log.d(TAG, "User $userId global rank: $rank")
                }.onFailure { e ->
                    _error.postValue("Failed to load rank: ${e.message}")
                    Log.e(TAG, "Failed to load user rank", e)
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
                Log.e(TAG, "Exception loading user rank", e)
            }
        }
    }

    /**
     * Load regional leaderboard.
     */
    fun loadRegionalLeaderboard(region: String, limit: Int = 50) {
        _currentType.value = LeaderboardType.REGIONAL_ALL_TIME
        viewModelScope.launch {
            try {
                val result = regionalRepo.getRegionalLeaderboard(region, limit)
                result.onSuccess { entries ->
                    _regionalLeaderboard.postValue(entries)
                    Log.d(TAG, "Loaded regional leaderboard for $region: ${entries.size} entries")
                }.onFailure { e ->
                    _error.postValue("Failed to load regional leaderboard: ${e.message}")
                    Log.e(TAG, "Failed to load regional leaderboard", e)
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
                Log.e(TAG, "Exception loading regional leaderboard", e)
            }
        }
    }

    /**
     * Observe regional leaderboard for real-time updates.
     */
    fun observeRegionalLeaderboard(region: String) {
        _currentType.value = LeaderboardType.REGIONAL_ALL_TIME
        regionalRepo.observeRegionalLeaderboard(region).observeForever { entries ->
            _regionalLeaderboard.postValue(entries)
            Log.d(TAG, "Regional leaderboard for $region updated: ${entries.size} entries")
        }
    }

    /**
     * Load friends leaderboard.
     */
    fun loadFriendsLeaderboard(userId: String, limit: Int = 50) {
        _currentType.value = LeaderboardType.FRIENDS_ALL_TIME
        viewModelScope.launch {
            try {
                val result = friendRepo.getFriendsLeaderboard(userId, limit)
                result.onSuccess { entries ->
                    _friendsLeaderboard.postValue(entries)
                    Log.d(TAG, "Loaded friends leaderboard: ${entries.size} entries")
                }.onFailure { e ->
                    _error.postValue("Failed to load friends leaderboard: ${e.message}")
                    Log.e(TAG, "Failed to load friends leaderboard", e)
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
                Log.e(TAG, "Exception loading friends leaderboard", e)
            }
        }
    }

    /**
     * Observe friends leaderboard for real-time updates.
     */
    fun observeFriendsLeaderboard(userId: String) {
        _currentType.value = LeaderboardType.FRIENDS_ALL_TIME
        friendRepo.observeFriendsLeaderboard(userId).observeForever { entries ->
            _friendsLeaderboard.postValue(entries)
            Log.d(TAG, "Friends leaderboard updated: ${entries.size} entries")
        }
    }

    /**
     * Get user's position on current leaderboard.
     */
    fun getUserPosition(): Int? {
        return globalRank.value ?: regionalRank.value ?: friendsRank.value
    }

    /**
     * Clear error message.
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Get leaderboard type display name.
     */
    fun getLeaderboardTypeName(type: LeaderboardType): String {
        return when (type) {
            LeaderboardType.GLOBAL_ALL_TIME -> "Global Rankings"
            LeaderboardType.GLOBAL_THIS_MONTH -> "This Month"
            LeaderboardType.GLOBAL_THIS_WEEK -> "This Week"
            LeaderboardType.REGIONAL_ALL_TIME -> "Regional"
            LeaderboardType.FRIENDS_ALL_TIME -> "Friends"
            LeaderboardType.DISASTER_SPECIFIC -> "Disaster"
        }
    }
}

