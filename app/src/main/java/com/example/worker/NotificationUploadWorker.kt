package com.example.worker

import android.content.Context
import android.provider.Settings
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.model.NotificationLog
import com.example.network.NetworkClient
import com.example.network.NotificationPayload
import com.example.storage.AppDatabase
import com.example.storage.EncryptedSharedPreferencesManager
import com.example.storage.NotificationRepository
import java.io.IOException

class NotificationUploadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = EncryptedSharedPreferencesManager(applicationContext)
        if (!prefs.monitoringEnabled) {
            return Result.success()
        }

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = NotificationRepository(database.notificationLogDao())
        val networkClient = NetworkClient(applicationContext, prefs)

        val pendingLogs = repository.getPendingLogs()
        if (pendingLogs.isEmpty()) {
            return Result.success()
        }

        val deviceId = Settings.Secure.getString(
            applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown_device"

        var hasFailures = false

        for (log in pendingLogs) {
            val payload = NotificationPayload(
                deviceId = deviceId,
                deviceName = prefs.deviceName,
                packageName = log.packageName,
                applicationName = log.appName,
                title = log.title,
                message = log.message,
                bigText = log.bigText,
                subText = log.subText,
                receivedAt = log.receivedAt,
                androidVersion = log.androidVersion,
                deviceBrand = log.deviceBrand,
                deviceModel = log.deviceModel
            )

            val sendResult = networkClient.sendNotification(payload)
            if (sendResult.isSuccess) {
                val updatedLog = log.copy(
                    status = "SUCCESS",
                    httpStatus = sendResult.getOrNull() ?: 200
                )
                repository.updateLog(updatedLog)
            } else {
                val nextRetryCount = log.retryCount + 1
                val isMaxExceeded = nextRetryCount >= prefs.retryCount
                val finalStatus = if (isMaxExceeded) "FAILED" else "PENDING_RETRY"
                val httpErrorStatus = when (val exception = sendResult.exceptionOrNull()) {
                    is IOException -> null // Network/Socket timeout, no status code
                    else -> {
                        val msg = exception?.message ?: ""
                        if (msg.contains("HTTP error code:")) {
                            msg.substringAfter("HTTP error code:").trim().toIntOrNull()
                        } else {
                            null
                        }
                    }
                }

                val updatedLog = log.copy(
                    status = finalStatus,
                    retryCount = nextRetryCount,
                    httpStatus = httpErrorStatus ?: log.httpStatus
                )
                repository.updateLog(updatedLog)

                if (!isMaxExceeded) {
                    hasFailures = true
                }
            }
        }

        return if (hasFailures) {
            Result.retry()
        } else {
            Result.success()
        }
    }
}
