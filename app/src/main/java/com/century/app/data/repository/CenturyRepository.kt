package com.century.app.data.repository

import com.century.app.data.local.dao.*
import com.century.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CenturyRepository @Inject constructor(
    private val userProfileDao: UserProfileDao,
    private val weightLogDao: WeightLogDao,
    private val workoutSessionDao: WorkoutSessionDao,
    private val exerciseLogDao: ExerciseLogDao,
    private val exerciseImageDao: ExerciseImageDao,
    private val pushUpTestDao: PushUpTestDao
) {
    // ===== User Profile =====
    fun getProfile(): Flow<UserProfile?> = userProfileDao.getProfile()
    suspend fun getProfileOnce(): UserProfile? = userProfileDao.getProfileOnce()
    suspend fun insertProfile(profile: UserProfile): Long = userProfileDao.insertProfile(profile)
    suspend fun updateProfile(profile: UserProfile) = userProfileDao.updateProfile(profile)
    suspend fun updateCurrentDay(day: Int) = userProfileDao.updateCurrentDay(day)
    suspend fun hasProfile(): Boolean = userProfileDao.getProfileCount() > 0

    // ===== Weight Log =====
    fun getAllWeightLogs(): Flow<List<WeightLog>> = weightLogDao.getAllWeightLogs()
    fun getRecentWeightLogs(limit: Int = 30): Flow<List<WeightLog>> = weightLogDao.getRecentLogs(limit)
    suspend fun getLatestWeightLog(): WeightLog? = weightLogDao.getLatestLog()
    suspend fun insertWeightLog(log: WeightLog): Long = weightLogDao.insertLog(log)
    suspend fun updateWeightLog(log: WeightLog) = weightLogDao.updateLog(log)
    suspend fun deleteWeightLog(log: WeightLog) = weightLogDao.deleteLog(log)

    // ===== Workout Sessions =====
    fun getAllSessions(): Flow<List<WorkoutSession>> = workoutSessionDao.getAllSessions()
    fun getCompletedSessions(): Flow<List<WorkoutSession>> = workoutSessionDao.getCompletedSessions()
    suspend fun getSessionForDay(week: Int, day: Int): WorkoutSession? =
        workoutSessionDao.getSessionForDay(week, day)
    suspend fun getSessionById(id: Long): WorkoutSession? = workoutSessionDao.getSessionById(id)
    fun getCompletedCount(): Flow<Int> = workoutSessionDao.getCompletedCount()
    fun getTotalReps(): Flow<Int?> = workoutSessionDao.getTotalReps()
    fun getTotalCalories(): Flow<Float?> = workoutSessionDao.getTotalCalories()
    suspend fun insertSession(session: WorkoutSession): Long = workoutSessionDao.insertSession(session)
    suspend fun updateSession(session: WorkoutSession) = workoutSessionDao.updateSession(session)
    suspend fun getCompletedSessionsList(): List<WorkoutSession> =
        workoutSessionDao.getCompletedSessionsList()

    // ===== Exercise Logs =====
    fun getExercisesForSession(sessionId: Long): Flow<List<ExerciseLog>> =
        exerciseLogDao.getExercisesForSession(sessionId)
    suspend fun getExercisesForSessionOnce(sessionId: Long): List<ExerciseLog> =
        exerciseLogDao.getExercisesForSessionOnce(sessionId)
    suspend fun insertExercise(log: ExerciseLog): Long = exerciseLogDao.insertExercise(log)
    suspend fun insertExercises(logs: List<ExerciseLog>) = exerciseLogDao.insertExercises(logs)
    suspend fun updateExercise(log: ExerciseLog) = exerciseLogDao.updateExercise(log)

    // ===== Exercise Images =====
    suspend fun getExerciseImage(illustrationId: String): ExerciseImage? =
        exerciseImageDao.getImageForExercise(illustrationId)
    fun getAllCustomImages(): Flow<List<ExerciseImage>> = exerciseImageDao.getAllCustomImages()
    suspend fun insertExerciseImage(image: ExerciseImage): Long = exerciseImageDao.insertImage(image)
    suspend fun deleteExerciseImage(illustrationId: String) = exerciseImageDao.deleteImage(illustrationId)

    // ===== Push-Up Tests =====
    fun getAllPushUpTests(): Flow<List<PushUpTest>> = pushUpTestDao.getAllTests()
    suspend fun getPushUpTestForWeek(week: Int): PushUpTest? = pushUpTestDao.getTestForWeek(week)
    suspend fun insertPushUpTest(test: PushUpTest): Long = pushUpTestDao.insertTest(test)

    // ===== Streak Calculation =====
    suspend fun calculateStreak(): Int {
        val sessions = getCompletedSessionsList()
        if (sessions.isEmpty()) return 0

        var streak = 0
        val sortedDays = sessions.map { it.weekNumber * 7 + it.dayNumber }.sorted().reversed()
        var expected = sortedDays.firstOrNull() ?: return 0

        for (day in sortedDays) {
            if (day == expected) {
                streak++
                expected--
            } else break
        }
        return streak
    }
}
