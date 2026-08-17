package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.OilLog
import kotlinx.coroutines.flow.Flow

@Dao
interface OilDao {
    @Query("SELECT * FROM oil_logs ORDER BY tanggal DESC, id DESC")
    fun getAllLogs(): Flow<List<OilLog>>

    @Query("SELECT * FROM oil_logs ORDER BY tanggal DESC, id DESC")
    suspend fun getAllLogsList(): List<OilLog>

    @Query("SELECT * FROM oil_logs WHERE jenis_oli = :jenis ORDER BY tanggal DESC, id DESC LIMIT 1")
    suspend fun getLatestLogByJenis(jenis: String): OilLog?

    @Query("SELECT * FROM oil_logs ORDER BY tanggal DESC, id DESC LIMIT 1")
    suspend fun getLatestLog(): OilLog?

    @Query("SELECT * FROM oil_logs WHERE vehicle_id = :vehicleId ORDER BY tanggal DESC, id DESC LIMIT 1")
    suspend fun getLatestLogByVehicle(vehicleId: Int): OilLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: OilLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<OilLog>)

    @Query("DELETE FROM oil_logs")
    suspend fun clearAll()

    @Delete
    suspend fun deleteLog(log: OilLog)

    @Query("DELETE FROM oil_logs WHERE id = :id")
    suspend fun deleteLogById(id: Int)
}
