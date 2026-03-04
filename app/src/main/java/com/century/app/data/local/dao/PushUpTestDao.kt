package com.century.app.data.local.dao

import androidx.room.*
import com.century.app.data.local.entity.PushUpTest
import kotlinx.coroutines.flow.Flow

@Dao
interface PushUpTestDao {
    @Query("SELECT * FROM push_up_test ORDER BY testedAt ASC")
    fun getAllTests(): Flow<List<PushUpTest>>

    @Query("SELECT * FROM push_up_test WHERE weekNumber = :week LIMIT 1")
    suspend fun getTestForWeek(week: Int): PushUpTest?

    @Insert
    suspend fun insertTest(test: PushUpTest): Long

    @Update
    suspend fun updateTest(test: PushUpTest)
}
