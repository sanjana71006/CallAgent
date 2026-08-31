package com.callmate.ai.data.local.dao

import androidx.room.*
import com.callmate.ai.data.local.entity.CallEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CallDao {
    @Query("SELECT * FROM calls ORDER BY timestamp DESC")
    fun getAllCalls(): Flow<List<CallEntity>>

    @Query("SELECT * FROM calls ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentCalls(limit: Int): Flow<List<CallEntity>>

    @Query("SELECT * FROM calls WHERE id = :callId LIMIT 1")
    fun getCallById(callId: String): Flow<CallEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCall(call: CallEntity)

    @Query("DELETE FROM calls WHERE id = :callId")
    suspend fun deleteCallById(callId: String)

    @Query("DELETE FROM calls")
    suspend fun clearAllCalls()
}
