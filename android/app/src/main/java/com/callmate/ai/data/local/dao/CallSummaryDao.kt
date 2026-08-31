package com.callmate.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.callmate.ai.data.local.entity.CallSummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CallSummaryDao {

    @Query("SELECT * FROM call_summaries WHERE callId = :callId LIMIT 1")
    fun getSummaryForCall(callId: String): Flow<CallSummaryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: CallSummaryEntity)

    @Query("DELETE FROM call_summaries")
    suspend fun clearAllSummaries()
}
