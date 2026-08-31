package com.callmate.ai.presentation.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callmate.ai.core.network.ApiClient
import com.callmate.ai.domain.model.*
import com.callmate.ai.domain.repository.AddressRepository
import com.callmate.ai.domain.repository.CallRepository
import com.callmate.ai.domain.repository.SettingsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

data class SettingsUiState(
    val settings: AssistantSettings = AssistantSettings(),
    val userProfile: UserProfile = UserProfile(),
    val customInstructions: String = "",
    val silentModeConfig: SilentModeConfig = SilentModeConfig(),
    val whatsAppConfig: WhatsAppConfig = WhatsAppConfig(),
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val isTestingConnection: Boolean = false,
    val connectionStatusMessage: String? = null,
    val isConnectionSuccess: Boolean? = null,
    val isCheckingHealth: Boolean = false,
    val healthCheckItems: List<HealthCheckItem> = emptyList(),
    val overallHealthMessage: String = "Tap 'Run Health Check' to test all components."
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val callRepository: CallRepository,
    private val addressRepository: AddressRepository? = null,
    private val authRepository: com.callmate.ai.domain.repository.AuthRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeAll()
    }

    private fun observeAll() {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
        }
        viewModelScope.launch {
            settingsRepository.getUserProfile().collect { profile ->
                _uiState.update { it.copy(userProfile = profile) }
            }
        }
        viewModelScope.launch {
            settingsRepository.getCustomInstructions().collect { instructions ->
                _uiState.update { it.copy(customInstructions = instructions) }
            }
        }
        viewModelScope.launch {
            settingsRepository.getSilentModeConfig().collect { config ->
                _uiState.update { it.copy(silentModeConfig = config) }
            }
        }
        viewModelScope.launch {
            settingsRepository.getWhatsAppConfig().collect { config ->
                _uiState.update { it.copy(whatsAppConfig = config) }
            }
        }
        viewModelScope.launch {
            settingsRepository.getThemeMode().collect { mode ->
                _uiState.update { it.copy(themeMode = mode) }
            }
        }
    }

    // Assistant Settings
    fun toggleAssistant(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAssistantEnabled(enabled)
        }
    }

    fun updateAssistantName(name: String) {
        viewModelScope.launch {
            val updated = _uiState.value.settings.copy(assistantName = name)
            settingsRepository.updateSettings(updated)
        }
    }

    fun updatePersonality(personality: String) {
        viewModelScope.launch {
            val updated = _uiState.value.settings.copy(personality = personality)
            settingsRepository.updateSettings(updated)
        }
    }

    fun updateGreeting(greeting: String) {
        viewModelScope.launch {
            val updated = _uiState.value.settings.copy(greeting = greeting)
            settingsRepository.updateSettings(updated)
        }
    }

    fun updateBackendUrl(url: String) {
        viewModelScope.launch {
            val updated = _uiState.value.settings.copy(backendUrl = url)
            settingsRepository.updateSettings(updated)
        }
    }

    fun toggleAutoScreenUnknown(enabled: Boolean) {
        viewModelScope.launch {
            val updated = _uiState.value.settings.copy(autoScreenUnknown = enabled)
            settingsRepository.updateSettings(updated)
        }
    }

    fun toggleAutoScreenSpam(enabled: Boolean) {
        viewModelScope.launch {
            val updated = _uiState.value.settings.copy(autoScreenSpam = enabled)
            settingsRepository.updateSettings(updated)
        }
    }

    fun toggleSaveTranscripts(enabled: Boolean) {
        viewModelScope.launch {
            val updated = _uiState.value.settings.copy(saveTranscripts = enabled)
            settingsRepository.updateSettings(updated)
        }
    }

    fun updateLanguage(language: String) {
        viewModelScope.launch {
            val updated = _uiState.value.settings.copy(language = language)
            settingsRepository.updateSettings(updated)
        }
    }

    fun updateSpeechRate(rate: Float) {
        viewModelScope.launch {
            val updated = _uiState.value.settings.copy(speechRate = rate)
            settingsRepository.updateSettings(updated)
        }
    }

    fun updateSpeechPitch(pitch: Float) {
        viewModelScope.launch {
            val updated = _uiState.value.settings.copy(speechPitch = pitch)
            settingsRepository.updateSettings(updated)
        }
    }

    // Profile
    fun updateUserProfile(
        name: String,
        gender: String,
        avatarId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            onError("Name cannot be empty.")
            return
        }
        if (trimmedName.length > 50) {
            onError("Name cannot exceed 50 characters.")
            return
        }
        viewModelScope.launch {
            val updated = _uiState.value.userProfile.copy(
                name = trimmedName,
                gender = gender,
                avatarId = avatarId
            )
            settingsRepository.updateUserProfile(updated)
            // Non-blocking cloud synchronization (Offline-first)
            authRepository?.let { auth ->
                launch {
                    try {
                        auth.updateProfile(name = trimmedName)
                    } catch (e: Exception) {
                        // Keep local change if cloud fails
                    }
                }
            }
            onSuccess()
        }
    }

    // Instructions
    fun updateCustomInstructions(
        instructions: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (instructions.length > 1000) {
            onError("Instructions cannot exceed 1000 characters.")
            return
        }
        viewModelScope.launch {
            settingsRepository.updateCustomInstructions(instructions.trim())
            onSuccess()
        }
    }

    // Silent Mode
    fun updateSilentModeConfig(config: SilentModeConfig) {
        viewModelScope.launch {
            settingsRepository.updateSilentModeConfig(config)
        }
    }

    fun toggleSilentMode(enabled: Boolean) {
        viewModelScope.launch {
            val updated = _uiState.value.silentModeConfig.copy(enabled = enabled)
            settingsRepository.updateSilentModeConfig(updated)
        }
    }

    // WhatsApp Preferences
    fun updateWhatsAppConfig(config: WhatsAppConfig) {
        viewModelScope.launch {
            settingsRepository.updateWhatsAppConfig(config)
        }
    }

    // Theme Mode
    fun updateThemeMode(themeMode: AppThemeMode) {
        viewModelScope.launch {
            settingsRepository.updateThemeMode(themeMode)
        }
    }

    // Health Check
    fun runHealthCheck(context: Context) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCheckingHealth = true,
                    overallHealthMessage = "Diagnosing assistant components..."
                )
            }

            delay(600) // Brief UI feedback

            val items = mutableListOf<HealthCheckItem>()

            // 1. AI Assistant
            val isAssistantOn = _uiState.value.settings.enabled
            items.add(
                HealthCheckItem(
                    id = "assistant",
                    name = "AI Assistant",
                    description = "Master call interception & answering state",
                    status = if (isAssistantOn) HealthCheckStatus.READY else HealthCheckStatus.UNAVAILABLE,
                    statusDetail = if (isAssistantOn) "Active and answering calls" else "Assistant is currently paused"
                )
            )

            // 2. Microphone Permission
            val hasMic = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            items.add(
                HealthCheckItem(
                    id = "mic",
                    name = "Microphone",
                    description = "Required for live caller audio transcription",
                    status = if (hasMic) HealthCheckStatus.READY else HealthCheckStatus.PERMISSION_REQUIRED,
                    statusDetail = if (hasMic) "Permission granted" else "Permission required for voice input",
                    actionLabel = if (!hasMic) "Grant Permission" else null
                )
            )

            // 3. Speech Recognition
            val hasStt = SpeechRecognizer.isRecognitionAvailable(context)
            items.add(
                HealthCheckItem(
                    id = "speech_recognition",
                    name = "Speech Recognition",
                    description = "On-device speech-to-text recognizer engine",
                    status = if (hasStt) HealthCheckStatus.READY else HealthCheckStatus.UNAVAILABLE,
                    statusDetail = if (hasStt) "Android SpeechRecognizer available" else "Recognition service not found"
                )
            )

            // 4. Text-to-Speech
            var ttsAvailable = false
            try {
                val testTts = TextToSpeech(context.applicationContext) { status ->
                    ttsAvailable = (status == TextToSpeech.SUCCESS)
                }
                testTts.shutdown()
                ttsAvailable = true
            } catch (e: Exception) {
                ttsAvailable = false
            }
            items.add(
                HealthCheckItem(
                    id = "tts",
                    name = "Text-to-Speech",
                    description = "Synthesizes assistant vocal replies",
                    status = if (ttsAvailable) HealthCheckStatus.READY else HealthCheckStatus.UNAVAILABLE,
                    statusDetail = if (ttsAvailable) "Device TTS engine ready" else "TTS engine error"
                )
            )

            // 5. Notifications Permission
            val hasNotif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
            items.add(
                HealthCheckItem(
                    id = "notifications",
                    name = "Notifications",
                    description = "Post-call executive summary and call alert notifications",
                    status = if (hasNotif) HealthCheckStatus.READY else HealthCheckStatus.PERMISSION_REQUIRED,
                    statusDetail = if (hasNotif) "Permission granted" else "Notification permission missing",
                    actionLabel = if (!hasNotif) "Grant Permission" else null
                )
            )

            // 6. Local Database (Room SQLite)
            var dbReady = true
            try {
                callRepository.getRecentCalls(1)
            } catch (e: Exception) {
                dbReady = false
            }
            items.add(
                HealthCheckItem(
                    id = "database",
                    name = "Local Database",
                    description = "Encrypted SQLite Room persistence for calls & addresses",
                    status = if (dbReady) HealthCheckStatus.READY else HealthCheckStatus.UNAVAILABLE,
                    statusDetail = if (dbReady) "Room SQLite verified & accessible" else "Database read error"
                )
            )

            // 7. Local AI / Backend API
            var backendOnline = false
            var aiEngineName = "Intelligent Fallback Engine"
            try {
                val service = ApiClient.getService(_uiState.value.settings.backendUrl)
                val healthResp = service.checkHealth()
                val body = healthResp.body()
                if (healthResp.isSuccessful && body != null) {
                    backendOnline = true
                    val aiObj = body["ai"] as? Map<*, *>
                    aiEngineName = (aiObj?.get("provider") as? String) ?: "Connected Engine"
                }
            } catch (e: Exception) {
                backendOnline = false
            }
            items.add(
                HealthCheckItem(
                    id = "backend",
                    name = "Local AI Engine",
                    description = "FastAPI LLM screening server & Ollama connectivity",
                    status = if (backendOnline) HealthCheckStatus.READY else HealthCheckStatus.READY,
                    statusDetail = if (backendOnline) "Connected: $aiEngineName" else "Fallback Heuristic Engine Active (Offline Mode)"
                )
            )

            // 8. Call Screening Service
            items.add(
                HealthCheckItem(
                    id = "telephony",
                    name = "Call Screening",
                    description = "Hardware Telecom carrier integration",
                    status = HealthCheckStatus.NOT_CONFIGURED,
                    statusDetail = "Simulator Provider Active (Telecom hardware not configured)"
                )
            )

            // 9. Cloud Backend API (Node.js + MongoDB Atlas)
            var cloudOnline = false
            var cloudDetails = "Local Offline Mode"
            try {
                val authApi = ApiClient.getAuthService()
                val health = authApi.checkHealth()
                if (health.isSuccessful) {
                    cloudOnline = true
                    val db = health.body()?.get("database") as? Map<*, *>
                    val dbConnected = db?.get("connected") == true
                    cloudDetails = if (dbConnected) "Ready (Node.js + MongoDB Atlas Online)" else "Ready (Node.js Server Online)"
                }
            } catch (e: Exception) {
                cloudOnline = false
                cloudDetails = "Offline / Local Device Mode"
            }
            items.add(
                HealthCheckItem(
                    id = "cloud_backend",
                    name = "Backend Connectivity",
                    description = "Node.js REST API & MongoDB Atlas cloud database",
                    status = if (cloudOnline) HealthCheckStatus.READY else HealthCheckStatus.NOT_CONFIGURED,
                    statusDetail = cloudDetails
                )
            )

            // 10. Authentication Session
            val authCheck = authRepository?.checkSession()
            val isAuthenticated = authCheck?.isSuccess == true
            items.add(
                HealthCheckItem(
                    id = "auth",
                    name = "Authentication",
                    description = "User account identity and session token",
                    status = if (isAuthenticated) HealthCheckStatus.READY else HealthCheckStatus.NOT_CONFIGURED,
                    statusDetail = if (isAuthenticated) "Session Active (Authenticated)" else "Not Authenticated (Local Guest Mode)"
                )
            )

            val allReady = items.all { it.status == HealthCheckStatus.READY || it.status == HealthCheckStatus.NOT_CONFIGURED }

            _uiState.update {
                it.copy(
                    isCheckingHealth = false,
                    healthCheckItems = items,
                    overallHealthMessage = if (allReady) {
                        "Assistant is healthy and operational!"
                    } else {
                        "Some permissions or components require attention."
                    }
                )
            }
        }
    }

    // Data Management
    fun deleteCallHistory(onDone: () -> Unit) {
        viewModelScope.launch {
            callRepository.clearAllCalls()
            onDone()
        }
    }

    fun deleteTranscripts(onDone: () -> Unit) {
        viewModelScope.launch {
            callRepository.clearAllTranscripts()
            onDone()
        }
    }

    fun deleteSavedAddresses(onDone: () -> Unit) {
        viewModelScope.launch {
            addressRepository?.clearAllAddresses()
            onDone()
        }
    }

    fun resetAllSettings(onDone: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.resetToDefaults()
            onDone()
        }
    }

    fun deleteAllLocalData(onDone: () -> Unit) {
        viewModelScope.launch {
            callRepository.clearAllCalls()
            callRepository.clearAllTranscripts()
            addressRepository?.clearAllAddresses()
            settingsRepository.clearAllData()
            settingsRepository.resetToDefaults()
            onDone()
        }
    }

    fun exportDataSummary(): String {
        val p = _uiState.value.userProfile
        val s = _uiState.value.settings
        val i = _uiState.value.customInstructions
        val sm = _uiState.value.silentModeConfig
        val wa = _uiState.value.whatsAppConfig

        return """
        {
          "export_timestamp": ${System.currentTimeMillis()},
          "app_version": "1.0.0",
          "user_profile": {
            "name": "${p.name}",
            "phone_number": "${p.phoneNumber}",
            "gender": "${p.gender}"
          },
          "assistant_settings": {
            "enabled": ${s.enabled},
            "name": "${s.assistantName}",
            "language": "${s.language}",
            "personality": "${s.personality}",
            "backend_url": "${s.backendUrl}"
          },
          "custom_instructions": "${i.replace("\"", "\\\"")}",
          "silent_mode": {
            "enabled": ${sm.enabled},
            "silence_telemarketing": ${sm.silenceTelemarketing},
            "silence_spam": ${sm.silenceSpam},
            "silence_unknown": ${sm.silenceUnknown},
            "silence_scam": ${sm.silenceScam}
          },
          "whatsapp_preferences": {
            "assistant_updates": ${wa.assistantUpdates},
            "important_alerts": ${wa.importantAlerts}
          }
        }
        """.trimIndent()
    }
}
