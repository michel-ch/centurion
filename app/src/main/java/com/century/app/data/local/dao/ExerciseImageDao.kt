package com.century.app.data.local.dao

import androidx.room.*
import com.century.app.data.local.entity.ExerciseImage
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseImageDao {
    @Query("SELECT * FROM exercise_image WHERE illustrationId = :illustrationId LIMIT 1")
    suspend fun getImageForExercise(illustrationId: String): ExerciseImage?

    @Query("SELECT * FROM exercise_image WHERE isCustom = 1")
    fun getAllCustomImages(): Flow<List<ExerciseImage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: ExerciseImage): Long

    @Query("DELETE FROM exercise_image WHERE illustrationId = :illustrationId")
    suspend fun deleteImage(illustrationId: String)
}
