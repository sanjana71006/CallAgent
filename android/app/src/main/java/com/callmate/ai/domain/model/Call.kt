package com.callmate.ai.domain.model

data class Call(
    val id: String,
    val phoneNumber: String,
    val callerName: String,
    val timestamp: Long,
    val durationSeconds: Int = 0,
    val status: String = "COMPLETED", // "SCREENED" | "TAKEN_OVER" | "MISSED" | "DECLINED"
    val category: CallCategory = CallCategory.UNKNOWN,
    val importance: Importance = Importance.MEDIUM,
    val summary: String = "",
    val purpose: String = "",
    val importantDetails: String = "",
    val recommendation: String = "",
    val isSpam: Boolean = false,
    val transcriptAvailable: Boolean = true
)
