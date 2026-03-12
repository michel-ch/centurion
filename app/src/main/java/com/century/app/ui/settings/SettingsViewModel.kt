package com.century.app.ui.settings

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.century.app.data.local.entity.UserProfile
import com.century.app.data.repository.CenturyRepository
import com.century.app.worker.ReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: CenturyRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val profile: StateFlow<UserProfile?> = repository.getProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            repository.getProfileOnce()?.let { p ->
                ReminderWorker.schedule(context, p.reminderTime, p.reminderEnabled)
            }
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.updateProfile(profile.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun updateRestBetweenSets(seconds: Int) {
        viewModelScope.launch {
            profile.value?.let {
                repository.updateProfile(it.copy(restBetweenSetsSec = seconds, updatedAt = System.currentTimeMillis()))
            }
        }
    }

    fun updateRestBetweenExercises(seconds: Int) {
        viewModelScope.launch {
            profile.value?.let {
                repository.updateProfile(it.copy(restBetweenExercisesSec = seconds, updatedAt = System.currentTimeMillis()))
            }
        }
    }

    fun toggleAutoReduceRest() {
        viewModelScope.launch {
            profile.value?.let {
                repository.updateProfile(it.copy(autoReduceRest = !it.autoReduceRest, updatedAt = System.currentTimeMillis()))
            }
        }
    }

    fun toggleDarkTheme() {
        viewModelScope.launch {
            profile.value?.let {
                repository.updateProfile(it.copy(useDarkTheme = !it.useDarkTheme, updatedAt = System.currentTimeMillis()))
            }
        }
    }

    fun toggleReminder() {
        viewModelScope.launch {
            profile.value?.let {
                val newEnabled = !it.reminderEnabled
                repository.updateProfile(it.copy(reminderEnabled = newEnabled, updatedAt = System.currentTimeMillis()))
                ReminderWorker.schedule(context, it.reminderTime, newEnabled)
            }
        }
    }

    fun updateReminderTime(time: String) {
        viewModelScope.launch {
            profile.value?.let {
                repository.updateProfile(it.copy(reminderTime = time, updatedAt = System.currentTimeMillis()))
                ReminderWorker.schedule(context, time, it.reminderEnabled)
            }
        }
    }

    fun updateName(name: String) {
        viewModelScope.launch {
            profile.value?.let {
                repository.updateProfile(it.copy(name = name, updatedAt = System.currentTimeMillis()))
            }
        }
    }

    fun updateWeight(weight: Float) {
        viewModelScope.launch {
            profile.value?.let {
                repository.updateProfile(it.copy(bodyWeight = weight, updatedAt = System.currentTimeMillis()))
            }
        }
    }

    fun toggleUnits() {
        viewModelScope.launch {
            profile.value?.let { p ->
                if (p.bodyWeightUnit == "kg") {
                    repository.updateProfile(p.copy(
                        bodyWeight = p.bodyWeight * 2.20462f,
                        bodyWeightUnit = "lbs",
                        height = p.heightCm / 30.48f,
                        heightUnit = "ft",
                        goalWeight = p.goalWeight?.let { it * 2.20462f },
                        updatedAt = System.currentTimeMillis()
                    ))
                } else {
                    repository.updateProfile(p.copy(
                        bodyWeight = p.bodyWeight * 0.453592f,
                        bodyWeightUnit = "kg",
                        height = p.heightCm,
                        heightUnit = "cm",
                        goalWeight = p.goalWeight?.let { it * 0.453592f },
                        updatedAt = System.currentTimeMillis()
                    ))
                }
            }
        }
    }

    fun exportData(): Intent? {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.getDefault())
            val fileName = "century_export_${dateFormat.format(Date())}.csv"
            val file = File(context.cacheDir, fileName)

            FileWriter(file).use { writer ->
                writer.write("Type,Date,Value,Unit,Notes\n")

                // Weight logs would need to be loaded synchronously for export
                // This is a simplified export
                val profile = profile.value
                if (profile != null) {
                    writer.write("Profile,${dateFormat.format(Date(profile.createdAt))},${profile.name},,Created\n")
                    writer.write("Weight,,${profile.bodyWeight},${profile.bodyWeightUnit},Current\n")
                    writer.write("Height,,${profile.height},${profile.heightUnit},\n")
                }
            }

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } catch (_: Exception) {
            null
        }
    }

    fun resetProgram() {
        viewModelScope.launch {
            profile.value?.let {
                repository.updateProfile(
                    it.copy(
                        currentDay = 1,
                        programStartDate = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}
