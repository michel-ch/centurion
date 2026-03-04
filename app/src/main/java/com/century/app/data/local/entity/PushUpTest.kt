package com.century.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "push_up_test")
data class PushUpTest(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 1,
    val weekNumber: Int,
    val maxReps: Int,
    val testedAt: Long = System.currentTimeMillis()
)
