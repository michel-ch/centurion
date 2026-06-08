package com.century.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "push_up_test",
    indices = [
        Index(value = ["userId", "weekNumber"], unique = true)
    ]
)
data class PushUpTest(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 1,
    val weekNumber: Int,
    val maxReps: Int,
    val testedAt: Long = System.currentTimeMillis()
)
