package com.example.expenso

import com.example.expenso.data.model.EventEntity
import com.example.expenso.data.model.EventSpendSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SummaryModelsTest {

    @Test
    fun testEventSpendSummary_budgetUnderLimit() {
        val event = EventEntity(
            id = 1,
            name = "Goa Trip",
            targetBudget = 1000.0
        )
        val summary = EventSpendSummary(
            event = event,
            totalSpent = 400.0,
            transactionCount = 3
        )

        assertEquals(0.4f, summary.budgetProgress ?: 0f, 0.001f)
        assertFalse(summary.isOverBudget)
        assertEquals(600.0, summary.remainingBudget ?: 0.0, 0.001)
    }

    @Test
    fun testEventSpendSummary_overBudget() {
        val event = EventEntity(
            id = 2,
            name = "Wedding Party",
            targetBudget = 500.0
        )
        val summary = EventSpendSummary(
            event = event,
            totalSpent = 650.0,
            transactionCount = 5
        )

        assertEquals(1.0f, summary.budgetProgress ?: 0f, 0.001f) // clamped to 1.0
        assertTrue(summary.isOverBudget)
        assertEquals(0.0, summary.remainingBudget ?: 0.0, 0.001)
    }

    @Test
    fun testEventSpendSummary_pureTagMode_noBudget() {
        val event = EventEntity(
            id = 3,
            name = "Daily Group",
            targetBudget = null
        )
        val summary = EventSpendSummary(
            event = event,
            totalSpent = 250.0,
            transactionCount = 2
        )

        assertNull(summary.budgetProgress)
        assertFalse(summary.isOverBudget)
        assertNull(summary.remainingBudget)
    }

    @Test
    fun testEventSpendSummary_archivedEvent() {
        val event = EventEntity(
            id = 4,
            name = "Completed Roadtrip",
            isArchived = true
        )
        val summary = EventSpendSummary(
            event = event,
            totalSpent = 350.0,
            transactionCount = 2
        )
        assertTrue(summary.event.isArchived)
    }
}
