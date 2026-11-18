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

    @Test
    fun testEventSpendSummary_hierarchicalRolledUpSpend() {
        val parent = EventEntity(
            id = 10,
            name = "Sister's Wedding",
            targetBudget = 5000.0
        )

        val haldi = EventEntity(id = 11, name = "Haldi", targetBudget = 400.0, parentEventId = 10, colorHex = "#FF9800")
        val mehendi = EventEntity(id = 12, name = "Mehendi", targetBudget = 500.0, parentEventId = 10, colorHex = "#4CAF50")
        val sangeet = EventEntity(id = 13, name = "Sangeet", targetBudget = 800.0, parentEventId = 10, colorHex = "#E91E63")

        val haldiSummary = EventSpendSummary(event = haldi, totalSpent = 220.0, transactionCount = 2, directSpent = 220.0)
        val mehendiSummary = EventSpendSummary(event = mehendi, totalSpent = 310.0, transactionCount = 1, directSpent = 310.0)
        val sangeetSummary = EventSpendSummary(event = sangeet, totalSpent = 650.0, transactionCount = 3, directSpent = 650.0)

        val childSummaries = listOf(haldiSummary, mehendiSummary, sangeetSummary)
        val directSpend = 1600.0
        val totalRolledUp = directSpend + childSummaries.sumOf { it.totalSpent } // 1600 + 1180 = 2780.0
        val totalTransactions = 2 + childSummaries.sumOf { it.transactionCount } // 2 + 6 = 8

        val parentSummary = EventSpendSummary(
            event = parent,
            totalSpent = totalRolledUp,
            transactionCount = totalTransactions,
            directSpent = directSpend,
            subEventSummaries = childSummaries
        )

        assertTrue(parentSummary.hasSubEvents)
        assertEquals(3, parentSummary.subEventCount)
        assertEquals(1600.0, parentSummary.directSpent, 0.001)
        assertEquals(2780.0, parentSummary.totalSpent, 0.001)
        assertEquals(8, parentSummary.transactionCount)
        assertEquals(2220.0, parentSummary.remainingBudget ?: 0.0, 0.001)
        assertFalse(parentSummary.isOverBudget)
        assertEquals(0.556f, parentSummary.budgetProgress ?: 0f, 0.001f)

        // Test sub-event specific stats
        assertEquals(400.0, haldiSummary.event.targetBudget ?: 0.0, 0.001)
        assertEquals(180.0, haldiSummary.remainingBudget ?: 0.0, 0.001)
        assertEquals("#FF9800", haldiSummary.event.colorHex)
    }

    @Test
    fun testEventSpendSummary_subEventOverBudgetIndependent() {
        val sangeet = EventEntity(id = 20, name = "Sangeet", targetBudget = 500.0, parentEventId = 10)
        val sangeetSummary = EventSpendSummary(event = sangeet, totalSpent = 650.0, transactionCount = 4)

        assertTrue(sangeetSummary.isOverBudget)
        assertEquals(1.0f, sangeetSummary.budgetProgress ?: 0f, 0.001f)
        assertEquals(0.0, sangeetSummary.remainingBudget ?: 0.0, 0.001)
    }
}
