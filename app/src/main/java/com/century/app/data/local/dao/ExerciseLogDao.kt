package com.century.app.data.local.dao

import androidx.room.*
import com.century.app.data.local.entity.ExerciseLog
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ExerciseLogDao {
    @Query("SELECT * FROM exercise_log WHERE sessionId = :sessionId ORDER BY exerciseIndex ASC")
    abstract fun getExercisesForSession(sessionId: Long): Flow<List<ExerciseLog>>

    @Query("SELECT * FROM exercise_log WHERE sessionId = :sessionId ORDER BY exerciseIndex ASC")
    abstract suspend fun getExercisesForSessionOnce(sessionId: Long): List<ExerciseLog>

    suspend fun insertExercise(log: ExerciseLog): Long = upsertExercise(log)

    suspend fun insertExercises(logs: List<ExerciseLog>) = upsertExercises(logs)

    @Update
    abstract suspend fun updateExercise(log: ExerciseLog)

    @Transaction
    open suspend fun upsertExercise(log: ExerciseLog): Long {
        val insertedId = insertExerciseIgnoringConflict(log)
        if (insertedId != -1L) return insertedId

        updateExerciseByIdentity(
            sessionId = log.sessionId,
            exerciseIndex = log.exerciseIndex,
            exerciseName = log.exerciseName,
            illustrationId = log.illustrationId,
            targetSets = log.targetSets,
            targetReps = log.targetReps,
            completedSets = log.completedSets,
            completedReps = log.completedReps,
            restBetweenSetsSec = log.restBetweenSetsSec,
            restBetweenExercisesSec = log.restBetweenExercisesSec,
            actualRestTimeSec = log.actualRestTimeSec,
            notes = log.notes,
            completedAt = log.completedAt
        )

        return getExerciseId(log.sessionId, log.exerciseIndex)
            ?: error("Exercise log upsert failed for session ${log.sessionId}, exercise ${log.exerciseIndex}")
    }

    @Transaction
    open suspend fun upsertExercises(logs: List<ExerciseLog>) {
        logs.forEach { upsertExercise(it) }
    }

    @Query("SELECT COALESCE(SUM(completedReps), 0) FROM exercise_log WHERE sessionId = :sessionId")
    abstract suspend fun getTotalRepsForSession(sessionId: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertExerciseIgnoringConflict(log: ExerciseLog): Long

    @Query(
        """
        UPDATE exercise_log
        SET exerciseName = :exerciseName,
            illustrationId = :illustrationId,
            targetSets = :targetSets,
            targetReps = :targetReps,
            completedSets = :completedSets,
            completedReps = :completedReps,
            restBetweenSetsSec = :restBetweenSetsSec,
            restBetweenExercisesSec = :restBetweenExercisesSec,
            actualRestTimeSec = :actualRestTimeSec,
            notes = :notes,
            completedAt = :completedAt
        WHERE sessionId = :sessionId AND exerciseIndex = :exerciseIndex
        """
    )
    protected abstract suspend fun updateExerciseByIdentity(
        sessionId: Long,
        exerciseIndex: Int,
        exerciseName: String,
        illustrationId: String,
        targetSets: Int,
        targetReps: String,
        completedSets: Int,
        completedReps: Int,
        restBetweenSetsSec: Int,
        restBetweenExercisesSec: Int,
        actualRestTimeSec: Int,
        notes: String?,
        completedAt: Long?
    ): Int

    @Query("SELECT id FROM exercise_log WHERE sessionId = :sessionId AND exerciseIndex = :exerciseIndex")
    protected abstract suspend fun getExerciseId(sessionId: Long, exerciseIndex: Int): Long?
}
