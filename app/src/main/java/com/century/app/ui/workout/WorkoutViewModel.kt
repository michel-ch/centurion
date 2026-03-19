package com.century.app.ui.workout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.century.app.data.local.entity.ExerciseLog
import com.century.app.data.local.entity.UserProfile
import com.century.app.data.local.entity.WorkoutSession
import com.century.app.data.repository.CenturyRepository
import com.century.app.domain.model.ProgramDay
import com.century.app.domain.model.ProgramExercise
import com.century.app.domain.model.TrainingProgramData
import com.century.app.util.CalorieCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExerciseState(
    val exerciseIndex: Int,
    val setsCompleted: Int = 0,
    val isCompleted: Boolean = false
)

data class TimedExerciseState(
    val exerciseIndex: Int,
    val remainingSeconds: Int = 0,
    val totalSeconds: Int = 0,
    val isRunning: Boolean = false,
    val isFinished: Boolean = false
)

data class WorkoutUiState(
    val isLoading: Boolean = true,
    val weekNumber: Int = 1,
    val dayNumber: Int = 1,
    val dayLabel: String = "",
    val exercises: List<ProgramExercise> = emptyList(),
    val exerciseStates: List<ExerciseState> = emptyList(),
    val timedExerciseStates: Map<Int, TimedExerciseState> = emptyMap(),
    val isResting: Boolean = false,
    val restSeconds: Int = 0,
    val totalRestSeconds: Int = 0,
    val sessionId: Long = 0,
    val startTime: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val completionTime: Long? = null,
    val totalReps: Int = 0,
    val totalDurationMs: Long = 0,
    val estimatedCalories: Float = 0f,
    val totalRestTimeMs: Long = 0,
    val error: String? = null
)

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val repository: CenturyRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private var restTimerJob: Job? = null
    private var timedExerciseJobs: MutableMap<Int, Job> = mutableMapOf()
    private var userProfile: UserProfile? = null
    private var accumulatedRestTimeMs: Long = 0L

    fun loadWorkout(week: Int, day: Int) {
        // Guard: don't reload if already loaded for the same day (prevents orientation reset)
        val current = _uiState.value
        if (!current.isLoading && current.weekNumber == week && current.dayNumber == day
            && current.exercises.isNotEmpty() && current.error == null) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Load user profile for fitness adjustments
                userProfile = repository.getProfileOnce()

                // Get program day from training data
                val program = TrainingProgramData.getProgram()
                val programWeek = program.getOrNull(week - 1)
                val programDay = programWeek?.days?.getOrNull(day - 1)

                if (programDay == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Workout not found for Week $week, Day $day"
                    )
                    return@launch
                }

                // Adjust exercises based on fitness level and week
                val profile = userProfile
                val adjustedExercises = programDay.exercises.map { exercise ->
                    var adjusted = exercise
                    if (profile != null) {
                        adjusted = TrainingProgramData.adjustForFitnessLevel(
                            adjusted,
                            profile.fitnessLevel,
                            profile.currentMaxPushUps
                        )
                        adjusted = TrainingProgramData.adjustRestForWeek(
                            adjusted,
                            week,
                            profile.autoReduceRest
                        )
                    }
                    adjusted
                }

                // Check for existing session or create new one
                val existingSession = repository.getSessionForDay(week, day)
                val sessionId: Long
                val exerciseStates: List<ExerciseState>

                if (existingSession != null && !existingSession.isCompleted) {
                    sessionId = existingSession.id
                    // Restore exercise states from saved exercise logs
                    val savedLogs = repository.getExercisesForSessionOnce(sessionId)
                    exerciseStates = adjustedExercises.mapIndexed { index, exercise ->
                        val savedLog = savedLogs.find { it.exerciseName == exercise.name }
                        ExerciseState(
                            exerciseIndex = index,
                            setsCompleted = savedLog?.completedSets ?: 0,
                            isCompleted = savedLog?.completedAt != null
                        )
                    }
                } else if (existingSession != null && existingSession.isCompleted) {
                    // Already completed, start fresh
                    val session = WorkoutSession(
                        weekNumber = week,
                        dayNumber = day,
                        dayLabel = programDay.label,
                        startedAt = System.currentTimeMillis()
                    )
                    sessionId = repository.insertSession(session)
                    exerciseStates = adjustedExercises.mapIndexed { index, _ ->
                        ExerciseState(exerciseIndex = index)
                    }
                } else {
                    val session = WorkoutSession(
                        weekNumber = week,
                        dayNumber = day,
                        dayLabel = programDay.label,
                        startedAt = System.currentTimeMillis()
                    )
                    sessionId = repository.insertSession(session)
                    exerciseStates = adjustedExercises.mapIndexed { index, _ ->
                        ExerciseState(exerciseIndex = index)
                    }
                }

                // Build timed exercise states
                val timedStates = mutableMapOf<Int, TimedExerciseState>()
                adjustedExercises.forEachIndexed { index, exercise ->
                    if (exercise.isTimed) {
                        val totalSec = parseTimedSeconds(exercise.reps)
                        timedStates[index] = TimedExerciseState(
                            exerciseIndex = index,
                            remainingSeconds = totalSec,
                            totalSeconds = totalSec
                        )
                    }
                }

                _uiState.value = WorkoutUiState(
                    isLoading = false,
                    weekNumber = week,
                    dayNumber = day,
                    dayLabel = programDay.label,
                    exercises = adjustedExercises,
                    exerciseStates = exerciseStates,
                    timedExerciseStates = timedStates,
                    sessionId = sessionId,
                    startTime = System.currentTimeMillis()
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load workout. Please try again."
                )
            }
        }
    }

    fun completeSet(exerciseIndex: Int) {
        val state = _uiState.value
        val exercise = state.exercises.getOrNull(exerciseIndex) ?: return
        val exerciseState = state.exerciseStates.getOrNull(exerciseIndex) ?: return

        if (exerciseState.isCompleted) return

        val newSetsCompleted = exerciseState.setsCompleted + 1
        val isNowComplete = newSetsCompleted >= exercise.sets
        val updatedState = exerciseState.copy(
            setsCompleted = newSetsCompleted,
            isCompleted = isNowComplete
        )

        val updatedStates = state.exerciseStates.toMutableList()
        updatedStates[exerciseIndex] = updatedState
        _uiState.value = state.copy(exerciseStates = updatedStates)

        // Save progress to database
        saveExerciseProgress(exerciseIndex, newSetsCompleted, isNowComplete)

        // Start rest timer if not the last set or if exercise completed start between-exercise rest
        if (isNowComplete) {
            // Check if all exercises are done
            val allDone = updatedStates.all { it.isCompleted }
            if (allDone) {
                finishWorkout()
            } else {
                startRestTimer(exercise.restBetweenExercisesSec)
            }
        } else {
            startRestTimer(exercise.restBetweenSetsSec)
        }
    }

    fun completeExercise(exerciseIndex: Int) {
        val state = _uiState.value
        val exercise = state.exercises.getOrNull(exerciseIndex) ?: return
        val exerciseState = state.exerciseStates.getOrNull(exerciseIndex) ?: return

        if (exerciseState.isCompleted) return

        val updatedState = exerciseState.copy(
            setsCompleted = exercise.sets,
            isCompleted = true
        )

        val updatedStates = state.exerciseStates.toMutableList()
        updatedStates[exerciseIndex] = updatedState
        _uiState.value = state.copy(exerciseStates = updatedStates)

        // Save progress
        saveExerciseProgress(exerciseIndex, exercise.sets, true)

        // Check if all exercises are done
        val allDone = updatedStates.all { it.isCompleted }
        if (allDone) {
            finishWorkout()
        } else {
            startRestTimer(exercise.restBetweenExercisesSec)
        }
    }

    fun uncompleteExercise(exerciseIndex: Int) {
        val state = _uiState.value
        if (state.isCompleted) return
        val exerciseState = state.exerciseStates.getOrNull(exerciseIndex) ?: return

        if (!exerciseState.isCompleted) return

        val updatedState = exerciseState.copy(
            setsCompleted = 0,
            isCompleted = false
        )

        val updatedStates = state.exerciseStates.toMutableList()
        updatedStates[exerciseIndex] = updatedState
        _uiState.value = state.copy(exerciseStates = updatedStates)

        saveExerciseProgress(exerciseIndex, 0, false)
    }

    fun completeMaxTest(exerciseIndex: Int, achievedCount: Int) {
        val state = _uiState.value
        val exercise = state.exercises.getOrNull(exerciseIndex) ?: return
        val exerciseState = state.exerciseStates.getOrNull(exerciseIndex) ?: return
        if (exerciseState.isCompleted) return

        val updatedState = exerciseState.copy(setsCompleted = exercise.sets, isCompleted = true)
        val updatedStates = state.exerciseStates.toMutableList()
        updatedStates[exerciseIndex] = updatedState
        _uiState.value = state.copy(exerciseStates = updatedStates)

        viewModelScope.launch {
            val log = ExerciseLog(
                sessionId = state.sessionId,
                exerciseName = exercise.name,
                illustrationId = exercise.illustrationId,
                targetSets = exercise.sets,
                targetReps = exercise.reps,
                completedSets = exercise.sets,
                completedReps = achievedCount,
                restBetweenSetsSec = exercise.restBetweenSetsSec,
                restBetweenExercisesSec = exercise.restBetweenExercisesSec,
                completedAt = System.currentTimeMillis()
            )
            repository.insertExercise(log)
        }

        val allDone = updatedStates.all { it.isCompleted }
        if (allDone) {
            finishWorkout()
        } else {
            startRestTimer(exercise.restBetweenExercisesSec)
        }
    }

    fun skipRest() {
        restTimerJob?.cancel()
        restTimerJob = null
        _uiState.value = _uiState.value.copy(
            isResting = false,
            restSeconds = 0,
            totalRestSeconds = 0
        )
    }

    fun startTimedExercise(exerciseIndex: Int) {
        val state = _uiState.value
        val timedState = state.timedExerciseStates[exerciseIndex] ?: return
        if (timedState.isRunning || timedState.isFinished) return

        timedExerciseJobs[exerciseIndex]?.cancel()
        val job = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                timedExerciseStates = _uiState.value.timedExerciseStates.toMutableMap().apply {
                    put(exerciseIndex, timedState.copy(isRunning = true))
                }
            )

            var remaining = timedState.remainingSeconds
            while (remaining > 0) {
                delay(1000L)
                remaining--
                _uiState.value = _uiState.value.copy(
                    timedExerciseStates = _uiState.value.timedExerciseStates.toMutableMap().apply {
                        put(exerciseIndex, get(exerciseIndex)!!.copy(remainingSeconds = remaining))
                    }
                )
            }

            // Timer finished
            _uiState.value = _uiState.value.copy(
                timedExerciseStates = _uiState.value.timedExerciseStates.toMutableMap().apply {
                    put(
                        exerciseIndex,
                        get(exerciseIndex)!!.copy(
                            isRunning = false,
                            isFinished = true,
                            remainingSeconds = 0
                        )
                    )
                }
            )
        }
        timedExerciseJobs[exerciseIndex] = job
    }

    fun pauseTimedExercise(exerciseIndex: Int) {
        timedExerciseJobs[exerciseIndex]?.cancel()
        timedExerciseJobs.remove(exerciseIndex)

        val timedState = _uiState.value.timedExerciseStates[exerciseIndex] ?: return
        _uiState.value = _uiState.value.copy(
            timedExerciseStates = _uiState.value.timedExerciseStates.toMutableMap().apply {
                put(exerciseIndex, timedState.copy(isRunning = false))
            }
        )
    }

    fun resetTimedExercise(exerciseIndex: Int) {
        timedExerciseJobs[exerciseIndex]?.cancel()
        timedExerciseJobs.remove(exerciseIndex)

        val timedState = _uiState.value.timedExerciseStates[exerciseIndex] ?: return
        _uiState.value = _uiState.value.copy(
            timedExerciseStates = _uiState.value.timedExerciseStates.toMutableMap().apply {
                put(
                    exerciseIndex,
                    timedState.copy(
                        remainingSeconds = timedState.totalSeconds,
                        isRunning = false,
                        isFinished = false
                    )
                )
            }
        )
    }

    fun finishWorkout() {
        val state = _uiState.value
        if (state.isCompleted) return

        restTimerJob?.cancel()
        timedExerciseJobs.values.forEach { it.cancel() }
        timedExerciseJobs.clear()

        viewModelScope.launch {
            val completionTime = System.currentTimeMillis()
            val totalDurationMs = completionTime - state.startTime
            val profile = userProfile

            // Calculate total reps
            var totalReps = 0
            state.exercises.forEachIndexed { index, exercise ->
                val exState = state.exerciseStates.getOrNull(index)
                if (exState != null && exState.isCompleted) {
                    val repsPerSet = Regex("\\d+").find(exercise.reps)?.value?.toIntOrNull() ?: 0
                    totalReps += exercise.sets * repsPerSet
                }
            }

            // Calculate estimated calories
            var estimatedCalories = 0f
            val bodyWeightKg = profile?.bodyWeightKg ?: 70f
            state.exercises.forEachIndexed { index, exercise ->
                val exState = state.exerciseStates.getOrNull(index)
                if (exState != null && exState.isCompleted) {
                    val durationMin = CalorieCalculator.estimateExerciseDuration(
                        exercise.sets,
                        exercise.reps,
                        exercise.restBetweenSetsSec
                    )
                    estimatedCalories += CalorieCalculator.calculate(
                        exercise.metValue,
                        bodyWeightKg,
                        durationMin
                    )
                }
            }

            // Save session to Room
            val session = WorkoutSession(
                id = state.sessionId,
                weekNumber = state.weekNumber,
                dayNumber = state.dayNumber,
                dayLabel = state.dayLabel,
                startedAt = state.startTime,
                completedAt = completionTime,
                totalDuration = totalDurationMs,
                totalRestTime = accumulatedRestTimeMs,
                totalReps = totalReps,
                estimatedCalories = estimatedCalories,
                isCompleted = true
            )
            repository.updateSession(session)

            // Save individual exercise logs
            state.exercises.forEachIndexed { index, exercise ->
                val exState = state.exerciseStates.getOrNull(index) ?: return@forEachIndexed
                val repsCompleted = if (exState.isCompleted) {
                    val perSet = Regex("\\d+").find(exercise.reps)?.value?.toIntOrNull() ?: 0
                    exercise.sets * perSet
                } else {
                    val perSet = Regex("\\d+").find(exercise.reps)?.value?.toIntOrNull() ?: 0
                    exState.setsCompleted * perSet
                }

                val log = ExerciseLog(
                    sessionId = state.sessionId,
                    exerciseName = exercise.name,
                    illustrationId = exercise.illustrationId,
                    targetSets = exercise.sets,
                    targetReps = exercise.reps,
                    completedSets = exState.setsCompleted,
                    completedReps = repsCompleted,
                    restBetweenSetsSec = exercise.restBetweenSetsSec,
                    restBetweenExercisesSec = exercise.restBetweenExercisesSec,
                    completedAt = if (exState.isCompleted) completionTime else null
                )
                repository.insertExercise(log)
            }

            // Advance current day in profile
            if (profile != null) {
                val absoluteDay = (state.weekNumber - 1) * 7 + state.dayNumber
                if (absoluteDay >= profile.currentDay) {
                    repository.updateCurrentDay(absoluteDay + 1)
                }
            }

            _uiState.value = state.copy(
                isResting = false,
                restSeconds = 0,
                totalRestSeconds = 0,
                isCompleted = true,
                completionTime = completionTime,
                totalReps = totalReps,
                totalDurationMs = totalDurationMs,
                estimatedCalories = estimatedCalories,
                totalRestTimeMs = accumulatedRestTimeMs
            )
        }
    }

    private fun startRestTimer(seconds: Int) {
        if (seconds <= 0) return

        restTimerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isResting = true,
            restSeconds = seconds,
            totalRestSeconds = seconds
        )

        restTimerJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                delay(1000L)
                remaining--
                accumulatedRestTimeMs += 1000L
                _uiState.value = _uiState.value.copy(restSeconds = remaining)
            }
            _uiState.value = _uiState.value.copy(
                isResting = false,
                restSeconds = 0,
                totalRestSeconds = 0
            )
        }
    }

    private fun saveExerciseProgress(exerciseIndex: Int, setsCompleted: Int, isComplete: Boolean) {
        val state = _uiState.value
        val exercise = state.exercises.getOrNull(exerciseIndex) ?: return

        viewModelScope.launch {
            val repsCompleted = run {
                val perSet = exercise.reps
                    .replace(Regex("[^0-9]"), "")
                    .toIntOrNull() ?: 0
                setsCompleted * perSet
            }

            val log = ExerciseLog(
                sessionId = state.sessionId,
                exerciseName = exercise.name,
                illustrationId = exercise.illustrationId,
                targetSets = exercise.sets,
                targetReps = exercise.reps,
                completedSets = setsCompleted,
                completedReps = repsCompleted,
                restBetweenSetsSec = exercise.restBetweenSetsSec,
                restBetweenExercisesSec = exercise.restBetweenExercisesSec,
                completedAt = if (isComplete) System.currentTimeMillis() else null
            )
            repository.insertExercise(log)
        }
    }

    private fun parseTimedSeconds(reps: String): Int {
        // Parse formats like "30s", "45s", "20s on / 40s off", "4 min 20s/10s", "20 min", "10 min"
        val minMatch = Regex("(\\d+)\\s*min").find(reps)
        if (minMatch != null) {
            val minutes = minMatch.groupValues[1].toIntOrNull() ?: 0
            val secMatch = Regex("(\\d+)s").find(reps)
            val extraSec = secMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
            return minutes * 60 + extraSec
        }

        val secMatch = Regex("(\\d+)s").find(reps)
        if (secMatch != null) {
            return secMatch.groupValues[1].toIntOrNull() ?: 30
        }

        // Fallback
        return 30
    }

    override fun onCleared() {
        super.onCleared()
        restTimerJob?.cancel()
        timedExerciseJobs.values.forEach { it.cancel() }
    }
}
