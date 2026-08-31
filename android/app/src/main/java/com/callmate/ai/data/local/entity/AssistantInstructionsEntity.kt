package com.callmate.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assistant_instructions")
data class AssistantInstructionsEntity(
    @PrimaryKey val id: String = "default_instructions",
    val instructions: String = "Be polite and concise. Ask unknown callers why they are calling. Never share my personal information.",
    val updatedAt: Long = System.currentTimeMillis()
)
