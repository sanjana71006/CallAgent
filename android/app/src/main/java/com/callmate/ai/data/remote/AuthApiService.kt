package com.callmate.ai.data.remote

import com.callmate.ai.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<AuthResponseDto>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>

    @POST("api/auth/logout")
    suspend fun logout(@Header("Authorization") token: String? = null): Response<ApiResponseDto>

    @GET("api/auth/me")
    suspend fun getMe(@Header("Authorization") token: String): Response<AuthResponseDto>

    @GET("api/users/me")
    suspend fun getProfile(@Header("Authorization") token: String): Response<AuthResponseDto>

    @PUT("api/users/me")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateUserRequestDto
    ): Response<AuthResponseDto>

    @DELETE("api/users/me")
    suspend fun deleteAccount(@Header("Authorization") token: String): Response<ApiResponseDto>

    @GET("api/health")
    suspend fun checkHealth(): Response<Map<String, Any>>
}
