package com.century.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.century.app.data.local.dao.*
import com.century.app.data.local.entity.*

@Database(
    entities = [
        UserProfile::class,
        WeightLog::class,
        WorkoutSession::class,
        ExerciseLog::class,
        ExerciseImage::class,
        PushUpTest::class
    ],
    version = 1,
    exportSchema = true
)
abstract class CenturyDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun weightLogDao(): WeightLogDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun exerciseLogDao(): ExerciseLogDao
    abstract fun exerciseImageDao(): ExerciseImageDao
    abstract fun pushUpTestDao(): PushUpTestDao
}
