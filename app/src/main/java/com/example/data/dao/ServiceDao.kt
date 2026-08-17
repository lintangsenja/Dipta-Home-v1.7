package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.ServiceLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ServiceLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<ServiceLog>)

    @Query("DELETE FROM service_logs WHERE id = :id")
    suspend fun deleteLogById(id: Int)

    @Query("DELETE FROM service_logs")
    suspend fun deleteAll()

    @Query("SELECT * FROM service_logs WHERE vehicle_id = :vehicleId ORDER BY tanggal DESC")
    fun getLogsByVehicle(vehicleId: Int): Flow<List<ServiceLog>>

    @Query("SELECT * FROM service_logs ORDER BY tanggal DESC")
    fun getAllLogs(): Flow<List<ServiceLog>>

    @Query("SELECT * FROM service_logs ORDER BY tanggal DESC LIMIT 1")
    suspend fun getLatestLog(): ServiceLog?

    @Query("SELECT * FROM service_logs ORDER BY id ASC")
    suspend fun getAllLogsList(): List<ServiceLog>
}
