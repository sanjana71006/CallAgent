package com.callmate.ai.presentation.call

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callmate.ai.core.audio.SpeechToTextManager
import com.callmate.ai.core.audio.TextToSpeechManager
import com.callmate.ai.core.network.ApiClient
import com.callmate.ai.core.telephony.CallProvider
import com.callmate.ai.data.remote.dto.*
import com.callmate.ai.domain.model.*
import com.callmate.ai.domain.repository.CallRepository
import com.callmate.ai.domain.repository.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class CallState {
    INCOMING,
    AI_SCREENING,
    USER_TAKEOVER,
    ENDED
}

enum class AiVoiceState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

data class CallUiState(
    val currentCall: Call? = null,
    val callState: CallState = CallState.INCOMING,
    val aiVoiceState: AiVoiceState = AiVoiceState.IDLE,
    val transcripts: List<TranscriptMessage> = emptyList(),
    val partialCallerSpeech: String = "",
    val isMuted: Boolean = false,
    val callDurationSeconds: Int = 0,
    val summaryResult: String? = null,
    val errorMessage: String? = null
)

class CallViewModel(
    private val callProvider: CallProvider,
    private val callRepository: CallRepository,
    private val settingsRepository: SettingsRepository,
    private val sttManager: SpeechToTextManager,
    private val ttsManager: TextToSpeechManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CallUiState())
    val uiState: StateFlow<CallUiState> = _uiState.asStateFlow()

    private var durationTimerJob: Job? = null
    private var activeSettings = AssistantSettings()
    private var customInstructions = ""

    init {
        observeSettings()
        setupAudioCallbacks()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { settings ->
                activeSettings = settings
            }
        }
        viewModelScope.launch {
            settingsRepository.getCustomInstructions().collect { instructions ->
                customInstructions = instructions
            }
        }
    }

    private fun setupAudioCallbacks() {
        sttManager.onFinalResult = { callerSpeech ->
            if (callerSpeech.isNotBlank()) {
                handleCallerSpeech(callerSpeech)
            }
        }
        sttManager.onErrorCallback = { error ->
            // In simulator mode, keep conversational flow resilient
            _uiState.update { it.copy(errorMessage = error) }
        }
        ttsManager.onSpeechCompleted = {
            if (_uiState.value.callState == CallState.AI_SCREENING) {
                _uiState.update { it.copy(aiVoiceState = AiVoiceState.LISTENING) }
                sttManager.startListening(activeSettings.language)
            }
        }
    }

    fun startSimulatedCall(callerName: String = "Sarah Jenkins", phoneNumber: String = "+1 (555) 382-9012") {
        val call = callProvider.startIncomingCall(callerName, phoneNumber)
        _uiState.value = CallUiState(
            currentCall = call,
            callState = CallState.INCOMING,
            aiVoiceState = AiVoiceState.IDLE,
            transcripts = emptyList()
        )
    }

    fun acceptWithAi() {
        val call = _uiState.value.currentCall ?: return
        callProvider.answerCallWithAi(call.id)
        
        _uiState.update {
            it.copy(
                callState = CallState.AI_SCREENING,
                aiVoiceState = AiVoiceState.SPEAKING
            )
        }

        startCallTimer()

        // Initial Greeting
        val greetingText = activeSettings.greeting
        addTranscriptMessage(speaker = "ai", message = greetingText)
        ttsManager.speak(greetingText, pitch = activeSettings.speechPitch, rate = activeSettings.speechRate)
    }

    fun handleCallerSpeech(callerText: String) {
        val call = _uiState.value.currentCall ?: return
        addTranscriptMessage(speaker = "caller", message = callerText)
        
        _uiState.update { it.copy(aiVoiceState = AiVoiceState.THINKING) }
        sttManager.stopListening()

        // Query AI via Backend
        viewModelScope.launch {
            try {
                val apiService = ApiClient.getService(activeSettings.backendUrl)
                val turnsDto = _uiState.value.transcripts.map {
                    TurnDto(speaker = it.speaker, text = it.message)
                }

                val effectivePersonality = if (customInstructions.isNotBlank()) {
                    "${activeSettings.personality}. Instructions from user: $customInstructions"
                } else {
                    activeSettings.personality
                }

                val requestDto = ChatRequestDto(
                    callId = call.id,
                    conversation = turnsDto,
                    callerPhone = call.phoneNumber,
                    callerName = call.callerName,
                    assistantName = activeSettings.assistantName,
                    personality = effectivePersonality
                )

                val response = apiService.chat(requestDto)
                if (response.isSuccessful && response.body() != null) {
                    val aiReply = response.body()!!.response
                    addTranscriptMessage(speaker = "ai", message = aiReply)
                    
                    _uiState.update { it.copy(aiVoiceState = AiVoiceState.SPEAKING) }
                    ttsManager.speak(aiReply, pitch = activeSettings.speechPitch, rate = activeSettings.speechRate)

                    if (response.body()!!.isCallComplete) {
                        delay(2500)
                        endCall()
                    }
                } else {
                    fallbackAiResponse("Thank you for the message. I will ensure the user receives this update promptly.")
                }
            } catch (e: Exception) {
                fallbackAiResponse("Thank you. I have recorded your message and will notify the user immediately.")
            }
        }
    }

    private fun fallbackAiResponse(reply: String) {
        addTranscriptMessage(speaker = "ai", message = reply)
        _uiState.update { it.copy(aiVoiceState = AiVoiceState.SPEAKING) }
        ttsManager.speak(reply, pitch = activeSettings.speechPitch, rate = activeSettings.speechRate)
    }

    fun takeOverCall() {
        val call = _uiState.value.currentCall ?: return
        callProvider.takeOverCall(call.id)
        sttManager.stopListening()
        ttsManager.stop()
        
        _uiState.update {
            it.copy(
                callState = CallState.USER_TAKEOVER,
                aiVoiceState = AiVoiceState.IDLE
            )
        }
    }

    fun toggleMute() {
        _uiState.update { it.copy(isMuted = !it.isMuted) }
    }

    fun endCall() {
        val call = _uiState.value.currentCall ?: return
        durationTimerJob?.cancel()
        sttManager.stopListening()
        ttsManager.stop()
        callProvider.endCall(call.id)

        _uiState.update {
            it.copy(
                callState = CallState.ENDED,
                aiVoiceState = AiVoiceState.IDLE
            )
        }

        // Post-call classification & summary persistence
        viewModelScope.launch {
            processPostCallAnalysis(call, _uiState.value.transcripts, _uiState.value.callDurationSeconds)
        }
    }

    private suspend fun processPostCallAnalysis(
        call: Call,
        transcripts: List<TranscriptMessage>,
        duration: Int
    ) {
        val turnsDto = transcripts.map { TurnDto(speaker = it.speaker, text = it.message) }
        var category = CallCategory.UNKNOWN
        var importance = Importance.MEDIUM
        var isSpam = false
        var summaryText = "Call screened by CallMate AI."
        var purpose = "Inquiry"
        var importantDetails = ""
        var recommendedAction = "Review transcript"

        try {
            val apiService = ApiClient.getService(activeSettings.backendUrl)
            
            // Classification
            val classifyResp = apiService.classify(
                ClassifyRequestDto(
                    callId = call.id,
                    conversation = turnsDto,
                    callerPhone = call.phoneNumber,
                    callerName = call.callerName
                )
            )
            if (classifyResp.isSuccessful && classifyResp.body() != null) {
                val data = classifyResp.body()!!
                category = CallCategory.fromString(data.category)
                importance = Importance.fromString(data.importance)
                isSpam = data.isSpam
            }

            // Summary
            val summaryResp = apiService.summarize(
                SummaryRequestDto(
                    callId = call.id,
                    conversation = turnsDto,
                    callerPhone = call.phoneNumber,
                    callerName = call.callerName
                )
            )
            if (summaryResp.isSuccessful && summaryResp.body() != null) {
                val data = summaryResp.body()!!
                purpose = data.purpose
                importantDetails = data.importantDetails
                recommendedAction = data.recommendedAction
                summaryText = data.executiveSummary
            }
        } catch (e: Exception) {
            // Local fallback heuristics if backend unreachable
            if (transcripts.any { it.message.contains("interview", true) }) {
                category = CallCategory.RECRUITMENT
                importance = Importance.HIGH
                purpose = "Job Interview"
                recommendedAction = "Call back recruiter"
            }
        }

        val completedCall = call.copy(
            durationSeconds = duration,
            status = if (_uiState.value.callState == CallState.USER_TAKEOVER) "TAKEN_OVER" else "SCREENED",
            category = category,
            importance = importance,
            summary = summaryText,
            purpose = purpose,
            importantDetails = importantDetails,
            recommendation = recommendedAction,
            isSpam = isSpam
        )

        // Save Call & Transcript Entities in Room DB
        callRepository.saveCall(completedCall)
        transcripts.forEach { msg ->
            callRepository.saveTranscript(msg)
        }
    }

    private fun addTranscriptMessage(speaker: String, message: String) {
        val currentCall = _uiState.value.currentCall ?: return
        val msg = TranscriptMessage(
            callId = currentCall.id,
            speaker = speaker,
            message = message,
            timestamp = System.currentTimeMillis()
        )
        _uiState.update {
            it.copy(transcripts = it.transcripts + msg)
        }
    }

    private fun startCallTimer() {
        durationTimerJob?.cancel()
        durationTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { it.copy(callDurationSeconds = it.callDurationSeconds + 1) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        durationTimerJob?.cancel()
        sttManager.release()
        ttsManager.release()
    }
}
