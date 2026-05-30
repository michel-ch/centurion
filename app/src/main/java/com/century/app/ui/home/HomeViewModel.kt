package com.century.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.century.app.data.local.entity.UserProfile
import com.century.app.data.repository.CenturyRepository
import com.century.app.domain.model.ProgramDay
import com.century.app.domain.model.ProgramWeek
import com.century.app.domain.model.TrainingProgramData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: CenturyRepository
) : ViewModel() {

    private val _hasProfile = MutableStateFlow<Boolean?>(null)
    val hasProfile: StateFlow<Boolean?> = _hasProfile.asStateFlow()

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    val totalReps: StateFlow<Int> = repository.getTotalReps()
        .map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val completedCount: StateFlow<Int> = repository.getCompletedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    private val totalDays = TrainingProgramData.totalDays()

    /** Days in the full program (28). Exposed for UI labels. */
    val totalProgramDays: Int get() = totalDays

    /** True once the user has advanced past the final program day. */
    val isProgramComplete: Boolean
        get() = (_profile.value?.currentDay ?: 1) > totalDays

    // Clamp to the valid program range so week/day lookups never fall off the end:
    // a finished program leaves currentDay at totalDays + 1.
    private val effectiveDay: Int
        get() = (_profile.value?.currentDay ?: 1).coerceIn(1, totalDays)

    val weekProgress: StateFlow<Float> = _profile.map { profile ->
        val currentDay = profile?.currentDay ?: 1
        if (currentDay > totalDays) return@map 1f
        val dayInWeek = (currentDay - 1) % 7
        dayInWeek.toFloat() / 7f
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val todayWorkout: ProgramDay
        get() = TrainingProgramData.getDayForProgram(effectiveDay)?.second
            ?: TrainingProgramData.getProgram().first().days.first()

    val currentWeek: ProgramWeek
        get() = TrainingProgramData.getDayForProgram(effectiveDay)?.first
            ?: TrainingProgramData.getProgram().first()

    val currentWeekNumber: Int
        get() = ((effectiveDay - 1) / 7) + 1

    val currentDayInWeek: Int
        get() = ((effectiveDay - 1) % 7) + 1

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _hasProfile.value = repository.hasProfile()
        }

        viewModelScope.launch {
            repository.getProfile().collect { userProfile ->
                _profile.value = userProfile
            }
        }

        viewModelScope.launch {
            _currentStreak.value = repository.calculateStreak()
        }
    }

    fun refreshStreak() {
        viewModelScope.launch {
            _currentStreak.value = repository.calculateStreak()
        }
    }
}
