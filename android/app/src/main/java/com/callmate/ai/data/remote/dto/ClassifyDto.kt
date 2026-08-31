package com.callmate.ai.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ClassifyRequestDto(
    @SerializedName("call_id") val callId: String,
    @SerializedName("conversation") val conversation: List<TurnDto>,
    @SerializedName("caller_phone") val callerPhone: String? = null,
    @SerializedName("caller_name") val callerName: String? = null
)

data class ClassifyResponseDto(
    @SerializedName("category") val category: String = "UNKNOWN",
    @SerializedName("importance") val importance: String = "MEDIUM",
    @SerializedName("confidence") val confidence: Float = 0.9f,
    @SerializedName("reason") val reason: String = "",
    @SerializedName("is_spam") val isSpam: Boolean = false
)
