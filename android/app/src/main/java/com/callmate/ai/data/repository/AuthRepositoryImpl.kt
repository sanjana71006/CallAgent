package com.callmate.ai.data.repository

import com.callmate.ai.core.network.ApiClient
import com.callmate.ai.core.network.TokenManager
import com.callmate.ai.data.local.dao.UserProfileDao
import com.callmate.ai.data.local.entity.UserProfileEntity
import com.callmate.ai.data.remote.dto.LoginRequestDto
import com.callmate.ai.data.remote.dto.RegisterRequestDto
import com.callmate.ai.data.remote.dto.UpdateUserRequestDto
import com.callmate.ai.data.remote.dto.UserDto
import com.callmate.ai.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val tokenManager: TokenManager,
    private val userProfileDao: UserProfileDao
) : AuthRepository {

    override val isLoggedIn: Flow<Boolean> = tokenManager.isLoggedInFlow
    override val currentEmail: Flow<String> = tokenManager.userEmailFlow
    override val currentName: Flow<String> = tokenManager.userNameFlow
    override val currentUserId: Flow<String> = tokenManager.userIdFlow

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        phoneNumber: String?
    ): Result<UserDto> = withContext(Dispatchers.IO) {
        try {
            val api = ApiClient.getAuthService()
            val request = RegisterRequestDto(
                name = name,
                email = email,
                password = password,
                confirmPassword = confirmPassword,
                phoneNumber = phoneNumber
            )
            val response = api.register(request)
            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                val token = body.token ?: ""
                val user = body.user ?: UserDto(userId = "usr_local", name = name, email = email)
                
                // Save session in TokenManager
                tokenManager.saveSession(token, user.userId, user.email, user.name)

                // Initialize local Room UserProfile
                userProfileDao.insertOrUpdate(
                    UserProfileEntity(
                        id = "default_user_profile",
                        name = user.name,
                        email = user.email,
                        phoneNumber = user.phoneNumber ?: "+1 (555) 019-2834",
                        isCloudSynced = true
                    )
                )
                Result.success(user)
            } else {
                val errorMsg = response.body()?.message ?: response.errorBody()?.string() ?: "Registration failed."
                Result.failure(Exception(cleanErrorMessage(errorMsg)))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Unable to connect to CallMate cloud server: ${e.message}"))
        }
    }

    override suspend fun login(email: String, password: String): Result<UserDto> = withContext(Dispatchers.IO) {
        try {
            val api = ApiClient.getAuthService()
            val request = LoginRequestDto(email = email, password = password)
            val response = api.login(request)
            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                val token = body.token ?: ""
                val user = body.user ?: UserDto(userId = "usr_local", name = "User", email = email)

                // Save session in TokenManager
                tokenManager.saveSession(token, user.userId, user.email, user.name)

                // Sync with local Room UserProfile
                val existing = userProfileDao.getUserProfileSync()
                userProfileDao.insertOrUpdate(
                    UserProfileEntity(
                        id = "default_user_profile",
                        name = user.name,
                        email = user.email,
                        phoneNumber = user.phoneNumber?.ifBlank { null } ?: existing?.phoneNumber ?: "+1 (555) 019-2834",
                        gender = existing?.gender ?: "Prefer not to say",
                        avatarUri = existing?.avatarUri ?: "avatar_1",
                        isCloudSynced = true
                    )
                )
                Result.success(user)
            } else {
                val errorMsg = response.body()?.message ?: response.errorBody()?.string() ?: "Invalid email or password."
                Result.failure(Exception(cleanErrorMessage(errorMsg)))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: Unable to reach server. Please check your connection."))
        }
    }

    override suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.getBearerToken()
            val api = ApiClient.getAuthService()
            api.logout(token)
        } catch (e: Exception) {
            // Even if network fails, proceed with local logout
        }
        tokenManager.clearSession()
        Result.success(Unit)
    }

    override suspend fun checkSession(): Result<UserDto> = withContext(Dispatchers.IO) {
        val bearer = tokenManager.getBearerToken()
        if (bearer == null) {
            return@withContext Result.failure(Exception("No active session"))
        }

        try {
            val api = ApiClient.getAuthService()
            val response = api.getMe(bearer)
            if (response.isSuccessful && response.body()?.success == true) {
                val user = response.body()!!.user!!
                tokenManager.updateName(user.name)
                Result.success(user)
            } else {
                Result.failure(Exception("Session expired"))
            }
        } catch (e: Exception) {
            // Offline-first: if network is down but token exists, return cached local session
            val localProfile = userProfileDao.getUserProfileSync()
            if (localProfile != null) {
                Result.success(
                    UserDto(
                        userId = "usr_cached",
                        name = localProfile.name,
                        email = localProfile.email,
                        phoneNumber = localProfile.phoneNumber
                    )
                )
            } else {
                Result.failure(Exception("Offline and no local profile"))
            }
        }
    }

    override suspend fun updateProfile(name: String, phoneNumber: String?): Result<UserDto> = withContext(Dispatchers.IO) {
        // Update local Room first (Offline-first requirement)
        val existing = userProfileDao.getUserProfileSync()
        if (existing != null) {
            userProfileDao.insertOrUpdate(
                existing.copy(
                    name = name,
                    phoneNumber = phoneNumber ?: existing.phoneNumber,
                    isCloudSynced = false,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
        tokenManager.updateName(name)

        // Then attempt cloud synchronization
        val bearer = tokenManager.getBearerToken()
        if (bearer != null) {
            try {
                val api = ApiClient.getAuthService()
                val response = api.updateProfile(bearer, UpdateUserRequestDto(name = name, phoneNumber = phoneNumber))
                if (response.isSuccessful && response.body()?.success == true) {
                    val user = response.body()!!.user!!
                    existing?.let {
                        userProfileDao.insertOrUpdate(it.copy(isCloudSynced = true))
                    }
                    return@withContext Result.success(user)
                }
            } catch (e: Exception) {
                // Cloud sync failed, but local change is preserved
            }
        }

        Result.success(
            UserDto(
                userId = "usr_local",
                name = name,
                email = existing?.email ?: "",
                phoneNumber = phoneNumber ?: existing?.phoneNumber
            )
        )
    }

    override suspend fun deleteAccount(): Result<Unit> = withContext(Dispatchers.IO) {
        val bearer = tokenManager.getBearerToken()
        if (bearer != null) {
            try {
                val api = ApiClient.getAuthService()
                val response = api.deleteAccount(bearer)
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Failed to delete account on server. Please try again."))
                }
            } catch (e: Exception) {
                return@withContext Result.failure(Exception("Cannot delete account: server unreachable."))
            }
        }

        // Clear local session & local profile
        tokenManager.clearSession()
        userProfileDao.deleteProfile()
        Result.success(Unit)
    }

    private fun cleanErrorMessage(raw: String): String {
        return if (raw.contains("\"message\":\"")) {
            raw.substringAfter("\"message\":\"").substringBefore("\"")
        } else {
            raw.take(120)
        }
    }
}
