package com.example.service

import android.app.Notification
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import android.provider.Settings
import com.example.network.NetworkClient
import com.example.network.NotificationPayload
import com.example.model.NotificationLog
import com.example.storage.AppDatabase
import com.example.storage.EncryptedSharedPreferencesManager
import com.example.storage.NotificationRepository
import com.example.worker.NotificationUploadWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class MyNotificationListenerService : NotificationListenerService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private lateinit var prefs: EncryptedSharedPreferencesManager
    private lateinit var repository: NotificationRepository

    override fun onCreate() {
        super.onCreate()
        prefs = EncryptedSharedPreferencesManager(applicationContext)
        val database = AppDatabase.getDatabase(applicationContext)
        repository = NotificationRepository(database.notificationLogDao())
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d("NotificationListener", "Service Bound")
        return super.onBind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("NotificationListener", "onStartCommand received")
        if (prefs.monitoringEnabled) {
            showForegroundNotification()
            enqueueUploadWork()
        }
        return START_STICKY
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("NotificationListener", "Notification Listener Connected")
        if (prefs.monitoringEnabled) {
            showForegroundNotification()
            enqueueUploadWork()
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d("NotificationListener", "Notification Listener Disconnected")
    }

    private fun showForegroundNotification() {
        val channelId = "monitoring_service_channel"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Invitnesia Monitoring Bridge",
                android.app.NotificationManager.IMPORTANCE_MIN
            )
            val manager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            manager.createNotificationChannel(channel)
        }

        val pendingIntent = android.app.PendingIntent.getActivity(
            this, 0,
            android.content.Intent(this, com.example.MainActivity::class.java),
            android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = androidx.core.app.NotificationCompat.Builder(this, channelId)
            .setContentTitle("Invitnesia Notif")
            .setContentText("Pemantauan Notifikasi E-Wallet Sedang Aktif")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(99, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
            } else {
                startForeground(99, notification)
            }
        } catch (e: Exception) {
            Log.e("NotificationListener", "Failed to start foreground service: ${e.message}", e)
            try {
                val manager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                manager.notify(99, notification)
                Log.d("NotificationListener", "Showing regular fallback notification instead.")
            } catch (ne: Exception) {
                Log.e("NotificationListener", "Failed to show fallback notification: ${ne.message}", ne)
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        if (!prefs.monitoringEnabled) {
            Log.d("NotificationListener", "Monitoring is inactive. Ignoring notification.")
            return
        }

        val packageName = sbn.packageName ?: return

        // Do not log or bridge notifications from this app itself
        val myPackageName = applicationContext.packageName
        val configuredAppId = prefs.applicationId
        if (packageName == myPackageName || (configuredAppId.isNotEmpty() && packageName == configuredAppId)) {
            Log.d("NotificationListener", "Skipping notification from our own application package: $packageName")
            return
        }

        // Fetch the monitored package list from encrypted settings
        val monitoredPackages = prefs.monitoringPackages

        // Check if this package should be monitored. If empty, all packages are monitored.
        if (monitoredPackages.isNotEmpty() && !monitoredPackages.contains(packageName)) {
            Log.d("NotificationListener", "Skipping package not in monitoring list: $packageName")
            return
        }

        scope.launch {
            try {
                val extras = sbn.notification.extras ?: android.os.Bundle()

                // Extract fields safely
                val appLabel = try {
                    val pm = packageManager
                    val appInfo = pm.getApplicationInfo(packageName, 0)
                    pm.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    packageName
                }

                val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
                val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
                val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
                val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() ?: ""

                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
                val receivedAt = dateFormat.format(Date(sbn.postTime))

                val logEntry = NotificationLog(
                    packageName = packageName,
                    appName = appLabel,
                    title = title,
                    message = text,
                    bigText = bigText.ifEmpty { null },
                    subText = subText.ifEmpty { null },
                    receivedAt = receivedAt,
                    androidVersion = Build.VERSION.RELEASE ?: "unknown",
                    deviceBrand = Build.BRAND ?: "unknown",
                    deviceModel = Build.MODEL ?: "unknown",
                    httpStatus = null,
                    retryCount = 0,
                    status = "PENDING_RETRY",
                    errorMessage = null
                )

                // Save to Room DB
                repository.insertLog(logEntry)

                // Enqueue work to upload all pending notifications in background immediately
                enqueueUploadWork()

            } catch (e: Exception) {
                Log.e("NotificationListener", "Error processing notification", e)
            }
        }
    }

    private fun enqueueUploadWork() {
        val uploadWorkRequest = OneTimeWorkRequestBuilder<NotificationUploadWorker>()
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                10,
                TimeUnit.SECONDS
            )
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            "notification_upload_work",
            ExistingWorkPolicy.REPLACE,
            uploadWorkRequest
        )
    }

    private fun showErrorNotification(title: String, message: String) {
        val channelId = "error_service_channel"
        val manager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Bridge Error Notifications",
                android.app.NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val pendingIntent = android.app.PendingIntent.getActivity(
            this, 0,
            android.content.Intent(this, com.example.MainActivity::class.java),
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = androidx.core.app.NotificationCompat.Builder(this, channelId)
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
            Log.e("NotificationListener", "Failed to show error notification", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
