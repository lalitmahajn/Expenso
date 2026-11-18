package com.example.expenso.data.repository

import com.example.expenso.data.local.EventDao
import com.example.expenso.data.local.ExpenseDao
import com.example.expenso.data.model.EventEntity
import com.example.expenso.data.model.EventSpendSummary
import com.example.expenso.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class EventRepository(
    private val eventDao: EventDao,
    private val expenseDao: ExpenseDao
) {
    val allEvents: Flow<List<EventEntity>> = eventDao.getAllEvents()
    val activeEvents: Flow<List<EventEntity>> = eventDao.getActiveEvents()
    val archivedEvents: Flow<List<EventEntity>> = eventDao.getArchivedEvents()

    val parentEvents: Flow<List<EventEntity>> = eventDao.getParentEvents()
    val activeParentEvents: Flow<List<EventEntity>> = eventDao.getActiveParentEvents()
    val archivedParentEvents: Flow<List<EventEntity>> = eventDao.getArchivedParentEvents()
    val allSubEvents: Flow<List<EventEntity>> = eventDao.getAllSubEvents()

    fun getEventById(id: Long): Flow<EventEntity?> {
        return eventDao.getEventById(id)
    }

    suspend fun getEventByIdSync(id: Long): EventEntity? {
        return eventDao.getEventByIdSync(id)
    }

    fun getSubEvents(parentEventId: Long): Flow<List<EventEntity>> {
        return eventDao.getSubEvents(parentEventId)
    }

    suspend fun getSubEventsSync(parentEventId: Long): List<EventEntity> {
        return eventDao.getSubEventsSync(parentEventId)
    }

    fun getAllEventSummaries(): Flow<List<EventSpendSummary>> {
        return combine(eventDao.getAllEvents(), expenseDao.getAllExpensesWithDetails()) { events, expenses ->
            val expenseItems = expenses.filter { it.expense.transactionType == TransactionType.EXPENSE }
            val parentEventsList = events.filter { it.parentEventId == null }
            val subEventsList = events.filter { it.parentEventId != null }
            val subEventsByParent = subEventsList.groupBy { it.parentEventId }

            parentEventsList.map { parent ->
                val children = subEventsByParent[parent.id] ?: emptyList()
                val childSummaries = children.map { child ->
                    val childExpenses = expenseItems.filter { it.expense.eventId == child.id }
                    val childTotal = childExpenses.sumOf { it.expense.amount }
                    EventSpendSummary(
                        event = child,
                        totalSpent = childTotal,
                        transactionCount = childExpenses.size,
                        directSpent = childTotal,
                        subEventSummaries = emptyList()
                    )
                }

                val directExpenses = expenseItems.filter { it.expense.eventId == parent.id }
                val directTotal = directExpenses.sumOf { it.expense.amount }
                val totalChildSpent = childSummaries.sumOf { it.totalSpent }
                val totalChildCount = childSummaries.sumOf { it.transactionCount }

                EventSpendSummary(
                    event = parent,
                    totalSpent = directTotal + totalChildSpent,
                    transactionCount = directExpenses.size + totalChildCount,
                    directSpent = directTotal,
                    subEventSummaries = childSummaries
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

    suspend fun deleteEventAndChildren(eventId: Long): Int {
        return eventDao.deleteEventAndChildren(eventId)
    }

    suspend fun deleteAllEvents(): Int {
        return eventDao.deleteAllEvents()
    }

    suspend fun setEventArchived(id: Long, isArchived: Boolean): Int {
        return eventDao.setEventArchived(id, isArchived)
    }
}
