package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.model.NotificationLog
import com.example.network.NetworkClient
import com.example.network.NotificationPayload
import com.example.storage.AppDatabase
import com.example.storage.EncryptedSharedPreferencesManager
import com.example.storage.NotificationRepository
import com.example.worker.NotificationUploadWorker
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = EncryptedSharedPreferencesManager(application)
    private val database = AppDatabase.getDatabase(application)
    private val repository = NotificationRepository(database.notificationLogDao())
    private val networkClient = NetworkClient(application, prefs)

    // Reactive database flows
    val allLogs: StateFlow<List<NotificationLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalLogsCount: StateFlow<Int> = repository.totalCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val successLogsCount: StateFlow<Int> = repository.successCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val failedLogsCount: StateFlow<Int> = repository.failedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingLogsCount: StateFlow<Int> = repository.pendingCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val latestLog: StateFlow<NotificationLog?> = repository.latestLog
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Today's reactive statistics
    private val todayPrefix = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

    val todayTotalLogsCount: StateFlow<Int> = repository.getTodayCount(todayPrefix)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todaySuccessLogsCount: StateFlow<Int> = repository.getTodaySuccessCount(todayPrefix)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todayFailedLogsCount: StateFlow<Int> = repository.getTodayFailedCount(todayPrefix)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Connection & Service states
    private val _isServiceEnabled = MutableStateFlow(false)
    val isServiceEnabled: StateFlow<Boolean> = _isServiceEnabled.asStateFlow()

    private val _isMonitoringEnabled = MutableStateFlow(false)
    val isMonitoringEnabled: StateFlow<Boolean> = _isMonitoringEnabled.asStateFlow()

    private val _isSyncingPackages = MutableStateFlow(false)
    val isSyncingPackages: StateFlow<Boolean> = _isSyncingPackages.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    private val _serverConnectionStatus = MutableStateFlow("BELUM DIUJI")
    val serverConnectionStatus: StateFlow<String> = _serverConnectionStatus.asStateFlow()

    private val _isTestingApi = MutableStateFlow(false)
    val isTestingApi: StateFlow<Boolean> = _isTestingApi.asStateFlow()

    // Settings States
    private val _backendUrl = MutableStateFlow("")
    val backendUrl: StateFlow<String> = _backendUrl.asStateFlow()

    private val _authToken = MutableStateFlow("")
    val authToken: StateFlow<String> = _authToken.asStateFlow()

    private val _deviceName = MutableStateFlow("")
    val deviceName: StateFlow<String> = _deviceName.asStateFlow()

    private val _syncPackages = MutableStateFlow(true)
    val syncPackages: StateFlow<Boolean> = _syncPackages.asStateFlow()

    private val _retryCount = MutableStateFlow(3)
    val retryCount: StateFlow<Int> = _retryCount.asStateFlow()

    private val _connectionTimeout = MutableStateFlow(15)
    val connectionTimeout: StateFlow<Int> = _connectionTimeout.asStateFlow()

    private val _monitoringPackages = MutableStateFlow<List<String>>(emptyList())
    val monitoringPackages: StateFlow<List<String>> = _monitoringPackages.asStateFlow()

    private val _applicationId = MutableStateFlow("")
    val applicationId: StateFlow<String> = _applicationId.asStateFlow()

    private val _autoRetry = MutableStateFlow(true)
    val autoRetry: StateFlow<Boolean> = _autoRetry.asStateFlow()

    private val _autoStartAfterBoot = MutableStateFlow(true)
    val autoStartAfterBoot: StateFlow<Boolean> = _autoStartAfterBoot.asStateFlow()

    private val _debugMode = MutableStateFlow(false)
    val debugMode: StateFlow<Boolean> = _debugMode.asStateFlow()

    private val _lastSyncTime = MutableStateFlow("Belum pernah")
    val lastSyncTime: StateFlow<String> = _lastSyncTime.asStateFlow()

    init {
        loadSettings()
        checkServiceStatus()
        if (prefs.monitoringEnabled && _isServiceEnabled.value) {
            val context = getApplication<Application>()
            val intent = Intent(context, com.example.service.MyNotificationListenerService::class.java)
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Failed to start service on init", e)
            }
        }
    }

    fun checkServiceStatus() {
        val context = getApplication<Application>()
        val pkgName = context.packageName
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        var enabled = false
        if (!flat.isNullOrEmpty()) {
            val names = flat.split(":")
            for (name in names) {
                val cn = android.content.ComponentName.unflattenFromString(name)
                if (cn != null && cn.packageName == pkgName) {
                    enabled = true
                    break
                }
            }
        }
        _isServiceEnabled.value = enabled

        if (enabled) {
            try {
                val componentName = android.content.ComponentName(
                    context,
                    com.example.service.MyNotificationListenerService::class.java
                )
                android.service.notification.NotificationListenerService.requestRebind(componentName)
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Failed to requestRebind of MyNotificationListenerService", e)
            }
        } else {
            // Force monitoringEnabled to false if system permission is disabled
            if (prefs.monitoringEnabled) {
                prefs.monitoringEnabled = false
                _isMonitoringEnabled.value = false
            }
        }
    }

    fun setMonitoringEnabled(enabled: Boolean) {
        if (enabled && !_isServiceEnabled.value) {
            viewModelScope.launch {
                _toastMessage.emit("Gagal: Berikan izin Akses Notifikasi terlebih dahulu!")
            }
            return
        }
        prefs.monitoringEnabled = enabled
        _isMonitoringEnabled.value = enabled
        viewModelScope.launch {
            val context = getApplication<Application>()
            val intent = Intent(context, com.example.service.MyNotificationListenerService::class.java)
            if (enabled) {
                _toastMessage.emit("Pemantauan Aktif 🟢")
                checkServiceStatus()
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MainViewModel", "Failed to start service explicitly", e)
                }
            } else {
                _toastMessage.emit("Pemantauan Nonaktif 🔴")
                try {
                    context.stopService(intent)
                } catch (e: Exception) {
                    android.util.Log.e("MainViewModel", "Failed to stop service", e)
                }
            }
        }
    }

    fun openNotificationAccessSettings(context: Context) {
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun loadSettings() {
        _backendUrl.value = prefs.backendUrl
        _authToken.value = prefs.authToken
        _deviceName.value = prefs.deviceName
        _syncPackages.value = prefs.syncPackages
        _retryCount.value = prefs.retryCount
        _connectionTimeout.value = prefs.connectionTimeout
        _monitoringPackages.value = prefs.monitoringPackages
        _isMonitoringEnabled.value = prefs.monitoringEnabled
        _lastSyncTime.value = prefs.lastSyncTime
        _applicationId.value = prefs.applicationId.ifEmpty { getApplication<Application>().packageName }
        _autoRetry.value = prefs.autoRetry
        _autoStartAfterBoot.value = prefs.autoStartAfterBoot
        _debugMode.value = prefs.debugMode
    }

    fun updateSettings(
        backendUrl: String,
        authToken: String,
        deviceName: String,
        syncPackages: Boolean,
        retryCount: Int,
        connectionTimeout: Int,
        monitoringPackages: List<String>,
        applicationId: String,
        autoRetry: Boolean,
        autoStartAfterBoot: Boolean,
        debugMode: Boolean
    ) {
        prefs.backendUrl = backendUrl
        prefs.authToken = authToken
        prefs.deviceName = deviceName
        prefs.syncPackages = syncPackages
        prefs.retryCount = retryCount
        prefs.connectionTimeout = connectionTimeout
        prefs.monitoringPackages = monitoringPackages
        prefs.applicationId = applicationId
        prefs.autoRetry = autoRetry
        prefs.autoStartAfterBoot = autoStartAfterBoot
        prefs.debugMode = debugMode

        loadSettings()
        viewModelScope.launch {
            _toastMessage.emit("Konfigurasi disimpan successfully")
        }
    }

    fun updateMonitoringPackages(packages: List<String>) {
        prefs.monitoringPackages = packages
        _monitoringPackages.value = packages
        viewModelScope.launch {
            _toastMessage.emit("Daftar aplikasi dipantau diperbarui")
        }
    }

    fun resetConfiguration() {
        val context = getApplication<Application>()
        prefs.resetToDefaults(context)
        loadSettings()
        viewModelScope.launch {
            _toastMessage.emit("Semua konfigurasi direset ke bawaan")
        }
    }

    fun testApiConnection() {
        viewModelScope.launch {
            _isTestingApi.value = true
            _toastMessage.emit("Menguji koneksi server ke: ${prefs.backendUrl} ...")
            val result = networkClient.syncPackages()
            _isTestingApi.value = false
            if (result.isSuccess) {
                _serverConnectionStatus.value = "CONNECTED"
                _toastMessage.emit("Test API Berhasil: Server Terhubung! ✅")
            } else {
                _serverConnectionStatus.value = "DISCONNECTED"
                val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                _toastMessage.emit("Test API Gagal: $errorMsg ❌")
            }
        }
    }

    fun syncPackagesFromServer() {
        viewModelScope.launch {
            _isSyncingPackages.value = true
            val result = networkClient.syncPackages()
            _isSyncingPackages.value = false

            if (result.isSuccess) {
                val packages = result.getOrThrow()
                val format = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm:ss", java.util.Locale.getDefault())
                val nowStr = format.format(java.util.Date())
                prefs.lastSyncTime = nowStr
                _lastSyncTime.value = nowStr

                if (packages.isNotEmpty()) {
                    prefs.monitoringPackages = packages
                    _monitoringPackages.value = packages
                    _toastMessage.emit("Berhasil menyelaraskan ${packages.size} package")
                } else {
                    _toastMessage.emit("Penyelarasan sukses (daftar kosong)")
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Unknown network error"
                _toastMessage.emit("Penyelarasan gagal: $errorMsg")
            }
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearAllLogs()
            _toastMessage.emit("Semua log lokal berhasil dibersihkan")
        }
    }

    fun triggerManualRetry() {
        viewModelScope.launch {
            val pendingLogs = repository.getPendingLogs()
            if (pendingLogs.isEmpty()) {
                _toastMessage.emit("Tidak ada antrean log pending untuk dikirim")
                return@launch
            }

            _toastMessage.emit("Memulai pengiriman ulang ${pendingLogs.size} log secara langsung...")

            val deviceId = Settings.Secure.getString(
                getApplication<Application>().contentResolver,
                Settings.Secure.ANDROID_ID
            ) ?: "unknown_device"

            var successCount = 0
            var failCount = 0

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
                    successCount++
                    val updatedLog = log.copy(
                        status = "SUCCESS",
                        httpStatus = sendResult.getOrNull() ?: 200,
                        errorMessage = null
                    )
                    repository.updateLog(updatedLog)
                } else {
                    failCount++
                    val exception = sendResult.exceptionOrNull()
                    val errorMsgText = exception?.message ?: "Unknown error"
                    val httpErrorStatus = when (exception) {
                        is java.io.IOException -> null
                        else -> {
                            val msg = exception?.message ?: ""
                            if (msg.contains("HTTP error code:")) {
                                msg.substringAfter("HTTP error code:").trim().toIntOrNull()
                            } else {
                                null
                            }
                        }
                    }

                    val nextRetryCount = log.retryCount + 1
                    val isMaxExceeded = nextRetryCount >= prefs.retryCount
                    val finalStatus = if (isMaxExceeded) "FAILED" else "PENDING_RETRY"

                    val updatedLog = log.copy(
                        status = finalStatus,
                        retryCount = nextRetryCount,
                        httpStatus = httpErrorStatus ?: log.httpStatus,
                        errorMessage = errorMsgText
                    )
                    repository.updateLog(updatedLog)
                }
            }

            if (failCount == 0) {
                _toastMessage.emit("Berhasil mengirim ulang semua log! 🎉")
            } else {
                _toastMessage.emit("Selesai: $successCount berhasil, $failCount gagal. Periksa tab Logs untuk detail error.")
                // Enqueue background worker as backup
                val uploadWorkRequest = OneTimeWorkRequestBuilder<NotificationUploadWorker>()
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                    .build()
                WorkManager.getInstance(getApplication())
                    .enqueueUniqueWork("notification_upload_work", ExistingWorkPolicy.REPLACE, uploadWorkRequest)
            }
        }
    }
}
