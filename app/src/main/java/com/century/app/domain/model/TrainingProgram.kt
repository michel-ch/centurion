package com.century.app.domain.model

data class ProgramWeek(
    val weekNumber: Int,
    val title: String,
    val pushUpTarget: String,
    val days: List<ProgramDay>
)

data class ProgramDay(
    val dayNumber: Int,
    val label: String,
    val exercises: List<ProgramExercise>,
    val isRestDay: Boolean = false
)

data class ProgramExercise(
    val name: String,
    val sets: Int,
    val reps: String,
    val restBetweenSetsSec: Int = 60,
    val restBetweenExercisesSec: Int = 90,
    val formNote: String,
    val illustrationId: String,
    val isTimed: Boolean = false,
    val isMaxTest: Boolean = false,
    val metValue: Float = 3.8f
)

object TrainingProgramData {

    fun getProgram(): List<ProgramWeek> = listOf(week1, week2, week3, week4)

    fun getDayForProgram(absoluteDay: Int): Pair<ProgramWeek, ProgramDay>? {
        val weekIndex = (absoluteDay - 1) / 7
        val dayIndex = (absoluteDay - 1) % 7
        val weeks = getProgram()
        if (weekIndex !in weeks.indices) return null
        val week = weeks[weekIndex]
        if (dayIndex !in week.days.indices) return null
        return week to week.days[dayIndex]
    }

    fun adjustForFitnessLevel(
        exercise: ProgramExercise,
        fitnessLevel: String,
        maxPushUps: Int
    ): ProgramExercise {
        val isBeginner = fitnessLevel == "Beginner" || maxPushUps < 10
        val isAdvanced = fitnessLevel == "Advanced" || maxPushUps > 40
        if (!isBeginner && !isAdvanced) return exercise

        return if (isBeginner) {
            val adjustedReps = try {
                val num = exercise.reps.replace(Regex("[^0-9]"), "").toIntOrNull()
                if (num != null && exercise.name.contains("Push-Up", ignoreCase = true)) {
                    val reduced = kotlin.math.ceil(num * 0.6).toInt()
                    exercise.reps.replace(Regex("\\d+"), reduced.toString())
                } else exercise.reps
            } catch (_: Exception) { exercise.reps }
            exercise.copy(
                reps = adjustedReps,
                restBetweenSetsSec = exercise.restBetweenSetsSec + 15,
                restBetweenExercisesSec = exercise.restBetweenExercisesSec + 15
            )
        } else {
            val adjustedReps = try {
                val num = exercise.reps.replace(Regex("[^0-9]"), "").toIntOrNull()
                if (num != null && exercise.name.contains("Push-Up", ignoreCase = true)) {
                    val increased = kotlin.math.ceil(num * 1.25).toInt()
                    exercise.reps.replace(Regex("\\d+"), increased.toString())
                } else exercise.reps
            } catch (_: Exception) { exercise.reps }
            exercise.copy(
                reps = adjustedReps,
                restBetweenSetsSec = maxOf(exercise.restBetweenSetsSec - 15, 10),
                restBetweenExercisesSec = maxOf(exercise.restBetweenExercisesSec - 15, 10)
            )
        }
    }

    fun adjustRestForWeek(exercise: ProgramExercise, weekNumber: Int, autoReduce: Boolean): ProgramExercise {
        if (!autoReduce || weekNumber <= 1) return exercise
        val reduction = (weekNumber - 1) * 5
        return exercise.copy(
            restBetweenSetsSec = maxOf(exercise.restBetweenSetsSec - reduction, 10),
            restBetweenExercisesSec = maxOf(exercise.restBetweenExercisesSec - reduction, 10)
        )
    }

