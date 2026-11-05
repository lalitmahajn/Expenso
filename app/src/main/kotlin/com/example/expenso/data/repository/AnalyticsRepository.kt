package com.example.expenso.data.repository

import com.example.expenso.data.local.CategoryDao
import com.example.expenso.data.local.EventDao
import com.example.expenso.data.local.ExpenseDao
import com.example.expenso.data.model.CategorySpendSummary
import com.example.expenso.data.model.EventSpendSummary
import com.example.expenso.data.model.ExpenseWithDetails
import com.example.expenso.data.model.OverallStats
import com.example.expenso.data.model.PaymentMethod
import com.example.expenso.data.model.PaymentMethodSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar

enum class TimePeriodFilter(val displayName: String) {
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    THIS_YEAR("This Year"),
    ALL_TIME("All Time"),
    CUSTOM("Custom")
}

data class TimeRange(
    val startTime: Long,
    val endTime: Long
)

class AnalyticsRepository(
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao,
    private val eventDao: EventDao
) {
    fun getTimeRange(period: TimePeriodFilter, customStart: Long = 0, customEnd: Long = Long.MAX_VALUE): TimeRange {
        val cal = Calendar.getInstance()
        return when (period) {
            TimePeriodFilter.TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                TimeRange(start, cal.timeInMillis)
            }
            TimePeriodFilter.THIS_WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.add(Calendar.DAY_OF_WEEK, 6)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                TimeRange(start, cal.timeInMillis)
            }
            TimePeriodFilter.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                TimeRange(start, cal.timeInMillis)
            }
            TimePeriodFilter.THIS_YEAR -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                TimeRange(start, cal.timeInMillis)
            }
            TimePeriodFilter.ALL_TIME -> TimeRange(0, Long.MAX_VALUE)
            TimePeriodFilter.CUSTOM -> TimeRange(customStart, customEnd)
        }
    }

    fun getCategoryBreakdown(startTime: Long, endTime: Long): Flow<List<CategorySpendSummary>> {
        return combine(
            categoryDao.getAllCategories(),
            expenseDao.getExpensesByDateRange(startTime, endTime)
        ) { categories, expenses ->
            categories.mapNotNull { cat ->
                val catExpenses = expenses.filter { it.expense.categoryId == cat.id }
                if (catExpenses.isNotEmpty()) {
                    CategorySpendSummary(
                        categoryId = cat.id,
                        categoryName = cat.name,
                        colorHex = cat.colorHex,
                        iconName = cat.iconName,
                        totalAmount = catExpenses.sumOf { it.expense.amount },
                        count = catExpenses.size
                    )
                } else null
            }.sortedByDescending { it.totalAmount }
        }
    }

    fun getEventBreakdown(startTime: Long, endTime: Long): Flow<List<EventSpendSummary>> {
        return combine(
            eventDao.getAllEvents(),
            expenseDao.getExpensesByDateRange(startTime, endTime)
        ) { events, expenses ->
            events.mapNotNull { event ->
                val eventExpenses = expenses.filter { it.expense.eventId == event.id }
                if (eventExpenses.isNotEmpty()) {
                    EventSpendSummary(
                        event = event,
                        totalSpent = eventExpenses.sumOf { it.expense.amount },
                        transactionCount = eventExpenses.size
                    )
                } else null
            }.sortedByDescending { it.totalSpent }
        }
    }

    fun getPaymentMethodBreakdown(startTime: Long, endTime: Long): Flow<List<PaymentMethodSummary>> {
        return combine(
            expenseDao.getExpensesByDateRange(startTime, endTime)
        ) { expensesList ->
            val expenses = expensesList[0]
            PaymentMethod.entries.mapNotNull { method ->
                val filtered = expenses.filter { it.expense.paymentMethod == method }
                if (filtered.isNotEmpty()) {
                    PaymentMethodSummary(
                        paymentMethod = method,
                        totalAmount = filtered.sumOf { it.expense.amount },
                        count = filtered.size
                    )
                } else null
            }.sortedByDescending { it.totalAmount }
        }
    }

    fun getOverallStats(startTime: Long, endTime: Long): Flow<OverallStats> {
        return combine(
            expenseDao.getExpensesByDateRange(startTime, endTime)
        ) { expensesList ->
            val expenses = expensesList[0]
            val count = expenses.size
            val total = expenses.sumOf { it.expense.amount }
            val avg = if (count > 0) total / count else 0.0
            val max = expenses.maxOfOrNull { it.expense.amount } ?: 0.0
            OverallStats(
                totalSpent = total,
                transactionCount = count,
                averageTransaction = avg,
                highestExpense = max
            )
        }
    }
}
