package com.callmate.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_settings")
data class VoiceSettingsEntity(
    @PrimaryKey val id: String = "default_voice_settings",
    val language: String = "en-US",
    val voiceId: String = "en-US-default",
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val updatedAt: Long = System.currentTimeMillis()
)
