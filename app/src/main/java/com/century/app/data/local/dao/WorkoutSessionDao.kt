package com.century.app.data.local.dao

import androidx.room.*
import com.century.app.data.local.entity.WorkoutSession
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {
    @Query("SELECT * FROM workout_session ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_session WHERE isCompleted = 1 ORDER BY startedAt DESC")
    fun getCompletedSessions(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_session WHERE weekNumber = :week AND dayNumber = :day LIMIT 1")
    suspend fun getSessionForDay(week: Int, day: Int): WorkoutSession?

    @Query("SELECT * FROM workout_session WHERE id = :id")
    suspend fun getSessionById(id: Long): WorkoutSession?

    @Query("SELECT COUNT(*) FROM workout_session WHERE isCompleted = 1")
    fun getCompletedCount(): Flow<Int>

    @Query("SELECT SUM(totalReps) FROM workout_session WHERE isCompleted = 1")
    fun getTotalReps(): Flow<Int?>

    @Query("SELECT SUM(estimatedCalories) FROM workout_session WHERE isCompleted = 1")
    fun getTotalCalories(): Flow<Float?>

    @Insert
    suspend fun insertSession(session: WorkoutSession): Long

    @Update
    suspend fun updateSession(session: WorkoutSession)

    @Query("SELECT * FROM workout_session WHERE isCompleted = 1 ORDER BY completedAt DESC")
    suspend fun getCompletedSessionsList(): List<WorkoutSession>
}
