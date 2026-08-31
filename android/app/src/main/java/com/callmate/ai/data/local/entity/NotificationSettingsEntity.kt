package com.callmate.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.callmate.ai.domain.model.WhatsAppConfig

@Entity(tableName = "notification_settings")
data class NotificationSettingsEntity(
    @PrimaryKey val id: String = "default_notifications",
    val assistantUpdates: Boolean = true,
    val importantAlerts: Boolean = true,
    val featureUpdates: Boolean = false,
    val promotionalUpdates: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): WhatsAppConfig {
        return WhatsAppConfig(
            assistantUpdates = assistantUpdates,
            importantAlerts = importantAlerts,
            featureUpdates = featureUpdates,
            promotionalUpdates = promotionalUpdates
        )
    }

    companion object {
        fun fromDomain(config: WhatsAppConfig): NotificationSettingsEntity {
            return NotificationSettingsEntity(
                assistantUpdates = config.assistantUpdates,
                importantAlerts = config.importantAlerts,
                featureUpdates = config.featureUpdates,
                promotionalUpdates = config.promotionalUpdates,
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}
