package com.callmate.ai.core.network

import android.os.Build
import com.callmate.ai.data.remote.AuthApiService
import com.callmate.ai.data.remote.CallMateApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // Detect if running on Android Emulator vs Real Physical Device
    val isEmulator: Boolean by lazy {
        Build.FINGERPRINT.startsWith("generic") ||
        Build.FINGERPRINT.startsWith("unknown") ||
        Build.MODEL.contains("google_sdk") ||
        Build.MODEL.contains("Emulator") ||
        Build.MODEL.contains("Android SDK built for x86") ||
        Build.HARDWARE.contains("goldfish") ||
        Build.HARDWARE.contains("ranchu") ||
        Build.PRODUCT.contains("sdk_gphone") ||
        Build.PRODUCT.contains("sdk_google")
    }

    // Default: 10.0.2.2 on Emulator, 192.168.31.86 on Physical Device
    var serverHost: String = if (isEmulator) "10.0.2.2" else "192.168.31.86"
        set(value) {
            field = value.trim().removePrefix("http://").removePrefix("https://").removeSuffix("/")
            updateBaseUrls()
        }

    private var currentAiBaseUrl = "http://$serverHost:8000/"
    private var currentAuthBaseUrl = "http://$serverHost:5000/"
    
    private var apiService: CallMateApiService? = null
    private var authService: AuthApiService? = null

    private fun updateBaseUrls() {
        currentAiBaseUrl = "http://$serverHost:8000/"
        currentAuthBaseUrl = "http://$serverHost:5000/"
        apiService = null
        authService = null
    }

    private val okHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(6, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    fun getService(baseUrl: String = currentAiBaseUrl): CallMateApiService {
        val sanitized = if (!baseUrl.endsWith("/")) "$baseUrl/" else baseUrl
        if (apiService == null || sanitized != currentAiBaseUrl) {
            currentAiBaseUrl = sanitized
            val retrofit = Retrofit.Builder()
                .baseUrl(currentAiBaseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            apiService = retrofit.create(CallMateApiService::class.java)
        }
        return apiService!!
    }

    fun getAuthService(baseUrl: String = currentAuthBaseUrl): AuthApiService {
        val sanitized = if (!baseUrl.endsWith("/")) "$baseUrl/" else baseUrl
        if (authService == null || sanitized != currentAuthBaseUrl) {
            currentAuthBaseUrl = sanitized
            val retrofit = Retrofit.Builder()
                .baseUrl(currentAuthBaseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            authService = retrofit.create(AuthApiService::class.java)
        }
        return authService!!
    }
}
