package com.example.expenso.test

import com.example.expenso.data.local.AccountDao
import com.example.expenso.data.model.AccountEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeAccountDao : AccountDao {
    private val accountsMap = mutableMapOf<Long, AccountEntity>()
    private val accountsFlow = MutableStateFlow<List<AccountEntity>>(emptyList())
    private var nextId = 1L

    private fun emit() {
        accountsFlow.value = accountsMap.values.sortedBy { it.name }
    }

    override fun getAllAccounts(): Flow<List<AccountEntity>> = accountsFlow

    override suspend fun getAllAccountsSync(): List<AccountEntity> = accountsMap.values.sortedBy { it.id }

    override suspend fun getAccountById(id: Long): AccountEntity? = accountsMap[id]

    override suspend fun insertAccount(account: AccountEntity): Long {
        val id = if (account.id == 0L) nextId++ else account.id
        val entity = account.copy(id = id)
        accountsMap[id] = entity
        emit()
        return id
    }

    override suspend fun insertAll(accounts: List<AccountEntity>): List<Long> {
        val ids = mutableListOf<Long>()
        for (acc in accounts) {
            ids.add(insertAccount(acc))
        }
        return ids
    }

    override suspend fun updateAccount(account: AccountEntity) {
        accountsMap[account.id] = account
        emit()
    }

    override suspend fun deleteAccount(account: AccountEntity) {
        accountsMap.remove(account.id)
        emit()
    }

    override suspend fun deleteAllAccounts(): Int {
        val count = accountsMap.size
        accountsMap.clear()
        emit()
        return count
    }

    override suspend fun debitBalance(id: Long, amount: Double, updatedAt: Long) {
        val existing = accountsMap[id] ?: return
        accountsMap[id] = existing.copy(balance = existing.balance - amount, updatedAt = updatedAt)
        emit()
    }

    override suspend fun creditBalance(id: Long, amount: Double, updatedAt: Long) {
        val existing = accountsMap[id] ?: return
        accountsMap[id] = existing.copy(balance = existing.balance + amount, updatedAt = updatedAt)
        emit()
    }

    override suspend fun setBalance(id: Long, newBalance: Double, updatedAt: Long) {
        val existing = accountsMap[id] ?: return
        accountsMap[id] = existing.copy(balance = newBalance, updatedAt = updatedAt)
        emit()
    }

    override suspend fun getAccountCount(): Int = accountsMap.size
}
