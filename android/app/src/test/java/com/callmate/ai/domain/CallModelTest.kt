package com.callmate.ai.domain

import com.callmate.ai.domain.model.Call
import com.callmate.ai.domain.model.CallCategory
import com.callmate.ai.domain.model.Importance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CallModelTest {

    @Test
    fun testCallCategoryFromString() {
        assertEquals(CallCategory.RECRUITMENT, CallCategory.fromString("RECRUITMENT"))
        assertEquals(CallCategory.WORK, CallCategory.fromString("work"))
        assertEquals(CallCategory.DELIVERY, CallCategory.fromString("Delivery"))
        assertEquals(CallCategory.SPAM, CallCategory.fromString("spam"))
        assertEquals(CallCategory.UNKNOWN, CallCategory.fromString("invalid_category"))
    }

    @Test
    fun testImportanceFromString() {
        assertEquals(Importance.HIGH, Importance.fromString("HIGH"))
        assertEquals(Importance.URGENT, Importance.fromString("urgent"))
        assertEquals(Importance.LOW, Importance.fromString("low"))
        assertEquals(Importance.MEDIUM, Importance.fromString("unknown_val"))
    }

    @Test
    fun testCallModelDefaults() {
        val call = Call(
            id = "test-123",
            phoneNumber = "+1234567890",
            callerName = "John Doe",
            timestamp = 1700000000000L
        )

        assertEquals("test-123", call.id)
        assertEquals(CallCategory.UNKNOWN, call.category)
        assertEquals(Importance.MEDIUM, call.importance)
        assertTrue(call.transcriptAvailable)
    }
}
