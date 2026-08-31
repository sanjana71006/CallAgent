package com.callmate.ai.domain.repository

import com.callmate.ai.data.remote.dto.UserDto
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isLoggedIn: Flow<Boolean>
    val currentEmail: Flow<String>
    val currentName: Flow<String>
    val currentUserId: Flow<String>

    suspend fun register(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        phoneNumber: String? = null
    ): Result<UserDto>

    suspend fun login(email: String, password: String): Result<UserDto>

    suspend fun logout(): Result<Unit>

    suspend fun checkSession(): Result<UserDto>

    suspend fun updateProfile(name: String, phoneNumber: String? = null): Result<UserDto>

    suspend fun deleteAccount(): Result<Unit>
}
