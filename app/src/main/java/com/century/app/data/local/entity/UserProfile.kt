package com.century.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val bodyWeight: Float,
    val bodyWeightUnit: String = "kg", // "kg" or "lbs"
    val height: Float,
    val heightUnit: String = "cm", // "cm" or "ft"
    val heightInches: Int = 0, // used only when heightUnit = "ft"
    val age: Int,
    val gender: String, // "Male", "Female", "Other"
    val fitnessLevel: String, // "Beginner", "Intermediate", "Advanced"
    val currentMaxPushUps: Int,
    val goalWeight: Float? = null,
    val reminderTime: String = "07:00",
    val reminderEnabled: Boolean = true,
    val profilePhotoUri: String? = null,
    val useDarkTheme: Boolean = true,
    val restBetweenSetsSec: Int = 60,
    val restBetweenExercisesSec: Int = 90,
    val autoReduceRest: Boolean = true,
    val programStartDate: Long = System.currentTimeMillis(),
    val currentDay: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val bodyWeightKg: Float
        get() = if (bodyWeightUnit == "kg") bodyWeight else bodyWeight * 0.453592f

    val heightCm: Float
        get() = if (heightUnit == "cm") height else (height * 30.48f + heightInches * 2.54f)

    val bmi: Float
        get() {
            val hm = heightCm / 100f
            return if (hm > 0) bodyWeightKg / (hm * hm) else 0f
        }

    val bmiCategory: String
        get() = when {
            bmi < 18.5f -> "Underweight"
            bmi < 25f -> "Normal"
            bmi < 30f -> "Overweight"
            else -> "Obese"
        }
}
