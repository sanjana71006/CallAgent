package com.callmate.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.callmate.ai.domain.model.AppThemeMode

@Entity(tableName = "app_preferences")
data class AppPreferencesEntity(
    @PrimaryKey val id: String = "default_app_preferences",
    val themeMode: String = "SYSTEM",
    val onboardingCompleted: Boolean = false,
    val selectedTab: String = "you",
    val token: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun getThemeModeEnum(): AppThemeMode {
        return try {
            AppThemeMode.valueOf(themeMode)
        } catch (e: Exception) {
            AppThemeMode.SYSTEM
        }
    }
}
