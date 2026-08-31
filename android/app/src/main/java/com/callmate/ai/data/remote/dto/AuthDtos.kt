package com.callmate.ai.data.remote.dto

data class RegisterRequestDto(
    val name: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val phoneNumber: String? = null
)

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class UserDto(
    val userId: String,
    val name: String,
    val email: String,
    val phoneNumber: String? = null,
    val accountStatus: String? = null,
    val createdAt: String? = null,
    val lastLogin: String? = null,
    val appVersion: String? = null
)

data class AuthResponseDto(
    val success: Boolean,
    val message: String? = null,
    val token: String? = null,
    val user: UserDto? = null
)

data class UpdateUserRequestDto(
    val name: String? = null,
    val phoneNumber: String? = null
)

data class ApiResponseDto(
    val success: Boolean,
    val message: String? = null
)
