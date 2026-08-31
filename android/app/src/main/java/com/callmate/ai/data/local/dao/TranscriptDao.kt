package com.callmate.ai.data.local.dao

import androidx.room.*
import com.callmate.ai.data.local.entity.TranscriptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TranscriptDao {
    @Query("SELECT * FROM transcripts WHERE callId = :callId ORDER BY timestamp ASC")
    fun getTranscriptsForCall(callId: String): Flow<List<TranscriptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranscript(transcript: TranscriptEntity)

    @Query("DELETE FROM transcripts WHERE callId = :callId")
    suspend fun deleteTranscriptsForCall(callId: String)

    @Query("DELETE FROM transcripts")
    suspend fun clearAllTranscripts()
}
