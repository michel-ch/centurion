package com.century.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.century.app.data.local.entity.UserProfile
import com.century.app.data.repository.CenturyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(
    val name: String = "",
    val bodyWeight: String = "",
    val bodyWeightUnit: String = "kg",
    val height: String = "",
    val heightUnit: String = "cm",
    val heightInches: String = "",
    val age: String = "",
    val gender: String = "",
    val fitnessLevel: String = "",
    val currentMaxPushUps: String = "",
    val goalWeight: String = "",
    val reminderTime: String = "07:00",
    val profilePhotoUri: String? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: CenturyRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError.asStateFlow()

    companion object {
        const val TOTAL_STEPS = 10
    }

    // --- Field update functions ---

    fun updateName(value: String) {
        _state.update { it.copy(name = value) }
    }

    fun updateBodyWeight(value: String) {
        _state.update { it.copy(bodyWeight = value) }
    }

    fun updateBodyWeightUnit(value: String) {
        _state.update { it.copy(bodyWeightUnit = value) }
    }

    fun updateHeight(value: String) {
        _state.update { it.copy(height = value) }
    }

    fun updateHeightUnit(value: String) {
        _state.update { it.copy(heightUnit = value) }
    }

    fun updateHeightInches(value: String) {
        _state.update { it.copy(heightInches = value) }
    }

    fun updateAge(value: String) {
        _state.update { it.copy(age = value) }
    }

    fun updateGender(value: String) {
        _state.update { it.copy(gender = value) }
    }

    fun updateFitnessLevel(value: String) {
        _state.update { it.copy(fitnessLevel = value) }
    }

    fun updateCurrentMaxPushUps(value: String) {
        _state.update { it.copy(currentMaxPushUps = value) }
    }

    fun updateGoalWeight(value: String) {
        _state.update { it.copy(goalWeight = value) }
    }

    fun updateReminderTime(value: String) {
        _state.update { it.copy(reminderTime = value) }
    }

    fun updateProfilePhotoUri(value: String?) {
        _state.update { it.copy(profilePhotoUri = value) }
    }

    // --- Navigation ---

    fun nextStep() {
        if (_currentStep.value < TOTAL_STEPS - 1) {
            _currentStep.update { it + 1 }
        }
    }

    fun prevStep() {
        if (_currentStep.value > 0) {
            _currentStep.update { it - 1 }
        }
    }

    // --- Validation ---

    fun isCurrentStepValid(): Boolean {
        val s = _state.value
        return when (_currentStep.value) {
            0 -> s.name.trim().length in 2..30
            1 -> {
                val w = s.bodyWeight.toFloatOrNull()
                w != null && w > 0f
            }
            2 -> {
                val h = s.height.toFloatOrNull()
                if (s.heightUnit == "cm") {
                    h != null && h > 0f
                } else {
                    val inches = s.heightInches.toIntOrNull() ?: 0
                    h != null && h >= 0f && (h > 0f || inches > 0) && inches in 0..11
                }
            }
            3 -> {
                val a = s.age.toIntOrNull()
                a != null && a in 5..120
            }
            4 -> s.gender.isNotBlank()
            5 -> s.fitnessLevel.isNotBlank()
            6 -> {
                val p = s.currentMaxPushUps.toIntOrNull()
                p != null && p >= 0
            }
            7 -> {
                // Goal weight is optional; if entered it must be valid
                s.goalWeight.isBlank() || (s.goalWeight.toFloatOrNull()?.let { it > 0f } == true)
            }
            8 -> {
                // Reminder time must match HH:mm pattern
                val parts = s.reminderTime.split(":")
                parts.size == 2 &&
                        (parts[0].toIntOrNull()?.let { it in 0..23 } == true) &&
                        (parts[1].toIntOrNull()?.let { it in 0..59 } == true)
            }
            9 -> true // Summary page is always valid
            else -> false
        }
    }

    // --- Persistence ---

    fun saveProfile(onSuccess: () -> Unit) {
        val s = _state.value

        // Final validation
        if (s.name.trim().length !in 2..30 ||
            s.bodyWeight.toFloatOrNull() == null ||
            s.height.toFloatOrNull() == null ||
            s.age.toIntOrNull() == null ||
            s.gender.isBlank() ||
            s.fitnessLevel.isBlank() ||
            s.currentMaxPushUps.toIntOrNull() == null
        ) {
            _saveError.value = "Please fill in all required fields."
            return
        }

        _isSaving.value = true
        _saveError.value = null

        viewModelScope.launch {
            try {
                val profile = UserProfile(
                    name = s.name.trim(),
                    bodyWeight = s.bodyWeight.toFloat(),
                    bodyWeightUnit = s.bodyWeightUnit,
                    height = s.height.toFloat(),
                    heightUnit = s.heightUnit,
                    heightInches = if (s.heightUnit == "ft") (s.heightInches.toIntOrNull() ?: 0) else 0,
                    age = s.age.toInt(),
                    gender = s.gender,
                    fitnessLevel = s.fitnessLevel,
                    currentMaxPushUps = s.currentMaxPushUps.toInt(),
                    goalWeight = s.goalWeight.toFloatOrNull(),
                    reminderTime = s.reminderTime,
                    profilePhotoUri = s.profilePhotoUri
                )
                repository.insertProfile(profile)
                _isSaving.value = false
                onSuccess()
            } catch (e: Exception) {
                _isSaving.value = false
                _saveError.value = "Failed to save profile. Please try again."
            }
        }
    }

    fun clearError() {
        _saveError.value = null
    }
}
