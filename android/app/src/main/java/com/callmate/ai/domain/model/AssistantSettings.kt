package com.callmate.ai.domain.model

data class AssistantSettings(
    val enabled: Boolean = true,
    val assistantName: String = "CallMate AI",
    val language: String = "en-US",
    val personality: String = "Polite and concise",
    val greeting: String = "Hello! I am CallMate AI, screening this call on behalf of the user. How may I assist you?",
    val autoScreenUnknown: Boolean = true,
    val autoScreenSpam: Boolean = true,
    val saveTranscripts: Boolean = true,
    val saveSummaries: Boolean = true,
    val backendUrl: String = "http://10.0.2.2:8000",
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f
)
