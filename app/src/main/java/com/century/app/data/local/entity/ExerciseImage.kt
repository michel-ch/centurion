package com.century.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercise_image",
    indices = [
        Index(value = ["illustrationId"], unique = true)
    ]
)
data class ExerciseImage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val illustrationId: String,
    val customImageUri: String? = null,
    val isCustom: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