    // ========== WEEK 1 — FOUNDATION ==========
    private val week1 = ProgramWeek(
        weekNumber = 1,
        title = "FOUNDATION",
        pushUpTarget = "4×10 to 4×12",
        days = listOf(
            ProgramDay(1, "PUSH + CORE", listOf(
                ProgramExercise("Push-Ups", 4, "10", 60, 90, "Strict form, chest to floor", "push_up_standard"),
                ProgramExercise("Incline Push-Ups", 3, "12", 45, 90, "Hands on bench or step", "push_up_incline"),
                ProgramExercise("Diamond Push-Ups", 3, "6", 60, 90, "Hands close together", "push_up_diamond"),
                ProgramExercise("Plank Hold", 3, "30s", 30, 60, "Squeeze glutes & abs", "plank_hold", isTimed = true),
                ProgramExercise("Dead Bugs", 3, "10/side", 30, 60, "Slow & controlled", "dead_bug", metValue = 3.0f),
                ProgramExercise("Mountain Climbers", 3, "20", 45, 90, "Keep hips level", "mountain_climber", metValue = 8.0f)
            )),
            ProgramDay(2, "LEGS + CARDIO", listOf(
                ProgramExercise("Bodyweight Squats", 4, "15", 45, 90, "Below parallel", "squat_bodyweight", metValue = 5.0f),
                ProgramExercise("Lunges", 3, "12/leg", 45, 90, "Long stride, upright torso", "lunge_forward", metValue = 5.0f),
                ProgramExercise("Glute Bridges", 3, "15", 30, 60, "Squeeze 2s at top", "glute_bridge", metValue = 3.0f),
                ProgramExercise("Calf Raises", 3, "20", 30, 60, "Full range of motion", "calf_raise", metValue = 3.0f),
                ProgramExercise("Jump Squats", 3, "10", 60, 90, "Land softly", "squat_jump", metValue = 8.0f),
                ProgramExercise("High Knees", 3, "30s", 45, 90, "Max effort", "high_knees", isTimed = true, metValue = 8.0f)
            )),
            ProgramDay(3, "REST / ACTIVE RECOVERY", listOf(
                ProgramExercise("Walk or Light Jog", 1, "20 min", 0, 0, "Stay loose", "walk_jog", isTimed = true, metValue = 3.0f),
                ProgramExercise("Full Body Stretch", 1, "10 min", 0, 0, "Hit every muscle group", "stretch_full_body", isTimed = true, metValue = 2.5f),
                ProgramExercise("Foam Roll", 1, "10 min", 0, 0, "Focus sore areas", "foam_roll", isTimed = true, metValue = 2.5f)
            ), isRestDay = true),
            ProgramDay(4, "PUSH + PULL", listOf(
                ProgramExercise("Push-Ups", 4, "12", 60, 90, "Push the rep count", "push_up_standard"),
                ProgramExercise("Wide Push-Ups", 3, "10", 45, 90, "Hands outside shoulders", "push_up_wide"),
                ProgramExercise("Superman Holds", 3, "12", 30, 60, "Squeeze shoulder blades", "superman_hold", metValue = 3.0f),
                ProgramExercise("Towel Rows", 3, "10", 45, 90, "Or inverted rows", "towel_row", metValue = 3.8f),
                ProgramExercise("Reverse Snow Angels", 3, "10", 30, 60, "Face down, arms sweep", "reverse_snow_angel", metValue = 3.0f),
                ProgramExercise("Bicycle Crunches", 3, "20", 30, 60, "Slow, touch elbow to knee", "bicycle_crunch", metValue = 3.0f)
            )),
            ProgramDay(5, "LEGS + CORE", listOf(
                ProgramExercise("Bulgarian Split Squats", 3, "10/leg", 60, 90, "Rear foot elevated", "bulgarian_split_squat", metValue = 5.0f),
                ProgramExercise("Wall Sit", 3, "30s", 30, 60, "Thighs parallel", "wall_sit", isTimed = true, metValue = 3.0f),
                ProgramExercise("Single-Leg Glute Bridges", 3, "10/leg", 30, 60, "Drive through heel", "glute_bridge_single_leg", metValue = 3.0f),
                ProgramExercise("Side Plank", 3, "20s/side", 20, 60, "Hips high", "side_plank", isTimed = true, metValue = 3.0f),
                ProgramExercise("Leg Raises", 3, "12", 30, 60, "Lower back stays flat", "leg_raise", metValue = 3.0f),
                ProgramExercise("Burpees", 3, "8", 60, 90, "Full extension at top", "burpee", metValue = 8.0f)
            )),
            ProgramDay(6, "PUSH-UP TEST + CONDITIONING", listOf(
                ProgramExercise("MAX Push-Up Test", 1, "to failure", 0, 120, "Record your number", "push_up_standard", isMaxTest = true),
                ProgramExercise("Jumping Jacks", 3, "40", 30, 60, "Full arm extension", "jumping_jack", metValue = 8.0f),
                ProgramExercise("Squat Thrusts", 3, "12", 45, 90, "Explosive", "squat_thrust", metValue = 8.0f),
                ProgramExercise("Plank to Push-Up", 3, "8", 45, 90, "Forearm to hand", "plank_to_pushup"),
                ProgramExercise("Flutter Kicks", 3, "20", 30, 60, "Lower back on ground", "flutter_kick", metValue = 3.0f)
            )),
            ProgramDay(7, "FULL REST", listOf(
                ProgramExercise("Complete Rest", 1, "—", 0, 0, "Sleep 8+ hours. Hydrate. Recover.", "rest_day", metValue = 1.0f)
            ), isRestDay = true)
        )
    )

