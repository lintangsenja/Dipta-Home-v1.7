package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.SocialLog
import kotlinx.coroutines.flow.Flow

@Dao
interface SocialDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SocialLog)

    @Update
    suspend fun updateLog(log: SocialLog)

    @Query("DELETE FROM social_logs WHERE id = :id")
    suspend fun deleteLogById(id: Int)

    @Query("DELETE FROM social_logs")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<SocialLog>)

    @Query("SELECT * FROM social_logs ORDER BY tanggal DESC")
    fun getAllLogs(): Flow<List<SocialLog>>

    @Query("SELECT * FROM social_logs ORDER BY tanggal DESC")
    suspend fun getAllLogsList(): List<SocialLog>
}
