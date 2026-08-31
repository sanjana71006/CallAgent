package com.callmate.ai.domain.model

enum class Importance(val level: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    URGENT("Urgent");

    companion object {
        fun fromString(value: String?): Importance {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: MEDIUM
        }
    }
}
