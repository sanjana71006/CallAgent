package com.callmate.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.callmate.ai.domain.model.SilentModeConfig

@Entity(tableName = "silent_mode_settings")
data class SilentModeSettingsEntity(
    @PrimaryKey val id: String = "default_silent_mode",
    val enabled: Boolean = false,
    val silenceTelemarketing: Boolean = true,
    val silenceSpam: Boolean = true,
    val silenceUnknown: Boolean = false,
    val silencePotentialScam: Boolean = true,
    val silenceOther: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): SilentModeConfig {
        return SilentModeConfig(
            enabled = enabled,
            silenceTelemarketing = silenceTelemarketing,
            silenceSpam = silenceSpam,
            silenceUnknown = silenceUnknown,
            silenceScam = silencePotentialScam,
            silenceUnwanted = true,
            silenceOther = silenceOther
        )
    }

    companion object {
        fun fromDomain(config: SilentModeConfig): SilentModeSettingsEntity {
            return SilentModeSettingsEntity(
                enabled = config.enabled,
                silenceTelemarketing = config.silenceTelemarketing,
                silenceSpam = config.silenceSpam,
                silenceUnknown = config.silenceUnknown,
                silencePotentialScam = config.silenceScam,
                silenceOther = config.silenceOther,
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}
