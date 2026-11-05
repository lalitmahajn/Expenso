package com.example.expenso.data.repository

import com.example.expenso.data.local.AccountDao
import com.example.expenso.data.local.AppDatabase
import com.example.expenso.data.model.AccountEntity
import kotlinx.coroutines.flow.Flow

class AccountRepository(private val accountDao: AccountDao) {

    val allAccounts: Flow<List<AccountEntity>> = accountDao.getAllAccounts()

    suspend fun getAccountById(id: Long): AccountEntity? = accountDao.getAccountById(id)

    suspend fun insertAccount(account: AccountEntity): Long = accountDao.insertAccount(account)

    suspend fun updateAccount(account: AccountEntity) = accountDao.updateAccount(account)

    suspend fun deleteAccount(account: AccountEntity) = accountDao.deleteAccount(account)

    suspend fun debitAccount(id: Long, amount: Double) {
        accountDao.debitBalance(id, amount)
    }

    suspend fun creditAccount(id: Long, amount: Double) {
        accountDao.creditBalance(id, amount)
    }

    suspend fun setAccountBalance(id: Long, newBalance: Double) {
        accountDao.setBalance(id, newBalance)
    }

    suspend fun ensureDefaultAccounts() {
        if (accountDao.getAccountCount() == 0) {
            accountDao.insertAll(AppDatabase.DEFAULT_ACCOUNTS)
        }
    }
}
