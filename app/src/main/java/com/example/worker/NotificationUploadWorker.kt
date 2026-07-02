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
                    httpStatus = sendResult.getOrNull() ?: 200,
                    errorMessage = null
                )
                repository.updateLog(updatedLog)
            } else {
                val nextRetryCount = log.retryCount + 1
                val isMaxExceeded = nextRetryCount >= prefs.retryCount
                val finalStatus = if (isMaxExceeded) "FAILED" else "PENDING_RETRY"
                val exception = sendResult.exceptionOrNull()
                val errorMsgText = exception?.message ?: "Unknown error"
                val httpErrorStatus = when (exception) {
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
                    httpStatus = httpErrorStatus ?: log.httpStatus,
                    errorMessage = errorMsgText
                )
                repository.updateLog(updatedLog)

                // Post a local push notification showing the error detail
                showErrorNotification(
                    "Gagal Meneruskan Notifikasi",
                    "Aplikasi: ${log.appName}\nError: $errorMsgText"
                )

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

    private fun showErrorNotification(title: String, message: String) {
        val channelId = "error_service_channel"
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Bridge Error Notifications",
                android.app.NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val pendingIntent = android.app.PendingIntent.getActivity(
            applicationContext, 0,
            android.content.Intent(applicationContext, com.example.MainActivity::class.java),
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = androidx.core.app.NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .build()

        try {
            manager.notify(101, notification)
        } catch (e: Exception) {
            android.util.Log.e("NotificationWorker", "Failed to show error notification", e)
        }
    }
}
