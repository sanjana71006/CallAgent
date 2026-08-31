package com.callmate.ai.core.telephony

import com.callmate.ai.domain.model.Call
import kotlinx.coroutines.flow.StateFlow

interface CallProvider {
    val activeIncomingCall: StateFlow<Call?>
    val isCallActive: StateFlow<Boolean>

    fun startIncomingCall(callerName: String = "Unknown Caller", phoneNumber: String = "+1 (555) 019-2834"): Call
    fun answerCallWithAi(callId: String)
    fun takeOverCall(callId: String)
    fun endCall(callId: String)
}
