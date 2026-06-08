package com.century.app.data.local.dao

import androidx.room.*
import com.century.app.data.local.entity.WorkoutSession
import kotlinx.coroutines.flow.Flow

@Dao
abstract class WorkoutSessionDao {
    @Query("SELECT * FROM workout_session ORDER BY startedAt DESC")
    abstract fun getAllSessions(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_session WHERE isCompleted = 1 ORDER BY startedAt DESC")
    abstract fun getCompletedSessions(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_session WHERE userId = 1 AND weekNumber = :week AND dayNumber = :day ORDER BY startedAt DESC LIMIT 1")
    abstract suspend fun getSessionForDay(week: Int, day: Int): WorkoutSession?

    @Query("SELECT * FROM workout_session WHERE id = :id")
    abstract suspend fun getSessionById(id: Long): WorkoutSession?

    @Query("SELECT COUNT(*) FROM workout_session WHERE isCompleted = 1")
    abstract fun getCompletedCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(totalReps), 0) FROM workout_session WHERE isCompleted = 1")
    abstract fun getTotalReps(): Flow<Int>

    @Query("SELECT COALESCE(SUM(estimatedCalories), 0) FROM workout_session WHERE isCompleted = 1")
    abstract fun getTotalCalories(): Flow<Float>

    @Transaction
    open suspend fun insertSession(session: WorkoutSession): Long {
        val insertedId = insertSessionIgnoringConflict(session)
        if (insertedId != -1L) return insertedId

        return getSessionIdForDay(session.userId, session.weekNumber, session.dayNumber)
            ?: error("Workout session insert failed for week ${session.weekNumber}, day ${session.dayNumber}")
    }

    @Update
    abstract suspend fun updateSession(session: WorkoutSession)

    @Query("SELECT * FROM workout_session WHERE isCompleted = 1 ORDER BY completedAt DESC")
    abstract suspend fun getCompletedSessionsList(): List<WorkoutSession>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertSessionIgnoringConflict(session: WorkoutSession): Long

    @Query("SELECT id FROM workout_session WHERE userId = :userId AND weekNumber = :week AND dayNumber = :day")
    protected abstract suspend fun getSessionIdForDay(userId: Long, week: Int, day: Int): Long?
}
