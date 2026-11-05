package com.example.expenso.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.expenso.data.model.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY name ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: Long): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(accounts: List<AccountEntity>): List<Long>

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Query("UPDATE accounts SET balance = balance - :amount, updatedAt = :updatedAt WHERE id = :id")
    suspend fun debitBalance(id: Long, amount: Double, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE accounts SET balance = balance + :amount, updatedAt = :updatedAt WHERE id = :id")
    suspend fun creditBalance(id: Long, amount: Double, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE accounts SET balance = :newBalance, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setBalance(id: Long, newBalance: Double, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun getAccountCount(): Int
}
