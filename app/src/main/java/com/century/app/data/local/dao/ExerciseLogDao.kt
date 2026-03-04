package com.century.app.data.local.dao

import androidx.room.*
import com.century.app.data.local.entity.ExerciseLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseLogDao {
    @Query("SELECT * FROM exercise_log WHERE sessionId = :sessionId")
    fun getExercisesForSession(sessionId: Long): Flow<List<ExerciseLog>>

    @Query("SELECT * FROM exercise_log WHERE sessionId = :sessionId")
    suspend fun getExercisesForSessionOnce(sessionId: Long): List<ExerciseLog>

    @Insert
    suspend fun insertExercise(log: ExerciseLog): Long

    @Insert
    suspend fun insertExercises(logs: List<ExerciseLog>)

    @Update
    suspend fun updateExercise(log: ExerciseLog)

    @Query("SELECT SUM(completedReps) FROM exercise_log WHERE sessionId = :sessionId")
    suspend fun getTotalRepsForSession(sessionId: Long): Int?
}
