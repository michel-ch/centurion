package com.century.app.di

import android.content.Context
import androidx.room.Room
import com.century.app.data.local.CenturyDatabase
import com.century.app.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CenturyDatabase {
        return Room.databaseBuilder(
            context,
            CenturyDatabase::class.java,
            "century_database"
        ).build()
    }

    @Provides
    fun provideUserProfileDao(db: CenturyDatabase): UserProfileDao = db.userProfileDao()

    @Provides
    fun provideWeightLogDao(db: CenturyDatabase): WeightLogDao = db.weightLogDao()

    @Provides
    fun provideWorkoutSessionDao(db: CenturyDatabase): WorkoutSessionDao = db.workoutSessionDao()

    @Provides
    fun provideExerciseLogDao(db: CenturyDatabase): ExerciseLogDao = db.exerciseLogDao()

    @Provides
    fun provideExerciseImageDao(db: CenturyDatabase): ExerciseImageDao = db.exerciseImageDao()

    @Provides
    fun providePushUpTestDao(db: CenturyDatabase): PushUpTestDao = db.pushUpTestDao()
}
