package com.century.app.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.century.app.data.local.entity.PushUpTest
import com.century.app.data.local.entity.UserProfile
import com.century.app.data.local.entity.WeightLog
import com.century.app.data.local.entity.WorkoutSession
import com.century.app.data.repository.CenturyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val repository: CenturyRepository
) : ViewModel() {

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    val weightLogs: Flow<List<WeightLog>> = repository.getAllWeightLogs()

    val pushUpTests: Flow<List<PushUpTest>> = repository.getAllPushUpTests()

    val completedSessions: Flow<List<WorkoutSession>> = repository.getCompletedSessions()

    val totalReps: StateFlow<Int> = repository.getTotalReps()
        .map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalCalories: StateFlow<Float> = repository.getTotalCalories()
        .map { it ?: 0f }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak.asStateFlow()

    private val _longestStreak = MutableStateFlow(0)
    val longestStreak: StateFlow<Int> = _longestStreak.asStateFlow()

    init {
        loadProfile()
        loadStreaks()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            repository.getProfile().collect { userProfile ->
                _profile.value = userProfile
            }
        }
    }

    private fun loadStreaks() {
        viewModelScope.launch {
            _streak.value = repository.calculateStreak()
        }
        viewModelScope.launch {
            repository.getCompletedSessions().collect { sessions ->
                _longestStreak.value = calculateLongestStreak(sessions)
            }
        }
    }

    private fun calculateLongestStreak(sessions: List<WorkoutSession>): Int {
        if (sessions.isEmpty()) return 0

        val sortedDays = sessions
            .map { it.weekNumber * 7 + it.dayNumber }
            .distinct()
            .sorted()

        var longest = 1
        var current = 1

        for (i in 1 until sortedDays.size) {
            if (sortedDays[i] == sortedDays[i - 1] + 1) {
                current++
                if (current > longest) longest = current
            } else {
                current = 1
            }
        }
        return longest
    }

    /**
     * Calculate BMI from profile data.
     * Returns null if profile is unavailable.
     */
    fun calculateBmi(): Float? {
        val p = _profile.value ?: return null
        return p.bmi
    }

    /**
     * Calculate weight change from the first weight log to the latest.
     * Positive = gained, negative = lost.
     */
    fun calculateWeightChange(logs: List<WeightLog>): Float? {
        if (logs.size < 2) return null
        val sorted = logs.sortedBy { it.loggedAt }
        return sorted.last().weightKg - sorted.first().weightKg
    }

    /**
     * Estimate body fat percentage using a basic BMI-based formula.
     * Male:   BF% = BMI * 1.2 + age * 0.23 - 5.4
     * Female: BF% = BMI * 1.2 + age * 0.23 + 10.8 - 5.4
     */
    fun estimateBodyFat(): Float? {
        val p = _profile.value ?: return null
        val bmi = p.bmi
        if (bmi <= 0f) return null

        val genderOffset = when (p.gender) {
            "Male" -> 0f
            "Female" -> 10.8f
            else -> 5.4f // midpoint for "Other"
        }
        return bmi * 1.2f + p.age * 0.23f + genderOffset - 5.4f
    }
}
