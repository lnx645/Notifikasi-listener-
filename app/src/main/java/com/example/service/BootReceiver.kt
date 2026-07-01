package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.storage.EncryptedSharedPreferencesManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Boot completed broadcast received")
            val prefs = EncryptedSharedPreferencesManager(context)
            if (prefs.autoStartAfterBoot && prefs.monitoringEnabled) {
                try {
                    val componentName = android.content.ComponentName(
                        context,
                        MyNotificationListenerService::class.java
                    )
                    android.service.notification.NotificationListenerService.requestRebind(componentName)
                    Log.d("BootReceiver", "Successfully requested rebind for MyNotificationListenerService")
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Failed to requestRebind MyNotificationListenerService on boot", e)
                }
            }
        }
    }
}
