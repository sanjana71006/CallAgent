package com.callmate.ai.domain.model

enum class CallCategory(val displayName: String) {
    PERSONAL("Personal"),
    WORK("Work"),
    RECRUITMENT("Recruitment"),
    DELIVERY("Delivery"),
    BANKING("Banking"),
    SERVICE("Service"),
    SALES("Sales"),
    TELEMARKETING("Telemarketing"),
    SPAM("Spam"),
    UNKNOWN("General Inquiry");

    companion object {
        fun fromString(value: String?): CallCategory {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
        }
    }
}
