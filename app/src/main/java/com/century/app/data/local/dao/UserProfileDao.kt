package com.century.app.data.local.dao

import androidx.room.*
import com.century.app.data.local.entity.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileOnce(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile): Long

    @Update
    suspend fun updateProfile(profile: UserProfile)

    @Query("UPDATE user_profile SET currentDay = :day, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateCurrentDay(day: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM user_profile")
    suspend fun getProfileCount(): Int
}
