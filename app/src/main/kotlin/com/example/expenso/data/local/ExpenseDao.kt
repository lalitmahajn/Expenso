package com.example.expenso.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.expenso.data.model.ExpenseEntity
import com.example.expenso.data.model.ExpenseWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Transaction
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpensesWithDetails(): Flow<List<ExpenseWithDetails>>

    @Transaction
    @Query("SELECT * FROM expenses WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getExpensesByDateRange(startTime: Long, endTime: Long): Flow<List<ExpenseWithDetails>>

    @Transaction
    @Query("SELECT * FROM expenses WHERE eventId = :eventId ORDER BY timestamp DESC")
    fun getExpensesByEvent(eventId: Long): Flow<List<ExpenseWithDetails>>

    @Transaction
    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    fun getExpenseWithDetailsById(id: Long): Flow<ExpenseWithDetails?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expenses: List<ExpenseEntity>): List<Long>

    @Update
    suspend fun updateExpense(expense: ExpenseEntity): Int

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity): Int

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE transactionType = 'EXPENSE' AND timestamp BETWEEN :startTime AND :endTime")
    fun getTotalSpentBetween(startTime: Long, endTime: Long): Flow<Double>

    @Query("SELECT COUNT(*) FROM expenses WHERE transactionType = 'EXPENSE' AND timestamp BETWEEN :startTime AND :endTime")
    fun getTransactionCountBetween(startTime: Long, endTime: Long): Flow<Int>

    @Query("SELECT COALESCE(MAX(amount), 0.0) FROM expenses WHERE transactionType = 'EXPENSE' AND timestamp BETWEEN :startTime AND :endTime")
    fun getMaxExpenseBetween(startTime: Long, endTime: Long): Flow<Double>

    @Query("SELECT COUNT(*) FROM expenses")
    suspend fun getTotalExpenseCount(): Int

    @Transaction
    @Query("SELECT * FROM expenses ORDER BY timestamp ASC")
    suspend fun getAllExpensesForExport(): List<ExpenseWithDetails>
}
