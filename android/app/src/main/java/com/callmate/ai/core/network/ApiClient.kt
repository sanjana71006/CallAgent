package com.callmate.ai.core.network

import com.callmate.ai.data.remote.AuthApiService
import com.callmate.ai.data.remote.CallMateApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private var currentAiBaseUrl = "http://10.0.2.2:8000/"
    private var currentAuthBaseUrl = "http://10.0.2.2:5000/"
    
    private var apiService: CallMateApiService? = null
    private var authService: AuthApiService? = null

    private val okHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
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
