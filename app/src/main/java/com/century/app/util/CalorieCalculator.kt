package com.century.app.util

object CalorieCalculator {
    /**
     * MET values:
     * Push-ups / bodyweight strength: 3.8
     * Plyometrics / HIIT: 8.0
     * Walking / active recovery: 3.0
     * Stretching: 2.5
     *
     * Formula: Calories = MET × body weight (kg) × duration (hours)
     */
    fun calculate(
        metValue: Float,
        bodyWeightKg: Float,
        durationMinutes: Float
    ): Float {
        return metValue * bodyWeightKg * (durationMinutes / 60f)
    }

    fun estimateExerciseDuration(sets: Int, reps: String, restBetweenSetsSec: Int): Float {
        val repCount = reps.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 10
        val secondsPerRep = 3f
        val workTime = sets * repCount * secondsPerRep
        val restTime = (sets - 1) * restBetweenSetsSec
        return (workTime + restTime) / 60f
    }
}
