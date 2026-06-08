package com.century.app.data.local.dao

import androidx.room.*
import com.century.app.data.local.entity.ExerciseImage
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ExerciseImageDao {
    @Query("SELECT * FROM exercise_image WHERE illustrationId = :illustrationId LIMIT 1")
    abstract suspend fun getImageForExercise(illustrationId: String): ExerciseImage?

    @Query("SELECT * FROM exercise_image WHERE isCustom = 1")
    abstract fun getAllCustomImages(): Flow<List<ExerciseImage>>

    @Transaction
    open suspend fun insertImage(image: ExerciseImage): Long {
        val insertedId = insertImageIgnoringConflict(image)
        if (insertedId != -1L) return insertedId

        updateImageByIllustrationId(
            illustrationId = image.illustrationId,
            customImageUri = image.customImageUri,
            isCustom = image.isCustom,
            updatedAt = image.updatedAt
        )

        return getImageId(image.illustrationId)
            ?: error("Exercise image upsert failed for ${image.illustrationId}")
    }

    @Query("DELETE FROM exercise_image WHERE illustrationId = :illustrationId")
    abstract suspend fun deleteImage(illustrationId: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertImageIgnoringConflict(image: ExerciseImage): Long

    @Query(
        """
        UPDATE exercise_image
        SET customImageUri = :customImageUri,
            isCustom = :isCustom,
            updatedAt = :updatedAt
        WHERE illustrationId = :illustrationId
        """
    )
    protected abstract suspend fun updateImageByIllustrationId(
        illustrationId: String,
        customImageUri: String?,
        isCustom: Boolean,
        updatedAt: Long
    ): Int

    @Query("SELECT id FROM exercise_image WHERE illustrationId = :illustrationId")
    protected abstract suspend fun getImageId(illustrationId: String): Long?
}
