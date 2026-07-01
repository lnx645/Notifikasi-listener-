package com.example.network

import android.content.Context
import com.example.storage.EncryptedSharedPreferencesManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class NetworkClient(
    private val context: Context,
    private val prefsManager: EncryptedSharedPreferencesManager
) {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val payloadAdapter = moshi.adapter(NotificationPayload::class.java)
    private val configAdapter = moshi.adapter(PackageConfigResponse::class.java)

    private fun getOkHttpClient(): OkHttpClient {
        val timeout = prefsManager.connectionTimeout.toLong()
        return OkHttpClient.Builder()
            .connectTimeout(timeout, TimeUnit.SECONDS)
            .readTimeout(timeout, TimeUnit.SECONDS)
            .writeTimeout(timeout, TimeUnit.SECONDS)
            .build()
    }

    suspend fun sendNotification(payload: NotificationPayload): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val url = prefsManager.backendUrl.trim().removeSuffix("/") + "/notification"
            val token = prefsManager.authToken
            val jsonBody = payloadAdapter.toJson(payload)
            val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())

            val requestBuilder = Request.Builder()
                .url(url)
                .post(requestBody)
                .header("User-Agent", "NotificationBridge/1.0")

            if (token.isNotEmpty()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }

            val request = requestBuilder.build()
            getOkHttpClient().newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success(response.code)
                } else {
                    Result.failure(Exception("HTTP error code: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncPackages(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val url = prefsManager.backendUrl.trim().removeSuffix("/") + "/config/packages"
            val token = prefsManager.authToken

            val requestBuilder = Request.Builder()
                .url(url)
                .get()
                .header("User-Agent", "NotificationBridge/1.0")

            if (token.isNotEmpty()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }

            val request = requestBuilder.build()
            getOkHttpClient().newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string() ?: throw Exception("Empty response body")
                    val configResponse = configAdapter.fromJson(bodyString)
                    if (configResponse != null) {
                        Result.success(configResponse.packages)
                    } else {
                        Result.failure(Exception("Failed to parse package response"))
                    }
                } else {
                    Result.failure(Exception("HTTP error code: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
