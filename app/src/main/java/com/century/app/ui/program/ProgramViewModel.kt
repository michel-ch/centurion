package com.century.app.ui.program

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.century.app.data.local.entity.UserProfile
import com.century.app.data.repository.CenturyRepository
import com.century.app.domain.model.ProgramWeek
import com.century.app.domain.model.TrainingProgramData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgramViewModel @Inject constructor(
    private val repository: CenturyRepository
) : ViewModel() {

    val allWeeks: List<ProgramWeek> = TrainingProgramData.getProgram()

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    private val _completedDays = MutableStateFlow<Set<Pair<Int, Int>>>(emptySet())
    val completedDays: StateFlow<Set<Pair<Int, Int>>> = _completedDays.asStateFlow()

    init {
        loadProfile()
        loadCompletedDays()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            repository.getProfile().collect { userProfile ->
                _profile.value = userProfile
            }
        }
    }

    private fun loadCompletedDays() {
        viewModelScope.launch {
            repository.getCompletedSessions().collect { sessions ->
                _completedDays.value = sessions
                    .map { it.weekNumber to it.dayNumber }
                    .toSet()
            }
        }
    }
}
