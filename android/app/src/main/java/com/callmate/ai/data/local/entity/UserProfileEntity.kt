package com.callmate.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.callmate.ai.domain.model.UserProfile

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String = "default_user_profile",
    val name: String = "User",
    val gender: String = "Prefer not to say",
    val avatarUri: String = "avatar_1",
    val phoneNumber: String = "+1 (555) 019-2834",
    val email: String = "",
    val isCloudSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): UserProfile {
        return UserProfile(
            name = name,
            phoneNumber = phoneNumber,
            gender = gender,
            avatarId = avatarUri
        )
    }

    companion object {
        fun fromDomain(profile: UserProfile, email: String = "", isSynced: Boolean = false): UserProfileEntity {
            return UserProfileEntity(
                name = profile.name,
                phoneNumber = profile.phoneNumber,
                gender = profile.gender,
                avatarUri = profile.avatarId,
                email = email,
                isCloudSynced = isSynced,
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}
