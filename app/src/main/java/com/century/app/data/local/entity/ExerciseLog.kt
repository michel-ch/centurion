package com.century.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_log")
data class ExerciseLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val exerciseName: String,
    val illustrationId: String,
    val targetSets: Int,
    val targetReps: String, // e.g. "10", "30s", "12/leg", "1-2-3...10"
    val completedSets: Int = 0,
    val completedReps: Int = 0,
    val restBetweenSetsSec: Int = 60,
    val restBetweenExercisesSec: Int = 90,
    val actualRestTimeSec: Int = 0,
    val notes: String? = null,
    val completedAt: Long? = null
)
