package com.callmate.ai.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.callmate.ai.domain.model.*
import com.callmate.ai.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "callmate_settings")

class SettingsRepositoryImpl(private val context: Context) : SettingsRepository {

    private object PreferencesKeys {
        // Assistant Settings
        val ASSISTANT_ENABLED = booleanPreferencesKey("assistant_enabled")
        val ASSISTANT_NAME = stringPreferencesKey("assistant_name")
        val LANGUAGE = stringPreferencesKey("language")
        val PERSONALITY = stringPreferencesKey("personality")
        val GREETING = stringPreferencesKey("greeting")
        val AUTO_SCREEN_UNKNOWN = booleanPreferencesKey("auto_screen_unknown")
        val AUTO_SCREEN_SPAM = booleanPreferencesKey("auto_screen_spam")
        val SAVE_TRANSCRIPTS = booleanPreferencesKey("save_transcripts")
        val SAVE_SUMMARIES = booleanPreferencesKey("save_summaries")
        val BACKEND_URL = stringPreferencesKey("backend_url")
        val SPEECH_RATE = floatPreferencesKey("speech_rate")
        val SPEECH_PITCH = floatPreferencesKey("speech_pitch")

        // User Profile
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_PHONE = stringPreferencesKey("user_phone")
        val USER_GENDER = stringPreferencesKey("user_gender")
        val USER_AVATAR = stringPreferencesKey("user_avatar")

        // Instructions
        val CUSTOM_INSTRUCTIONS = stringPreferencesKey("custom_instructions")

        // Silent Mode
        val SILENT_MODE_ENABLED = booleanPreferencesKey("silent_mode_enabled")
        val SILENCE_TELEMARKETING = booleanPreferencesKey("silence_telemarketing")
        val SILENCE_SPAM = booleanPreferencesKey("silence_spam")
        val SILENCE_UNKNOWN = booleanPreferencesKey("silence_unknown")
        val SILENCE_SCAM = booleanPreferencesKey("silence_scam")
        val SILENCE_UNWANTED = booleanPreferencesKey("silence_unwanted")
        val SILENCE_OTHER = booleanPreferencesKey("silence_other")

        // WhatsApp Preferences
        val WA_ASSISTANT_UPDATES = booleanPreferencesKey("wa_assistant_updates")
        val WA_IMPORTANT_ALERTS = booleanPreferencesKey("wa_important_alerts")
        val WA_FEATURE_UPDATES = booleanPreferencesKey("wa_feature_updates")
        val WA_PROMOTIONAL_UPDATES = booleanPreferencesKey("wa_promotional_updates")

        // Theme Mode
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    override fun getSettings(): Flow<AssistantSettings> {
        return context.dataStore.data.map { preferences ->
            AssistantSettings(
                enabled = preferences[PreferencesKeys.ASSISTANT_ENABLED] ?: true,
                assistantName = preferences[PreferencesKeys.ASSISTANT_NAME] ?: "CallMate AI",
                language = preferences[PreferencesKeys.LANGUAGE] ?: "en-US",
                personality = preferences[PreferencesKeys.PERSONALITY] ?: "Polite and concise",
                greeting = preferences[PreferencesKeys.GREETING] ?: "Hello! I am CallMate AI, screening this call on behalf of the user. How may I assist you?",
                autoScreenUnknown = preferences[PreferencesKeys.AUTO_SCREEN_UNKNOWN] ?: true,
                autoScreenSpam = preferences[PreferencesKeys.AUTO_SCREEN_SPAM] ?: true,
                saveTranscripts = preferences[PreferencesKeys.SAVE_TRANSCRIPTS] ?: true,
                saveSummaries = preferences[PreferencesKeys.SAVE_SUMMARIES] ?: true,
                backendUrl = preferences[PreferencesKeys.BACKEND_URL] ?: "http://10.0.2.2:8000",
                speechRate = preferences[PreferencesKeys.SPEECH_RATE] ?: 1.0f,
                speechPitch = preferences[PreferencesKeys.SPEECH_PITCH] ?: 1.0f
            )
        }
    }

