package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_logs")
data class NotificationLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packageName: String,
    val appName: String,
    val title: String,
    val message: String,
    val bigText: String?,
    val subText: String?,
    val receivedAt: String,
    val androidVersion: String,
    val deviceBrand: String,
    val deviceModel: String,
    val httpStatus: Int?,
    val retryCount: Int,
    val status: String, // "SUCCESS", "FAILED", "PENDING_RETRY"
    val errorMessage: String? = null
)