    // ========== WEEK 2 — VOLUME ==========
    private val week2 = ProgramWeek(
        weekNumber = 2,
        title = "VOLUME",
        pushUpTarget = "5×12 to 5×15",
        days = listOf(
            ProgramDay(1, "PUSH OVERLOAD", listOf(
                ProgramExercise("Push-Ups", 5, "12", 60, 90, "60s rest between sets", "push_up_standard"),
                ProgramExercise("Decline Push-Ups", 3, "10", 45, 90, "Feet on chair", "push_up_decline"),
                ProgramExercise("Diamond Push-Ups", 3, "8", 60, 90, "Elbows stay tight", "push_up_diamond"),
                ProgramExercise("Archer Push-Ups", 3, "5/side", 60, 90, "Wide, shift weight side to side", "push_up_archer"),
                ProgramExercise("Plank Shoulder Taps", 3, "16", 30, 60, "Minimal hip sway", "plank_shoulder_tap", metValue = 3.0f),
                ProgramExercise("Ab Rollouts (towel)", 3, "8", 45, 60, "Slide hands forward on towel", "ab_rollout_towel", metValue = 3.8f)
            )),
            ProgramDay(2, "LEGS + PLYOMETRICS", listOf(
                ProgramExercise("Jump Squats", 4, "12", 60, 90, "Explode upward", "squat_jump", metValue = 8.0f),
                ProgramExercise("Walking Lunges", 4, "12/leg", 45, 90, "Big steps", "lunge_walking", metValue = 5.0f),
                ProgramExercise("Pistol Squat Progressions", 3, "5/leg", 60, 90, "Use a chair for support", "pistol_squat_assisted", metValue = 5.0f),
                ProgramExercise("Box Jumps", 3, "10", 60, 90, "Step down, don't jump down", "box_jump", metValue = 8.0f),
                ProgramExercise("Calf Raises", 4, "20", 30, 60, "3s pause at top", "calf_raise", metValue = 3.0f),
                ProgramExercise("Sprint Intervals", 6, "20s on / 40s off", 0, 120, "All out effort", "sprint_interval", isTimed = true, metValue = 8.0f)
            )),
            ProgramDay(3, "REST / MOBILITY", listOf(
                ProgramExercise("Yoga Flow or Walk", 1, "25 min", 0, 0, "Focus on hips & shoulders", "yoga_flow", isTimed = true, metValue = 3.0f),
                ProgramExercise("Deep Stretching", 1, "15 min", 0, 0, "Hold each stretch 45s", "stretch_deep", isTimed = true, metValue = 2.5f)
            ), isRestDay = true),
            ProgramDay(4, "UPPER BODY + CORE", listOf(
                ProgramExercise("Push-Ups", 5, "15", 60, 90, "Keep pushing volume", "push_up_standard"),
                ProgramExercise("Pike Push-Ups", 3, "8", 60, 90, "Hips high, targets shoulders", "push_up_pike"),
                ProgramExercise("Towel/Band Rows", 4, "12", 45, 90, "Squeeze shoulder blades", "towel_row", metValue = 3.8f),
                ProgramExercise("Superman Pulses", 3, "15", 30, 60, "Small controlled pulses", "superman_pulse", metValue = 3.0f),
                ProgramExercise("Hanging Knee Raises", 3, "12", 30, 60, "Control the descent", "knee_raise", metValue = 3.0f),
                ProgramExercise("Russian Twists", 3, "20", 30, 60, "Feet off ground", "russian_twist", metValue = 3.0f)
            )),
            ProgramDay(5, "LEGS + GLUTES", listOf(
                ProgramExercise("Sumo Squats", 4, "15", 45, 90, "Wide stance, toes out", "squat_sumo", metValue = 5.0f),
                ProgramExercise("Single-Leg Deadlifts", 3, "10/leg", 45, 90, "Balance challenge", "deadlift_single_leg", metValue = 5.0f),
                ProgramExercise("Donkey Kicks", 3, "15/leg", 30, 60, "Squeeze at top", "donkey_kick", metValue = 3.0f),
                ProgramExercise("Fire Hydrants", 3, "15/leg", 30, 60, "Keep core tight", "fire_hydrant", metValue = 3.0f),
                ProgramExercise("Side Lunges", 3, "10/side", 45, 90, "Sit deep into the hip", "lunge_side", metValue = 5.0f),
                ProgramExercise("Tabata Burpees", 1, "4 min 20s/10s", 0, 120, "Survive it", "burpee", isTimed = true, metValue = 8.0f)
            )),
            ProgramDay(6, "PUSH-UP LADDER + CARDIO", listOf(
                ProgramExercise("Push-Up Ladder", 1, "1-2-3...10 down", 15, 90, "110 total reps", "push_up_standard"),
                ProgramExercise("Mountain Climbers", 4, "20", 30, 60, "Fast but controlled", "mountain_climber", metValue = 8.0f),
                ProgramExercise("Star Jumps", 3, "15", 45, 90, "Max height", "star_jump", metValue = 8.0f),
                ProgramExercise("Plank Hold", 3, "45s", 30, 60, "Don't let hips sag", "plank_hold", isTimed = true)
            )),
            ProgramDay(7, "FULL REST", listOf(
                ProgramExercise("Complete Rest", 1, "—", 0, 0, "Eat well. Sleep deep.", "rest_day", metValue = 1.0f)
            ), isRestDay = true)
        )
    )

