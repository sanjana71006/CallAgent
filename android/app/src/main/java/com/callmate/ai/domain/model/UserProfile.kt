package com.callmate.ai.domain.model

data class UserProfile(
    val name: String = "Sanjana",
    val phoneNumber: String = "9440886543",
    val gender: String = "Female",
    val avatarId: String = "avatar_1"
)

data class UserAddress(
    val id: String,
    val label: String = "Home", // Home, College, Work, Other
    val addressName: String = "",
    val addressLine: String = "",
    val deliveryInstructions: String = ""
)

data class UserInstruction(
    val id: String = "default",
    val title: String = "General Screening Instructions",
    val instruction: String = "Be polite and concise. Ask unknown callers why they are calling. Never share my personal information.",
    val isEnabled: Boolean = true
)

enum class AppThemeMode {
    LIGHT,
    DARK,
    SYSTEM;

    companion object {
        fun fromString(value: String): AppThemeMode {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: SYSTEM
        }
    }
}

data class SilentModeConfig(
    val enabled: Boolean = false,
    val silenceTelemarketing: Boolean = true,
    val silenceSpam: Boolean = true,
    val silenceUnknown: Boolean = false,
    val silenceScam: Boolean = true,
    val silenceUnwanted: Boolean = false,
    val silenceOther: Boolean = false
)

data class WhatsAppConfig(
    val assistantUpdates: Boolean = true,
    val importantAlerts: Boolean = true,
    val featureUpdates: Boolean = false,
    val promotionalUpdates: Boolean = false
)

enum class HealthCheckStatus {
    READY,
    PERMISSION_REQUIRED,
    UNAVAILABLE,
    NOT_CONFIGURED,
    CHECKING
}

data class HealthCheckItem(
    val id: String,
    val name: String,
    val description: String,
    val status: HealthCheckStatus,
    val statusDetail: String,
    val actionLabel: String? = null
)
