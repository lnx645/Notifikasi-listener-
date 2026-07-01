package com.example.storage

import com.example.model.NotificationLog
import kotlinx.coroutines.flow.Flow

class NotificationRepository(private val dao: NotificationLogDao) {
    val allLogs: Flow<List<NotificationLog>> = dao.getAllLogs()
    val totalCount: Flow<Int> = dao.getTotalLogsCount()
    val successCount: Flow<Int> = dao.getSuccessLogsCount()
    val failedCount: Flow<Int> = dao.getFailedLogsCount()

    suspend fun insertLog(log: NotificationLog): Long {
        return dao.insertLog(log)
    }

    suspend fun updateLog(log: NotificationLog) {
        dao.updateLog(log)
    }

    suspend fun getPendingLogs(): List<NotificationLog> {
        return dao.getPendingLogs()
    }

    suspend fun clearAllLogs() {
        dao.clearAllLogs()
    }
}
