package com.century.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_session",
    indices = [
        Index(value = ["userId", "weekNumber", "dayNumber"], unique = true)
    ]
)
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 1,
    val weekNumber: Int,
    val dayNumber: Int,
    val dayLabel: String,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val totalDuration: Long = 0, // milliseconds
    val totalRestTime: Long = 0, // milliseconds
    val totalReps: Int = 0,
    val estimatedCalories: Float = 0f,
    val isCompleted: Boolean = false
)
