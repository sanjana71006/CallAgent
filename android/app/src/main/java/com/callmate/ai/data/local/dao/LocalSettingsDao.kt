package com.callmate.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.callmate.ai.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalSettingsDao {

    // Assistant Settings
    @Query("SELECT * FROM assistant_settings WHERE id = 'default_assistant_settings' LIMIT 1")
    fun getAssistantSettings(): Flow<AssistantSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAssistantSettings(settings: AssistantSettingsEntity)

    // Instructions
    @Query("SELECT * FROM assistant_instructions WHERE id = 'default_instructions' LIMIT 1")
    fun getInstructions(): Flow<AssistantInstructionsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveInstructions(instructions: AssistantInstructionsEntity)

    @Query("DELETE FROM assistant_instructions")
    suspend fun clearInstructions()

    // Voice Settings
    @Query("SELECT * FROM voice_settings WHERE id = 'default_voice_settings' LIMIT 1")
    fun getVoiceSettings(): Flow<VoiceSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveVoiceSettings(voiceSettings: VoiceSettingsEntity)

    // Silent Mode Settings
    @Query("SELECT * FROM silent_mode_settings WHERE id = 'default_silent_mode' LIMIT 1")
    fun getSilentModeSettings(): Flow<SilentModeSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSilentModeSettings(silentMode: SilentModeSettingsEntity)

    // Notification Settings
    @Query("SELECT * FROM notification_settings WHERE id = 'default_notifications' LIMIT 1")
    fun getNotificationSettings(): Flow<NotificationSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveNotificationSettings(notificationSettings: NotificationSettingsEntity)

    // App Preferences
    @Query("SELECT * FROM app_preferences WHERE id = 'default_app_preferences' LIMIT 1")
    fun getAppPreferences(): Flow<AppPreferencesEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAppPreferences(preferences: AppPreferencesEntity)

    // Reset All Settings to defaults
    @Query("DELETE FROM assistant_settings")
    suspend fun clearAssistantSettings()

    @Query("DELETE FROM voice_settings")
    suspend fun clearVoiceSettings()

    @Query("DELETE FROM silent_mode_settings")
    suspend fun clearSilentModeSettings()

    @Query("DELETE FROM notification_settings")
    suspend fun clearNotificationSettings()
}
