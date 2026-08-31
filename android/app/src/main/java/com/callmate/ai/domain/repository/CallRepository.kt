package com.callmate.ai.domain.repository

import com.callmate.ai.domain.model.Call
import com.callmate.ai.domain.model.TranscriptMessage
import kotlinx.coroutines.flow.Flow

interface CallRepository {
    fun getAllCalls(): Flow<List<Call>>
    fun getRecentCalls(limit: Int = 10): Flow<List<Call>>
    fun getCallById(callId: String): Flow<Call?>
    fun getTranscriptsForCall(callId: String): Flow<List<TranscriptMessage>>
    suspend fun saveCall(call: Call)
    suspend fun saveTranscript(message: TranscriptMessage)
    suspend fun deleteCall(callId: String)
    suspend fun clearAllCalls()
    suspend fun clearAllTranscripts()
}
