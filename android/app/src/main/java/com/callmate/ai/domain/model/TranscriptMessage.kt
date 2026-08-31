package com.callmate.ai.domain.model

data class TranscriptMessage(
    val id: Long = 0,
    val callId: String,
    val speaker: String, // "caller" | "ai" | "user"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
