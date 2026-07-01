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

    // Connection & Service states
    private val _isServiceEnabled = MutableStateFlow(false)
    val isServiceEnabled: StateFlow<Boolean> = _isServiceEnabled.asStateFlow()

    private val _isSyncingPackages = MutableStateFlow(false)
    val isSyncingPackages: StateFlow<Boolean> = _isSyncingPackages.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

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

    init {
        loadSettings()
        checkServiceStatus()
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
    }

    fun updateSettings(
        backendUrl: String,
        authToken: String,
        deviceName: String,
        syncPackages: Boolean,
        retryCount: Int,
        connectionTimeout: Int,
        monitoringPackages: List<String>
    ) {
        prefs.backendUrl = backendUrl
        prefs.authToken = authToken
        prefs.deviceName = deviceName
        prefs.syncPackages = syncPackages
        prefs.retryCount = retryCount
        prefs.connectionTimeout = connectionTimeout
        prefs.monitoringPackages = monitoringPackages

        loadSettings()
        viewModelScope.launch {
            _toastMessage.emit("Settings saved successfully")
        }
    }

    fun syncPackagesFromServer() {
        viewModelScope.launch {
            _isSyncingPackages.value = true
            val result = networkClient.syncPackages()
            _isSyncingPackages.value = false

            if (result.isSuccess) {
                val packages = result.getOrThrow()
                if (packages.isNotEmpty()) {
                    prefs.monitoringPackages = packages
                    _monitoringPackages.value = packages
                    _toastMessage.emit("Successfully synchronized ${packages.size} package(s)")
                } else {
                    _toastMessage.emit("Config received empty package list")
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Unknown network error"
                _toastMessage.emit("Sync failed: $errorMsg")
            }
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearAllLogs()
            _toastMessage.emit("All logs cleared")
        }
    }

    fun triggerManualRetry() {
        viewModelScope.launch {
            val pendingCount = repository.getPendingLogs().size
            if (pendingCount == 0) {
                _toastMessage.emit("No pending logs to upload")
                return@launch
            }

            val uploadWorkRequest = OneTimeWorkRequestBuilder<NotificationUploadWorker>()
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(getApplication())
                .enqueueUniqueWork("notification_upload_work", ExistingWorkPolicy.REPLACE, uploadWorkRequest)

            _toastMessage.emit("Enqueued manual upload for $pendingCount pending notification(s)")
        }
    }
}
