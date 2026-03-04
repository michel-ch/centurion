package com.century.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_log")
data class WeightLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 1,
    val weight: Float,
    val unit: String = "kg",
    val loggedAt: Long = System.currentTimeMillis()
) {
    val weightKg: Float
        get() = if (unit == "kg") weight else weight * 0.453592f
}
