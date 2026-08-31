package com.callmate.ai.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.callmate.ai.domain.model.TranscriptMessage

@Entity(
    tableName = "transcripts",
    foreignKeys = [
        ForeignKey(
            entity = CallEntity::class,
            parentColumns = ["id"],
            childColumns = ["callId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["callId"])]
)
data class TranscriptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val callId: String,
    val speaker: String,
    val message: String,
    val timestamp: Long
) {
    fun toDomain(): TranscriptMessage {
        return TranscriptMessage(
            id = id,
            callId = callId,
            speaker = speaker,
            message = message,
            timestamp = timestamp
        )
    }

    companion object {
        fun fromDomain(msg: TranscriptMessage): TranscriptEntity {
            return TranscriptEntity(
                id = msg.id,
                callId = msg.callId,
                speaker = msg.speaker,
                message = msg.message,
                timestamp = msg.timestamp
            )
        }
    }
}
