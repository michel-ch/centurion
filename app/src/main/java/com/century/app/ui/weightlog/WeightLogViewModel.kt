package com.century.app.ui.weightlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.century.app.data.local.entity.UserProfile
import com.century.app.data.local.entity.WeightLog
import com.century.app.data.repository.CenturyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeightLogViewModel @Inject constructor(
    private val repository: CenturyRepository
) : ViewModel() {

    val profile: StateFlow<UserProfile?> = repository.getProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val weightLogs: StateFlow<List<WeightLog>> = repository.getAllWeightLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _inputWeight = MutableStateFlow("")
    val inputWeight: StateFlow<String> = _inputWeight

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate

    fun updateInputWeight(value: String) {
        _inputWeight.value = value
    }

    fun adjustWeight(delta: Float) {
        val current = _inputWeight.value.toFloatOrNull() ?: profile.value?.bodyWeight ?: 70f
        val new = (current + delta).coerceIn(20f, 500f)
        _inputWeight.value = String.format("%.1f", new)
    }

    fun setDate(millis: Long) {
        _selectedDate.value = millis
    }

    fun logWeight() {
        val weight = _inputWeight.value.toFloatOrNull() ?: return
        val unit = profile.value?.bodyWeightUnit ?: "kg"
        viewModelScope.launch {
            repository.insertWeightLog(
                WeightLog(
                    weight = weight,
                    unit = unit,
                    loggedAt = _selectedDate.value
                )
            )
            _inputWeight.value = ""
        }
    }

    fun deleteLog(log: WeightLog) {
        viewModelScope.launch {
            repository.deleteWeightLog(log)
        }
    }

    fun updateLog(log: WeightLog) {
        viewModelScope.launch {
            repository.updateWeightLog(log)
        }
    }
}
