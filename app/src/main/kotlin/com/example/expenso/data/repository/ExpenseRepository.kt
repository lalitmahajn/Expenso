package com.example.expenso.data.repository

import com.example.expenso.data.local.ExpenseDao
import com.example.expenso.data.model.ExpenseEntity
import com.example.expenso.data.model.ExpenseWithDetails
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    val allExpenses: Flow<List<ExpenseWithDetails>> = expenseDao.getAllExpensesWithDetails()

    fun getExpensesByDateRange(startTime: Long, endTime: Long): Flow<List<ExpenseWithDetails>> {
        return expenseDao.getExpensesByDateRange(startTime, endTime)
    }

    fun getExpensesByEvent(eventId: Long): Flow<List<ExpenseWithDetails>> {
        return expenseDao.getExpensesByEvent(eventId)
    }

    fun getExpensesForEventAndChildren(eventId: Long): Flow<List<ExpenseWithDetails>> {
        return expenseDao.getExpensesForEventAndChildren(eventId)
    }

    suspend fun getExpensesForEventAndChildrenSync(eventId: Long): List<ExpenseWithDetails> {
        return expenseDao.getExpensesForEventAndChildrenSync(eventId)
    }

    suspend fun deleteExpensesForEventAndChildren(eventId: Long): Int {
        return expenseDao.deleteExpensesForEventAndChildren(eventId)
    }

    suspend fun getExpensesByEventSync(eventId: Long): List<ExpenseWithDetails> {
        return expenseDao.getExpensesByEventSync(eventId)
    }

    suspend fun deleteExpensesByEvent(eventId: Long): Int {
        return expenseDao.deleteExpensesByEvent(eventId)
    }

    fun getExpenseById(id: Long): Flow<ExpenseWithDetails?> {
        return expenseDao.getExpenseWithDetailsById(id)
    }

    suspend fun insertExpense(expense: ExpenseEntity): Long {
        return expenseDao.insertExpense(expense)
    }

    suspend fun insertAll(expenses: List<ExpenseEntity>): List<Long> {
        return expenseDao.insertAll(expenses)
    }

    suspend fun updateExpense(expense: ExpenseEntity): Int {
        return expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity): Int {
        return expenseDao.deleteExpense(expense)
    }

    suspend fun deleteAllExpenses(): Int {
        return expenseDao.deleteAllExpenses()
    }

    suspend fun getAllForExport(): List<ExpenseWithDetails> {
        return expenseDao.getAllExpensesForExport()
    }
}
