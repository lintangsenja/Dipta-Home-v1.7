package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.ElectricityLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ElectricityDao {
    @Query("SELECT * FROM electricity_logs ORDER BY tanggal DESC, id DESC")
    fun getAllLogs(): Flow<List<ElectricityLog>>

    @Query("SELECT * FROM electricity_logs ORDER BY tanggal DESC, id DESC")
    suspend fun getAllLogsList(): List<ElectricityLog>

    @Query("SELECT * FROM electricity_logs ORDER BY tanggal DESC, id DESC LIMIT 1")
    suspend fun getLatestLog(): ElectricityLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ElectricityLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<ElectricityLog>)

    @Query("DELETE FROM electricity_logs")
    suspend fun clearAll()

    @Delete
    suspend fun deleteLog(log: ElectricityLog)

    @Query("DELETE FROM electricity_logs WHERE id = :id")
    suspend fun deleteLogById(id: Int)
}
