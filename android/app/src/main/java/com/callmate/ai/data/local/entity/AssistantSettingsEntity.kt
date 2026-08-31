package com.callmate.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.callmate.ai.domain.model.AssistantSettings

@Entity(tableName = "assistant_settings")
data class AssistantSettingsEntity(
    @PrimaryKey val id: String = "default_assistant_settings",
    val assistantEnabled: Boolean = true,
    val assistantName: String = "CallMate",
    val greeting: String = "Hello! I am CallMate, an AI screening assistant.",
    val autoScreenUnknown: Boolean = true,
    val autoScreenSpam: Boolean = true,
    val saveTranscripts: Boolean = true,
    val saveSummaries: Boolean = true,
    val backendUrl: String = "http://10.0.2.2:8000",
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): AssistantSettings {
        return AssistantSettings(
            enabled = assistantEnabled,
            assistantName = assistantName,
            greeting = greeting,
            autoScreenUnknown = autoScreenUnknown,
            autoScreenSpam = autoScreenSpam,
            saveTranscripts = saveTranscripts,
            saveSummaries = saveSummaries,
            backendUrl = backendUrl
        )
    }

    companion object {
        fun fromDomain(settings: AssistantSettings): AssistantSettingsEntity {
            return AssistantSettingsEntity(
                assistantEnabled = settings.enabled,
                assistantName = settings.assistantName,
                greeting = settings.greeting,
                autoScreenUnknown = settings.autoScreenUnknown,
                autoScreenSpam = settings.autoScreenSpam,
                saveTranscripts = settings.saveTranscripts,
                saveSummaries = settings.saveSummaries,
                backendUrl = settings.backendUrl,
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}
