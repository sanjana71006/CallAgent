package com.callmate.ai.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "call_summaries",
    foreignKeys = [
        ForeignKey(
            entity = CallEntity::class,
            parentColumns = ["id"],
            childColumns = ["callId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("callId")]
)
data class CallSummaryEntity(
    @PrimaryKey val id: String,
    val callId: String,
    val purpose: String,
    val importantInformation: String,
    val recommendedAction: String,
    val category: String,
    val createdAt: Long = System.currentTimeMillis()
)
