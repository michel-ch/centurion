package com.century.app.data.local.dao

import androidx.room.*
import com.century.app.data.local.entity.PushUpTest
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PushUpTestDao {
    @Query("SELECT * FROM push_up_test ORDER BY testedAt ASC")
    abstract fun getAllTests(): Flow<List<PushUpTest>>

    @Query("SELECT * FROM push_up_test WHERE userId = 1 AND weekNumber = :week LIMIT 1")
    abstract suspend fun getTestForWeek(week: Int): PushUpTest?

    @Transaction
    open suspend fun insertTest(test: PushUpTest): Long {
        val insertedId = insertTestIgnoringConflict(test)
        if (insertedId != -1L) return insertedId

        updateTestByWeek(
            userId = test.userId,
            weekNumber = test.weekNumber,
            maxReps = test.maxReps,
            testedAt = test.testedAt
        )

        return getTestId(test.userId, test.weekNumber)
            ?: error("Push-up test upsert failed for week ${test.weekNumber}")
    }

    @Update
    abstract suspend fun updateTest(test: PushUpTest)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertTestIgnoringConflict(test: PushUpTest): Long

    @Query(
        """
        UPDATE push_up_test
        SET maxReps = :maxReps,
            testedAt = :testedAt
        WHERE userId = :userId AND weekNumber = :weekNumber
        """
    )
    protected abstract suspend fun updateTestByWeek(
        userId: Long,
        weekNumber: Int,
        maxReps: Int,
        testedAt: Long
    ): Int

    @Query("SELECT id FROM push_up_test WHERE userId = :userId AND weekNumber = :weekNumber")
    protected abstract suspend fun getTestId(userId: Long, weekNumber: Int): Long?
}
