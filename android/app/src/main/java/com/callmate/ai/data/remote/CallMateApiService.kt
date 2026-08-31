package com.callmate.ai.data.remote

import com.callmate.ai.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CallMateApiService {
    @GET("api/v1/health")
    suspend fun checkHealth(): Response<Map<String, Any>>

    @POST("api/v1/ai/chat")
    suspend fun chat(@Body request: ChatRequestDto): Response<ChatResponseDto>

    @POST("api/v1/ai/classify")
    suspend fun classify(@Body request: ClassifyRequestDto): Response<ClassifyResponseDto>

    @POST("api/v1/ai/summarize")
    suspend fun summarize(@Body request: SummaryRequestDto): Response<SummaryResponseDto>

    @GET("api/v1/config")
    suspend fun getConfig(): Response<Map<String, Any>>
}
