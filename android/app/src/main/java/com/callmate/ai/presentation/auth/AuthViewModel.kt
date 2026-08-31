package com.callmate.ai.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callmate.ai.data.remote.dto.UserDto
import com.callmate.ai.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(val user: UserDto) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val currentUser: UserDto? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.isLoggedIn.collect { loggedIn ->
                _uiState.update { it.copy(isLoggedIn = loggedIn) }
                if (!loggedIn && _authState.value is AuthState.Authenticated) {
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
        viewModelScope.launch {
            checkSession()
        }
    }

    fun checkSession() {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.checkSession()
            result.onSuccess { user ->
                _uiState.update { it.copy(currentUser = user, isLoggedIn = true) }
                _authState.value = AuthState.Authenticated(user)
            }.onFailure { error ->
                _uiState.update { it.copy(currentUser = null, isLoggedIn = false) }
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter both email and password.") }
            return
        }

        val emailRegex = Regex("^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,3})+$")
        if (!emailRegex.matches(trimmedEmail)) {
            _uiState.update { it.copy(errorMessage = "Please provide a valid email address.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = authRepository.login(trimmedEmail, password)
            result.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        currentUser = user,
                        errorMessage = null
                    )
                }
                _authState.value = AuthState.Authenticated(user)
                onSuccess()
            }.onFailure { error ->
                val friendlyMessage = when {
                    error.message?.contains("Unable to reach server", ignoreCase = true) == true ->
                        "Unable to connect to CallMate server. Please check your internet connection."
                    error.message?.contains("Invalid email or password", ignoreCase = true) == true ->
                        "Invalid email or password. Please try again."
                    else ->
                        error.message ?: "Authentication failure. Please try again."
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = friendlyMessage
                    )
                }
                _authState.value = AuthState.Error(friendlyMessage)
            }
        }
    }

    fun register(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        phoneNumber: String? = null,
        onSuccess: () -> Unit
    ) {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()
        val trimmedPhone = phoneNumber?.trim()

        if (trimmedName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Name cannot be empty.") }
            return
        }
        if (trimmedEmail.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email cannot be empty.") }
            return
        }
        val emailRegex = Regex("^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,3})+$")
        if (!emailRegex.matches(trimmedEmail)) {
            _uiState.update { it.copy(errorMessage = "Please provide a valid email address.") }
            return
        }
        if (!trimmedPhone.isNullOrBlank()) {
            val phoneDigits = trimmedPhone.filter { it.isDigit() }
            if (phoneDigits.length < 7 || phoneDigits.length > 15) {
                _uiState.update { it.copy(errorMessage = "Please provide a valid phone number.") }
                return
            }
        }
        if (password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Password cannot be empty.") }
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters.") }
            return
        }
        if (password != confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Passwords do not match.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = authRepository.register(
                name = trimmedName,
                email = trimmedEmail,
                password = password,
                confirmPassword = confirmPassword,
                phoneNumber = trimmedPhone
            )
            result.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        currentUser = user,
                        errorMessage = null
                    )
                }
                _authState.value = AuthState.Authenticated(user)
                onSuccess()
            }.onFailure { error ->
                val friendlyMessage = when {
                    error.message?.contains("already exists", ignoreCase = true) == true ->
                        "An account with this email address already exists."
                    error.message?.contains("Unable to connect", ignoreCase = true) == true ->
                        "Unable to connect to CallMate server. Please check your internet connection."
                    else ->
                        error.message ?: "Registration failed. Please try again."
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = friendlyMessage
                    )
                }
                _authState.value = AuthState.Error(friendlyMessage)
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update {
                it.copy(
                    isLoggedIn = false,
                    currentUser = null,
                    errorMessage = null
                )
            }
            _authState.value = AuthState.Unauthenticated
            onSuccess()
        }
    }

    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = authRepository.deleteAccount()
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = false,
                        currentUser = null
                    )
                }
                _authState.value = AuthState.Unauthenticated
                onSuccess()
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false) }
                onError(error.message ?: "Failed to delete account from server.")
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