    // ========== WEEK 3 — INTENSITY ==========
    private val week3 = ProgramWeek(
        weekNumber = 3,
        title = "INTENSITY",
        pushUpTarget = "5×18 to 4×25",
        days = listOf(
            ProgramDay(1, "PUSH POWER", listOf(
                ProgramExercise("Push-Ups", 5, "18", 45, 90, "45s rest between sets", "push_up_standard"),
                ProgramExercise("Explosive Push-Ups", 3, "8", 60, 90, "Hands leave the floor", "push_up_explosive"),
                ProgramExercise("Decline Diamond Push-Ups", 3, "8", 60, 90, "Feet elevated, close grip", "push_up_decline_diamond"),
                ProgramExercise("Tempo Push-Ups (3s down)", 3, "8", 60, 90, "Slow eccentric", "push_up_tempo"),
                ProgramExercise("L-Sit Hold", 3, "15s", 30, 60, "Legs straight if possible", "l_sit_hold", isTimed = true, metValue = 3.0f),
                ProgramExercise("V-Ups", 3, "12", 30, 60, "Touch toes at top", "v_up", metValue = 3.0f)
            )),
            ProgramDay(2, "LEGS + POWER", listOf(
                ProgramExercise("Squat Jumps", 5, "12", 60, 90, "Max height each rep", "squat_jump", metValue = 8.0f),
                ProgramExercise("Pistol Squats (assisted)", 4, "6/leg", 60, 90, "Use wall or doorframe", "pistol_squat_assisted", metValue = 5.0f),
                ProgramExercise("Nordic Curl Negatives", 3, "5", 60, 90, "Slow 5s descent", "nordic_curl", metValue = 5.0f),
                ProgramExercise("Step-Ups", 3, "12/leg", 45, 90, "Drive through the heel", "step_up", metValue = 5.0f),
                ProgramExercise("Wall Sit Hold", 3, "45s", 30, 60, "Add arm raises for difficulty", "wall_sit", isTimed = true, metValue = 3.0f),
                ProgramExercise("Tuck Jumps", 3, "10", 60, 90, "Knees to chest", "tuck_jump", metValue = 8.0f)
            )),
            ProgramDay(3, "ACTIVE RECOVERY", listOf(
                ProgramExercise("Light Jog or Walk", 1, "25 min", 0, 0, "Conversational pace", "walk_jog", isTimed = true, metValue = 3.0f),
                ProgramExercise("Dynamic Stretching", 1, "15 min", 0, 0, "Leg swings, arm circles", "stretch_dynamic", isTimed = true, metValue = 2.5f),
                ProgramExercise("Push-Up Greasing the Groove", 5, "10", 0, 0, "Never go to failure", "push_up_standard")
            ), isRestDay = true),
            ProgramDay(4, "FULL UPPER", listOf(
                ProgramExercise("Push-Ups", 4, "20", 60, 90, "Getting close to the goal", "push_up_standard"),
                ProgramExercise("Handstand Wall Hold", 3, "20s", 45, 90, "Face the wall", "handstand_wall_hold", isTimed = true, metValue = 3.8f),
                ProgramExercise("Pike Push-Ups", 4, "10", 60, 90, "Deeper range", "push_up_pike"),
                ProgramExercise("Inverted Rows (table)", 4, "12", 45, 90, "Body straight like a plank", "inverted_row", metValue = 3.8f),
                ProgramExercise("Plank Up-Downs", 3, "12", 30, 60, "Alternate leading arm", "plank_up_down", metValue = 3.0f),
                ProgramExercise("Dragon Flags (progression)", 3, "5", 60, 90, "Tuck knees if needed", "dragon_flag", metValue = 3.8f)
            )),
            ProgramDay(5, "LEGS + HIIT", listOf(
                ProgramExercise("Bulgarian Split Squats", 4, "12/leg", 60, 90, "Deep stretch at bottom", "bulgarian_split_squat", metValue = 5.0f),
                ProgramExercise("Broad Jumps", 4, "8", 60, 90, "Stick the landing", "broad_jump", metValue = 8.0f),
                ProgramExercise("Hip Thrusts (elevated)", 4, "15", 45, 90, "Shoulders on couch/bench", "hip_thrust", metValue = 3.0f),
                ProgramExercise("HIIT Circuit", 3, "30s each", 10, 60, "Burpees→Squats→High Knees→Lunges", "hiit_circuit", isTimed = true, metValue = 8.0f),
                ProgramExercise("Calf Raises (single leg)", 3, "15/leg", 30, 60, "Full ROM", "calf_raise_single", metValue = 3.0f)
            )),
            ProgramDay(6, "PUSH-UP VOLUME DAY", listOf(
                ProgramExercise("Push-Ups", 4, "25", 90, 120, "Break into micro-sets if needed", "push_up_standard"),
                ProgramExercise("Close-Grip Push-Ups", 3, "10", 45, 90, "Tricep focus", "push_up_close_grip"),
                ProgramExercise("Wide Push-Ups", 3, "12", 45, 90, "Chest focus", "push_up_wide"),
                ProgramExercise("Plank Hold", 3, "60s", 30, 60, "One minute, no breaks", "plank_hold", isTimed = true),
                ProgramExercise("Hollow Body Hold", 3, "20s", 30, 60, "Press lower back down", "hollow_body_hold", isTimed = true, metValue = 3.0f)
            )),
            ProgramDay(7, "FULL REST", listOf(
                ProgramExercise("Complete Rest", 1, "—", 0, 0, "Trust the process.", "rest_day", metValue = 1.0f)
            ), isRestDay = true)
        )
    )

