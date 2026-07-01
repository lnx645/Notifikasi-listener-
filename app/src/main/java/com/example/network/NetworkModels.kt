package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NotificationPayload(
    @Json(name = "device_id") val deviceId: String,
    @Json(name = "device_name") val deviceName: String,
    @Json(name = "package_name") val packageName: String,
    @Json(name = "application_name") val applicationName: String,
    @Json(name = "title") val title: String,
    @Json(name = "message") val message: String,
    @Json(name = "big_text") val bigText: String?,
    @Json(name = "sub_text") val subText: String?,
    @Json(name = "received_at") val receivedAt: String,
    @Json(name = "android_version") val androidVersion: String,
    @Json(name = "device_brand") val deviceBrand: String,
    @Json(name = "device_model") val deviceModel: String
)

@JsonClass(generateAdapter = true)
data class PackageConfigResponse(
    @Json(name = "packages") val packages: List<String>
)
