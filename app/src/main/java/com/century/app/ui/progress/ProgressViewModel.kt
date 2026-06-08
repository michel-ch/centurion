package com.century.app.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.century.app.data.local.entity.PushUpTest
import com.century.app.data.local.entity.UserProfile
import com.century.app.data.local.entity.WeightLog
import com.century.app.data.local.entity.WorkoutSession
import com.century.app.data.repository.CenturyRepository
import com.century.app.domain.model.TrainingProgramData
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

    val completedSessions: StateFlow<List<WorkoutSession>> = repository.getCompletedSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedProgramDays: StateFlow<Int> = completedSessions
        .map { sessions -> distinctProgramDays(sessions).size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalReps: StateFlow<Int> = repository.getTotalReps()
        .map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalCalories: StateFlow<Float> = repository.getTotalCalories()
        .map { it ?: 0f }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val streak: StateFlow<Int> = completedSessions
        .map { sessions -> calculateCurrentStreak(sessions) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val longestStreak: StateFlow<Int> = completedSessions
        .map { sessions -> calculateLongestStreak(sessions) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            repository.getProfile().collect { userProfile ->
                _profile.value = userProfile
            }
        }
    }

    private fun calculateLongestStreak(sessions: List<WorkoutSession>): Int {
        val sortedDays = distinctProgramDays(sessions)
        if (sortedDays.isEmpty()) return 0

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

    private fun calculateCurrentStreak(sessions: List<WorkoutSession>): Int {
        val sortedDays = distinctProgramDays(sessions).asReversed()
        var streak = 0
        var expected = sortedDays.firstOrNull() ?: return 0

        for (day in sortedDays) {
            if (day == expected) {
                streak++
                expected--
            } else break
        }
        return streak
    }

    private fun distinctProgramDays(sessions: List<WorkoutSession>): List<Int> {
        return sessions
            .mapNotNull { session ->
                TrainingProgramData.dayIdFor(session.weekNumber, session.dayNumber)
                    ?.let { TrainingProgramData.absoluteDayFor(it) }
            }
            .distinct()
            .sorted()
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
            "Female" -> 10.8f
            else -> 0f
        }
        return bmi * 1.2f + p.age * 0.23f + genderOffset - 5.4f
    }
}