    // ========== WEEK 4 — THE CENTURY ==========
    private val week4 = ProgramWeek(
        weekNumber = 4,
        title = "THE CENTURY",
        pushUpTarget = "100 total → 100 straight",
        days = listOf(
            ProgramDay(1, "PUSH — DENSITY TRAINING", listOf(
                ProgramExercise("Push-Ups", 1, "100 total", 30, 120, "Time yourself. Rest as little as possible.", "push_up_standard"),
                ProgramExercise("Archer Push-Ups", 3, "6/side", 60, 90, "Unilateral strength", "push_up_archer"),
                ProgramExercise("Plank Shoulder Taps", 3, "20", 30, 60, "Zero hip rotation", "plank_shoulder_tap", metValue = 3.0f),
                ProgramExercise("Leg Raises", 3, "15", 30, 60, "Control every inch", "leg_raise", metValue = 3.0f)
            )),
            ProgramDay(2, "LEGS + EXPLOSIVE", listOf(
                ProgramExercise("Pistol Squats", 4, "6/leg", 60, 90, "Full depth", "pistol_squat_assisted", metValue = 5.0f),
                ProgramExercise("Box Jump Burpees", 4, "8", 60, 90, "Combo movement", "box_jump_burpee", metValue = 8.0f),
                ProgramExercise("Walking Lunges", 4, "16/leg", 45, 90, "Long steps", "lunge_walking", metValue = 5.0f),
                ProgramExercise("Single-Leg Calf Raises", 4, "15/leg", 30, 60, "Slow & controlled", "calf_raise_single", metValue = 3.0f),
                ProgramExercise("Sprint Intervals", 8, "20s on / 40s off", 0, 120, "Leave nothing", "sprint_interval", isTimed = true, metValue = 8.0f)
            )),
            ProgramDay(3, "ACTIVE RECOVERY", listOf(
                ProgramExercise("Easy Walk or Swim", 1, "30 min", 0, 0, "Flush the muscles", "walk_jog", isTimed = true, metValue = 3.0f),
                ProgramExercise("Full Body Stretch", 1, "15 min", 0, 0, "Deep holds, breathe", "stretch_full_body", isTimed = true, metValue = 2.5f),
                ProgramExercise("Greasing the Groove Push-Ups", 8, "10", 0, 0, "Never near failure", "push_up_standard")
            ), isRestDay = true),
            ProgramDay(4, "UPPER BODY — PEAK", listOf(
                ProgramExercise("Push-Ups", 2, "50", 120, 120, "Two big sets. Push through.", "push_up_standard"),
                ProgramExercise("Handstand Push-Up Negatives", 3, "3", 90, 90, "Against wall, 5s down", "handstand_pushup_negative"),
                ProgramExercise("Inverted Rows", 4, "12", 45, 90, "Table or sturdy bar", "inverted_row", metValue = 3.8f),
                ProgramExercise("Superman + Push-Up Combo", 3, "8", 60, 90, "Superman then push-up", "superman_pushup_combo"),
                ProgramExercise("Ab Wheel / Towel Rollouts", 3, "10", 45, 60, "Full extension", "ab_rollout_towel", metValue = 3.8f)
            )),
            ProgramDay(5, "FULL BODY BURN", listOf(
                ProgramExercise("Circuit: Push-Ups", 5, "20", 10, 90, "5 rounds, minimal rest", "push_up_standard"),
                ProgramExercise("Circuit: Jump Squats", 5, "15", 10, 10, "Part of circuit", "squat_jump", metValue = 8.0f),
                ProgramExercise("Circuit: Burpees", 5, "10", 10, 10, "Part of circuit", "burpee", metValue = 8.0f),
                ProgramExercise("Circuit: Mountain Climbers", 5, "20", 10, 10, "Part of circuit", "mountain_climber", metValue = 8.0f),
                ProgramExercise("Circuit: Plank Hold", 5, "30s", 10, 90, "End of each round", "plank_hold", isTimed = true)
            )),
            ProgramDay(6, "THE 100 PUSH-UP CHALLENGE", listOf(
                ProgramExercise("Warm-Up: Arm Circles + Light Push-Ups", 2, "10", 30, 60, "Get blood flowing", "warmup_arm_circles"),
                ProgramExercise("100 CONSECUTIVE PUSH-UPS", 1, "100", 0, 0, "If you stall, hold plank and continue. YOU GOT THIS.", "push_up_standard", isMaxTest = true),
                ProgramExercise("Celebration Stretch", 1, "15 min", 0, 0, "You earned it. Stretch everything.", "stretch_full_body", isTimed = true, metValue = 2.5f)
            )),
            ProgramDay(7, "VICTORY REST", listOf(
                ProgramExercise("You did it.", 1, "—", 0, 0, "Reflect. Recover. Plan what's next.", "rest_day", metValue = 1.0f)
            ), isRestDay = true)
        )
    )
}
