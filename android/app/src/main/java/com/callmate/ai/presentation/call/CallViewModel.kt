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

                val response = try {
                    apiService.chat(requestDto)
                } catch (e: Exception) {
                    null
                }

                val (aiReply, isComplete) = if (response != null && response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    Pair(body.response, body.isCallComplete)
                } else {
                    generateContextualAiResponse(
                        speech = callerText,
                        history = _uiState.value.transcripts,
                        assistantName = activeSettings.assistantName,
                        instructions = customInstructions
                    )
                }

                addTranscriptMessage(speaker = "ai", message = aiReply)
                _uiState.update { it.copy(aiVoiceState = AiVoiceState.SPEAKING) }
                ttsManager.speak(aiReply, pitch = activeSettings.speechPitch, rate = activeSettings.speechRate)

                if (isComplete) {
                    delay(2500)
                    endCall()
                }
            } catch (e: Exception) {
                val (fallbackReply, isDone) = generateContextualAiResponse(
                    speech = callerText,
                    history = _uiState.value.transcripts,
                    assistantName = activeSettings.assistantName,
                    instructions = customInstructions
                )
                addTranscriptMessage(speaker = "ai", message = fallbackReply)
                _uiState.update { it.copy(aiVoiceState = AiVoiceState.SPEAKING) }
                ttsManager.speak(fallbackReply, pitch = activeSettings.speechPitch, rate = activeSettings.speechRate)
                if (isDone) {
                    delay(2500)
                    endCall()
                }
            }
        }
    }

    private fun generateContextualAiResponse(
        speech: String,
        history: List<TranscriptMessage>,
        assistantName: String,
        instructions: String
    ): Pair<String, Boolean> {
        val lower = speech.lowercase().trim()
        val callerTurnsCount = history.count { it.speaker == "caller" }

        // 1. Delivery & Couriers
        if (lower.contains("otp") || lower.contains("pin") || lower.contains("code") || lower.contains("verification")) {
            return Pair("For security, OTPs cannot be shared over a phone call. Please leave the package at the doorstep or with building security.", false)
        }
        if (lower.contains("delivery") || lower.contains("courier") || lower.contains("package") ||
            lower.contains("parcel") || lower.contains("amazon") || lower.contains("swiggy") ||
            lower.contains("zomato") || lower.contains("blinkit") || lower.contains("gate") || lower.contains("door")) {
            if (lower.contains("outside") || lower.contains("arrived") || lower.contains("here") || lower.contains("reach")) {
                return Pair("Thank you! Please leave the parcel at the doorstep or security gate. I have notified the user.", true)
            }
            return Pair("Thank you for the delivery update! Please leave the package at the door. Do you need any directions?", false)
        }

        // 2. Job / Interview / Recruiter
        if (lower.contains("interview") || lower.contains("recruiter") || lower.contains("hiring") ||
            lower.contains("job") || lower.contains("resume") || lower.contains("position") || lower.contains("hr")) {
            if (callerTurnsCount <= 1) {
                return Pair("Thank you for reaching out regarding this opportunity. Which company and role is this for, and what is your preferred callback time?", false)
            } else {
                return Pair("Got it, I have recorded the interview notes and scheduled details for the user. Have a great day!", true)
            }
        }

        // 3. Telemarketing / Sales / Loans / Cards / Promotions
        if (lower.contains("loan") || lower.contains("credit card") || lower.contains("insurance") ||
            lower.contains("investment") || lower.contains("offer") || lower.contains("promotion") ||
            lower.contains("crypto") || lower.contains("free gift")) {
            return Pair("Thank you for calling, but the user is not interested in marketing or commercial offers. Please remove this number from your list. Goodbye.", true)
        }

        // 4. Urgent / Emergency
        if (lower.contains("urgent") || lower.contains("emergency") || lower.contains("hospital") ||
            lower.contains("accident") || lower.contains("doctor") || lower.contains("asap")) {
            return Pair("Understood, I am marking this call as urgent and alerting the user immediately.", true)
        }

        // 5. Work / Project / Meetings
        if (lower.contains("meeting") || lower.contains("project") || lower.contains("client") ||
            lower.contains("deadline") || lower.contains("report") || lower.contains("office")) {
            return Pair("Thank you for the update. Could you please leave a brief message regarding the agenda so I can pass it along?", false)
        }

        // 6. Identity / AI Bot inquiries
        if (lower.contains("who are you") || lower.contains("is this ai") || lower.contains("are you a bot") || lower.contains("robot") || lower.contains("human")) {
            return Pair("I am $assistantName, an AI call assistant screening this call on behalf of the user. How may I assist you?", false)
        }

        // 7. Greeting / Speaking request
        if (lower.contains("hello") || lower.contains("hi") || lower.contains("hey") || lower.contains("can i speak") || lower.contains("is this")) {
            return Pair("Hello! The user is currently unavailable. May I ask what this call is regarding so I can relay your message?", false)
        }

        // 8. Ending Call / Bye
        if (lower.contains("bye") || lower.contains("goodbye") || lower.contains("thank you") ||
            lower.contains("thanks") || lower.contains("that's all") || lower.contains("nothing else")) {
            return Pair("Thank you for calling. I have saved our conversation and notified the user. Have a wonderful day!", true)
        }

        // 9. Multi-turn conversational fallback
        return when (callerTurnsCount) {
            1 -> Pair("Thank you for providing that. May I take down your name and callback number?", false)
            2 -> Pair("Understood, I have recorded your message. Is there any specific time you'd prefer a callback?", false)
            else -> Pair("Thank you, I have logged all the details from this call and will update the user right away. Goodbye!", true)
        }
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
