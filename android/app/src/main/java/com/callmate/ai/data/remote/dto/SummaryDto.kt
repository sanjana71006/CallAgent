package com.callmate.ai.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SummaryRequestDto(
    @SerializedName("call_id") val callId: String,
    @SerializedName("conversation") val conversation: List<TurnDto>,
    @SerializedName("caller_phone") val callerPhone: String? = null,
    @SerializedName("caller_name") val callerName: String? = null
)

data class SummaryResponseDto(
    @SerializedName("caller") val caller: String = "Unknown",
    @SerializedName("purpose") val purpose: String = "",
    @SerializedName("important_details") val importantDetails: String = "",
    @SerializedName("recommended_action") val recommendedAction: String = "",
    @SerializedName("category") val category: String = "UNKNOWN",
    @SerializedName("importance") val importance: String = "MEDIUM",
    @SerializedName("is_spam") val isSpam: Boolean = false,
    @SerializedName("executive_summary") val executiveSummary: String = ""
)
