package com.callmate.ai.data.repository

import com.callmate.ai.data.local.dao.CallDao
import com.callmate.ai.data.local.dao.TranscriptDao
import com.callmate.ai.data.local.entity.CallEntity
import com.callmate.ai.data.local.entity.TranscriptEntity
import com.callmate.ai.domain.model.Call
import com.callmate.ai.domain.model.TranscriptMessage
import com.callmate.ai.domain.repository.CallRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CallRepositoryImpl(
    private val callDao: CallDao,
    private val transcriptDao: TranscriptDao
) : CallRepository {

    override fun getAllCalls(): Flow<List<Call>> {
        return callDao.getAllCalls().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRecentCalls(limit: Int): Flow<List<Call>> {
        return callDao.getRecentCalls(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCallById(callId: String): Flow<Call?> {
        return callDao.getCallById(callId).map { it?.toDomain() }
    }

    override fun getTranscriptsForCall(callId: String): Flow<List<TranscriptMessage>> {
        return transcriptDao.getTranscriptsForCall(callId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveCall(call: Call) {
        callDao.insertCall(CallEntity.fromDomain(call))
    }

    override suspend fun saveTranscript(message: TranscriptMessage) {
        transcriptDao.insertTranscript(TranscriptEntity.fromDomain(message))
    }

    override suspend fun deleteCall(callId: String) {
        transcriptDao.deleteTranscriptsForCall(callId)
        callDao.deleteCallById(callId)
    }

    override suspend fun clearAllCalls() {
        callDao.clearAllCalls()
    }

    override suspend fun clearAllTranscripts() {
        transcriptDao.clearAllTranscripts()
    }
}
