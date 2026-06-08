package com.century.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

private const val LBS_TO_KG = 0.453592f
private const val KG_TO_LBS = 2.20462f
private const val MIN_WEIGHT_KG = 20f
private const val MAX_WEIGHT_KG = 500f

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
        get() = weightToKg(weight, unit)

    fun weightInUnit(targetUnit: String): Float = weightFromKg(weightKg, targetUnit)
}

fun isSaneWeight(weight: Float, unit: String): Boolean {
    if (!weight.isFinite()) return false
    return weightToKg(weight, unit) in MIN_WEIGHT_KG..MAX_WEIGHT_KG
}

fun weightToKg(weight: Float, unit: String): Float =
    if (unit == "lbs") weight * LBS_TO_KG else weight

fun weightFromKg(weightKg: Float, unit: String): Float =
    if (unit == "lbs") weightKg * KG_TO_LBS else weightKg
