package com.callmate.ai.domain.repository

import com.callmate.ai.domain.model.*
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<AssistantSettings>
    suspend fun updateSettings(settings: AssistantSettings)
    suspend fun setAssistantEnabled(enabled: Boolean)
    suspend fun setBackendUrl(url: String)
    suspend fun setPersonality(personality: String)

    // User Profile
    fun getUserProfile(): Flow<UserProfile>
    suspend fun updateUserProfile(profile: UserProfile)

    // Instructions
    fun getCustomInstructions(): Flow<String>
    suspend fun updateCustomInstructions(instructions: String)

    // Silent Mode
    fun getSilentModeConfig(): Flow<SilentModeConfig>
    suspend fun updateSilentModeConfig(config: SilentModeConfig)

    // WhatsApp Preferences
    fun getWhatsAppConfig(): Flow<WhatsAppConfig>
    suspend fun updateWhatsAppConfig(config: WhatsAppConfig)

    // Theme Mode
    fun getThemeMode(): Flow<AppThemeMode>
    suspend fun updateThemeMode(themeMode: AppThemeMode)

    // Reset & Wipe
    suspend fun resetToDefaults()
    suspend fun clearAllData()
}
