package com.example.expenso.data.repository

import com.example.expenso.data.local.EventDao
import com.example.expenso.data.local.ExpenseDao
import com.example.expenso.data.model.EventEntity
import com.example.expenso.data.model.EventSpendSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class EventRepository(
    private val eventDao: EventDao,
    private val expenseDao: ExpenseDao
) {
    val allEvents: Flow<List<EventEntity>> = eventDao.getAllEvents()
    val activeEvents: Flow<List<EventEntity>> = eventDao.getActiveEvents()
    val archivedEvents: Flow<List<EventEntity>> = eventDao.getArchivedEvents()

    fun getEventById(id: Long): Flow<EventEntity?> {
        return eventDao.getEventById(id)
    }

    fun getAllEventSummaries(): Flow<List<EventSpendSummary>> {
        return combine(eventDao.getAllEvents(), expenseDao.getAllExpensesWithDetails()) { events, expenses ->
            events.map { event ->
                val eventExpenses = expenses.filter { it.expense.eventId == event.id }
                val total = eventExpenses.sumOf { it.expense.amount }
                EventSpendSummary(
                    event = event,
                    totalSpent = total,
                    transactionCount = eventExpenses.size
                )
            }
        }
    }

    suspend fun insertEvent(event: EventEntity): Long {
        return eventDao.insertEvent(event)
    }

    suspend fun updateEvent(event: EventEntity): Int {
        return eventDao.updateEvent(event)
    }

    suspend fun deleteEvent(event: EventEntity): Int {
        return eventDao.deleteEvent(event)
    }

    suspend fun setEventArchived(id: Long, isArchived: Boolean): Int {
        return eventDao.setEventArchived(id, isArchived)
    }
}