    override suspend fun updateSettings(settings: AssistantSettings) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ASSISTANT_ENABLED] = settings.enabled
            preferences[PreferencesKeys.ASSISTANT_NAME] = settings.assistantName
            preferences[PreferencesKeys.LANGUAGE] = settings.language
            preferences[PreferencesKeys.PERSONALITY] = settings.personality
            preferences[PreferencesKeys.GREETING] = settings.greeting
            preferences[PreferencesKeys.AUTO_SCREEN_UNKNOWN] = settings.autoScreenUnknown
            preferences[PreferencesKeys.AUTO_SCREEN_SPAM] = settings.autoScreenSpam
            preferences[PreferencesKeys.SAVE_TRANSCRIPTS] = settings.saveTranscripts
            preferences[PreferencesKeys.SAVE_SUMMARIES] = settings.saveSummaries
            preferences[PreferencesKeys.BACKEND_URL] = settings.backendUrl
            preferences[PreferencesKeys.SPEECH_RATE] = settings.speechRate
            preferences[PreferencesKeys.SPEECH_PITCH] = settings.speechPitch
        }
    }

    override suspend fun setAssistantEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ASSISTANT_ENABLED] = enabled
        }
    }

    override suspend fun setBackendUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BACKEND_URL] = url
        }
    }

    override suspend fun setPersonality(personality: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PERSONALITY] = personality
        }
    }

    // User Profile
    override fun getUserProfile(): Flow<UserProfile> {
        return context.dataStore.data.map { preferences ->
            UserProfile(
                name = preferences[PreferencesKeys.USER_NAME] ?: "User",
                phoneNumber = preferences[PreferencesKeys.USER_PHONE] ?: "",
                gender = preferences[PreferencesKeys.USER_GENDER] ?: "Prefer not to say",
                avatarId = preferences[PreferencesKeys.USER_AVATAR] ?: "avatar_1"
            )
        }
    }

    override suspend fun updateUserProfile(profile: UserProfile) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = profile.name
            preferences[PreferencesKeys.USER_PHONE] = profile.phoneNumber
            preferences[PreferencesKeys.USER_GENDER] = profile.gender
            preferences[PreferencesKeys.USER_AVATAR] = profile.avatarId
        }
    }

    // Instructions
    override fun getCustomInstructions(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.CUSTOM_INSTRUCTIONS]
                ?: "Be polite and concise. Ask unknown callers why they are calling. Never share my personal information."
        }
    }

    override suspend fun updateCustomInstructions(instructions: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CUSTOM_INSTRUCTIONS] = instructions
        }
    }

    // Silent Mode
    override fun getSilentModeConfig(): Flow<SilentModeConfig> {
        return context.dataStore.data.map { preferences ->
            SilentModeConfig(
                enabled = preferences[PreferencesKeys.SILENT_MODE_ENABLED] ?: false,
                silenceTelemarketing = preferences[PreferencesKeys.SILENCE_TELEMARKETING] ?: true,
                silenceSpam = preferences[PreferencesKeys.SILENCE_SPAM] ?: true,
                silenceUnknown = preferences[PreferencesKeys.SILENCE_UNKNOWN] ?: false,
                silenceScam = preferences[PreferencesKeys.SILENCE_SCAM] ?: true,
                silenceUnwanted = preferences[PreferencesKeys.SILENCE_UNWANTED] ?: false,
                silenceOther = preferences[PreferencesKeys.SILENCE_OTHER] ?: false
            )
        }
    }

    override suspend fun updateSilentModeConfig(config: SilentModeConfig) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SILENT_MODE_ENABLED] = config.enabled
            preferences[PreferencesKeys.SILENCE_TELEMARKETING] = config.silenceTelemarketing
            preferences[PreferencesKeys.SILENCE_SPAM] = config.silenceSpam
            preferences[PreferencesKeys.SILENCE_UNKNOWN] = config.silenceUnknown
            preferences[PreferencesKeys.SILENCE_SCAM] = config.silenceScam
            preferences[PreferencesKeys.SILENCE_UNWANTED] = config.silenceUnwanted
            preferences[PreferencesKeys.SILENCE_OTHER] = config.silenceOther
        }
    }

    // WhatsApp Preferences
    override fun getWhatsAppConfig(): Flow<WhatsAppConfig> {
        return context.dataStore.data.map { preferences ->
            WhatsAppConfig(
                assistantUpdates = preferences[PreferencesKeys.WA_ASSISTANT_UPDATES] ?: true,
                importantAlerts = preferences[PreferencesKeys.WA_IMPORTANT_ALERTS] ?: true,
                featureUpdates = preferences[PreferencesKeys.WA_FEATURE_UPDATES] ?: false,
                promotionalUpdates = preferences[PreferencesKeys.WA_PROMOTIONAL_UPDATES] ?: false
            )
        }
    }

    override suspend fun updateWhatsAppConfig(config: WhatsAppConfig) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WA_ASSISTANT_UPDATES] = config.assistantUpdates
            preferences[PreferencesKeys.WA_IMPORTANT_ALERTS] = config.importantAlerts
            preferences[PreferencesKeys.WA_FEATURE_UPDATES] = config.featureUpdates
            preferences[PreferencesKeys.WA_PROMOTIONAL_UPDATES] = config.promotionalUpdates
        }
    }

    // Theme Mode
    override fun getThemeMode(): Flow<AppThemeMode> {
        return context.dataStore.data.map { preferences ->
            val themeStr = preferences[PreferencesKeys.THEME_MODE] ?: AppThemeMode.SYSTEM.name
            AppThemeMode.fromString(themeStr)
        }
    }

    override suspend fun updateThemeMode(themeMode: AppThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode.name
        }
    }

    // Reset & Wipe
    override suspend fun resetToDefaults() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ASSISTANT_ENABLED] = true
            preferences[PreferencesKeys.ASSISTANT_NAME] = "CallMate AI"
            preferences[PreferencesKeys.LANGUAGE] = "en-US"
            preferences[PreferencesKeys.PERSONALITY] = "Polite and concise"
            preferences[PreferencesKeys.GREETING] = "Hello! I am CallMate AI, screening this call on behalf of the user. How may I assist you?"
            preferences[PreferencesKeys.AUTO_SCREEN_UNKNOWN] = true
            preferences[PreferencesKeys.AUTO_SCREEN_SPAM] = true
            preferences[PreferencesKeys.SAVE_TRANSCRIPTS] = true
            preferences[PreferencesKeys.SAVE_SUMMARIES] = true
            preferences[PreferencesKeys.BACKEND_URL] = "http://10.0.2.2:8000"
            preferences[PreferencesKeys.SPEECH_RATE] = 1.0f
            preferences[PreferencesKeys.SPEECH_PITCH] = 1.0f

            preferences[PreferencesKeys.CUSTOM_INSTRUCTIONS] = "Be polite and concise. Ask unknown callers why they are calling. Never share my personal information."
            preferences[PreferencesKeys.SILENT_MODE_ENABLED] = false
            preferences[PreferencesKeys.SILENCE_TELEMARKETING] = true
            preferences[PreferencesKeys.SILENCE_SPAM] = true
            preferences[PreferencesKeys.SILENCE_UNKNOWN] = false
            preferences[PreferencesKeys.SILENCE_SCAM] = true
            preferences[PreferencesKeys.SILENCE_UNWANTED] = false
            preferences[PreferencesKeys.SILENCE_OTHER] = false

            preferences[PreferencesKeys.WA_ASSISTANT_UPDATES] = true
            preferences[PreferencesKeys.WA_IMPORTANT_ALERTS] = true
            preferences[PreferencesKeys.WA_FEATURE_UPDATES] = false
            preferences[PreferencesKeys.WA_PROMOTIONAL_UPDATES] = false

            preferences[PreferencesKeys.THEME_MODE] = AppThemeMode.SYSTEM.name
        }
    }

    override suspend fun clearAllData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
