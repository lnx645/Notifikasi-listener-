package com.example.storage

import android.content.Context
import android.os.Build
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class EncryptedSharedPreferencesManager(context: Context) {

    private val sharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "secure_notification_bridge_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Fallback to standard SharedPreferences if Keystore is corrupted/unavailable
        context.getSharedPreferences("fallback_notification_bridge_prefs", Context.MODE_PRIVATE)
    }

    companion object {
        private const val KEY_BACKEND_URL = "backend_url"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_DEVICE_NAME = "device_name"
        private const val KEY_SYNC_PACKAGES = "sync_packages"
        private const val KEY_MONITORING_PACKAGES = "monitoring_packages"
        private const val KEY_RETRY_COUNT = "retry_count"
        private const val KEY_CONNECTION_TIMEOUT = "connection_timeout"

        private val DEFAULT_PACKAGES = listOf(
            "id.dana",
            "com.gojek.merchant",
            "com.gojek.app",
            "com.bca.mybca",
            "id.co.bankmandiri.livin",
            "id.co.bri.brimo",
            "id.bni.wondr"
        )
    }

    var backendUrl: String
        get() = sharedPreferences.getString(KEY_BACKEND_URL, "https://api.example.com") ?: "https://api.example.com"
        set(value) = sharedPreferences.edit().putString(KEY_BACKEND_URL, value).apply()

    var authToken: String
        get() = sharedPreferences.getString(KEY_AUTH_TOKEN, "") ?: ""
        set(value) = sharedPreferences.edit().putString(KEY_AUTH_TOKEN, value).apply()

    var deviceName: String
        get() = sharedPreferences.getString(KEY_DEVICE_NAME, "${Build.MANUFACTURER} ${Build.MODEL}") ?: "${Build.MANUFACTURER} ${Build.MODEL}"
        set(value) = sharedPreferences.edit().putString(KEY_DEVICE_NAME, value).apply()

    var syncPackages: Boolean
        get() = sharedPreferences.getBoolean(KEY_SYNC_PACKAGES, true)
        set(value) = sharedPreferences.edit().putBoolean(KEY_SYNC_PACKAGES, value).apply()

    var retryCount: Int
        get() = sharedPreferences.getInt(KEY_RETRY_COUNT, 3)
        set(value) = sharedPreferences.edit().putInt(KEY_RETRY_COUNT, value).apply()

    var connectionTimeout: Int
        get() = sharedPreferences.getInt(KEY_CONNECTION_TIMEOUT, 15)
        set(value) = sharedPreferences.edit().putInt(KEY_CONNECTION_TIMEOUT, value).apply()

    var monitoringPackages: List<String>
        get() {
            val raw = sharedPreferences.getString(KEY_MONITORING_PACKAGES, null)
            return if (raw.isNullOrBlank()) {
                DEFAULT_PACKAGES
            } else {
                raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            }
        }
        set(value) {
            val raw = value.joinToString(",")
            sharedPreferences.edit().putString(KEY_MONITORING_PACKAGES, raw).apply()
        }
}
