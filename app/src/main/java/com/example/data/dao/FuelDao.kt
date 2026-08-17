package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.FuelLog
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelDao {
    @Query("SELECT * FROM fuel_logs ORDER BY tanggal DESC, id DESC")
    fun getAllLogs(): Flow<List<FuelLog>>

    @Query("SELECT * FROM fuel_logs ORDER BY tanggal DESC, id DESC")
    suspend fun getAllLogsList(): List<FuelLog>

    @Query("SELECT * FROM fuel_logs ORDER BY tanggal DESC, id DESC LIMIT 1")
    suspend fun getLatestLog(): FuelLog?

    @Query("SELECT * FROM fuel_logs WHERE vehicle_id = :vehicleId ORDER BY tanggal DESC, id DESC LIMIT 1")
    suspend fun getLatestLogByVehicle(vehicleId: Int): FuelLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: FuelLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<FuelLog>)

    @Query("DELETE FROM fuel_logs")
    suspend fun clearAll()

    @Delete
    suspend fun deleteLog(log: FuelLog)

    @Query("DELETE FROM fuel_logs WHERE id = :id")
    suspend fun deleteLogById(id: Int)
}
