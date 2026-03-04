package com.century.app.data.local.dao

import androidx.room.*
import com.century.app.data.local.entity.WeightLog
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightLogDao {
    @Query("SELECT * FROM weight_log ORDER BY loggedAt DESC")
    fun getAllWeightLogs(): Flow<List<WeightLog>>

    @Query("SELECT * FROM weight_log ORDER BY loggedAt DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 30): Flow<List<WeightLog>>

    @Query("SELECT * FROM weight_log ORDER BY loggedAt DESC LIMIT 1")
    suspend fun getLatestLog(): WeightLog?

    @Insert
    suspend fun insertLog(log: WeightLog): Long

    @Update
    suspend fun updateLog(log: WeightLog)

    @Delete
    suspend fun deleteLog(log: WeightLog)

    @Query("SELECT * FROM weight_log WHERE id = :id")
    suspend fun getLogById(id: Long): WeightLog?
}
