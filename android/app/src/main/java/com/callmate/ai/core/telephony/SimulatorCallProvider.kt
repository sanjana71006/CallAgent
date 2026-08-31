package com.callmate.ai.core.telephony

import com.callmate.ai.domain.model.Call
import com.callmate.ai.domain.model.CallCategory
import com.callmate.ai.domain.model.Importance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class SimulatorCallProvider : CallProvider {

    private val _activeIncomingCall = MutableStateFlow<Call?>(null)
    override val activeIncomingCall: StateFlow<Call?> = _activeIncomingCall.asStateFlow()

    private val _isCallActive = MutableStateFlow(false)
    override val isCallActive: StateFlow<Boolean> = _isCallActive.asStateFlow()

    override fun startIncomingCall(callerName: String, phoneNumber: String): Call {
        val call = Call(
            id = "sim-" + UUID.randomUUID().toString().take(8),
            phoneNumber = phoneNumber,
            callerName = callerName,
            timestamp = System.currentTimeMillis(),
            status = "INCOMING",
            category = CallCategory.UNKNOWN,
            importance = Importance.MEDIUM
        )
        _activeIncomingCall.value = call
        _isCallActive.value = false
        return call
    }

    override fun answerCallWithAi(callId: String) {
        val current = _activeIncomingCall.value
        if (current?.id == callId) {
            _activeIncomingCall.value = current.copy(status = "SCREENED")
            _isCallActive.value = true
        }
    }

    override fun takeOverCall(callId: String) {
        val current = _activeIncomingCall.value
        if (current?.id == callId) {
            _activeIncomingCall.value = current.copy(status = "TAKEN_OVER")
        }
    }

    override fun endCall(callId: String) {
        _activeIncomingCall.value = null
        _isCallActive.value = false
    }
}
