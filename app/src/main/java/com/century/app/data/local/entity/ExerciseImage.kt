package com.century.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_image")
data class ExerciseImage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val illustrationId: String,
    val customImageUri: String? = null,
    val isCustom: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
