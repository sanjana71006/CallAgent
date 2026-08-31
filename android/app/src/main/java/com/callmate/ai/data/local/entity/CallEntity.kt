package com.callmate.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.callmate.ai.domain.model.Call
import com.callmate.ai.domain.model.CallCategory
import com.callmate.ai.domain.model.Importance

@Entity(tableName = "calls")
data class CallEntity(
    @PrimaryKey val id: String,
    val phoneNumber: String,
    val callerName: String,
    val timestamp: Long,
    val durationSeconds: Int,
    val status: String,
    val category: String,
    val importance: String,
    val summary: String,
    val purpose: String,
    val importantDetails: String,
    val recommendation: String,
    val isSpam: Boolean,
    val transcriptAvailable: Boolean
) {
    fun toDomain(): Call {
        return Call(
            id = id,
            phoneNumber = phoneNumber,
            callerName = callerName,
            timestamp = timestamp,
            durationSeconds = durationSeconds,
            status = status,
            category = CallCategory.fromString(category),
            importance = Importance.fromString(importance),
            summary = summary,
            purpose = purpose,
            importantDetails = importantDetails,
            recommendation = recommendation,
            isSpam = isSpam,
            transcriptAvailable = transcriptAvailable
        )
    }

    companion object {
        fun fromDomain(call: Call): CallEntity {
            return CallEntity(
                id = call.id,
                phoneNumber = call.phoneNumber,
                callerName = call.callerName,
                timestamp = call.timestamp,
                durationSeconds = call.durationSeconds,
                status = call.status,
                category = call.category.name,
                importance = call.importance.name,
                summary = call.summary,
                purpose = call.purpose,
                importantDetails = call.importantDetails,
                recommendation = call.recommendation,
                isSpam = call.isSpam,
                transcriptAvailable = call.transcriptAvailable
            )
        }
    }
}
