package com.callmate.ai.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TurnDto(
    @SerializedName("speaker") val speaker: String,
    @SerializedName("text") val text: String
)

data class ChatRequestDto(
    @SerializedName("call_id") val callId: String,
    @SerializedName("conversation") val conversation: List<TurnDto>,
    @SerializedName("caller_phone") val callerPhone: String? = null,
    @SerializedName("caller_name") val callerName: String? = null,
    @SerializedName("assistant_name") val assistantName: String = "CallMate AI",
    @SerializedName("personality") val personality: String = "polite and professional"
)

data class ChatResponseDto(
    @SerializedName("response") val response: String,
    @SerializedName("suggested_action") val suggestedAction: String? = null,
    @SerializedName("is_call_complete") val isCallComplete: Boolean = false
)
