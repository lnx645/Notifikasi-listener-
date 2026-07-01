package com.example.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.NotificationLog
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationLogDao {
    @Query("SELECT * FROM notification_logs ORDER BY id DESC")
    fun getAllLogs(): Flow<List<NotificationLog>>

    @Query("SELECT * FROM notification_logs WHERE status = 'PENDING_RETRY'")
    suspend fun getPendingLogs(): List<NotificationLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: NotificationLog): Long

    @Update
    suspend fun updateLog(log: NotificationLog)

    @Query("SELECT COUNT(*) FROM notification_logs WHERE status = 'PENDING_RETRY'")
    fun getPendingLogsCount(): Flow<Int>

    @Query("SELECT * FROM notification_logs ORDER BY id DESC LIMIT 1")
    fun getLatestLog(): Flow<NotificationLog?>

    @Query("SELECT COUNT(*) FROM notification_logs WHERE receivedAt LIKE :todayPrefix || '%'")
    fun getTodayLogsCount(todayPrefix: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM notification_logs WHERE status = 'SUCCESS' AND receivedAt LIKE :todayPrefix || '%'")
    fun getTodaySuccessCount(todayPrefix: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM notification_logs WHERE status = 'FAILED' AND receivedAt LIKE :todayPrefix || '%'")
    fun getTodayFailedCount(todayPrefix: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM notification_logs")
    fun getTotalLogsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notification_logs WHERE status = 'SUCCESS'")
    fun getSuccessLogsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notification_logs WHERE status = 'FAILED'")
    fun getFailedLogsCount(): Flow<Int>

    @Query("DELETE FROM notification_logs")
    suspend fun clearAllLogs()
}
