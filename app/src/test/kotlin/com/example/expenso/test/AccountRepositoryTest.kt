package com.example.expenso.test

import com.example.expenso.data.model.AccountEntity
import com.example.expenso.data.model.AccountType
import com.example.expenso.data.repository.AccountRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class AccountRepositoryTest {

    private lateinit var fakeDao: FakeAccountDao
    private lateinit var repository: AccountRepository

    @Before
    fun setUp() {
        fakeDao = FakeAccountDao()
        repository = AccountRepository(fakeDao)
    }

    @Test
    fun testInsertAndRetrieveAccount() = runTest {
        val account = AccountEntity(
            id = 1L,
            name = "Main Bank",
            type = AccountType.BANK,
            balance = 5000.0,
            colorHex = "#1976D2"
        )
        val id = repository.insertAccount(account)
        assertEquals(1L, id)

        val retrieved = repository.getAccountById(1L)
        assertNotNull(retrieved)
        assertEquals("Main Bank", retrieved?.name)
        assertEquals(5000.0, retrieved?.balance ?: 0.0, 0.001)
    }

    @Test
    fun testDebitAndCreditAccount() = runTest {
        val account = AccountEntity(
            id = 1L,
            name = "Cash Wallet",
            type = AccountType.CASH,
            balance = 1000.0,
            colorHex = "#388E3C"
        )
        repository.insertAccount(account)

        // Debit 250
        repository.debitAccount(1L, 250.0)
        var updated = repository.getAccountById(1L)
        assertEquals(750.0, updated?.balance ?: 0.0, 0.001)

        // Credit 500
        repository.creditAccount(1L, 500.0)
        updated = repository.getAccountById(1L)
        assertEquals(1250.0, updated?.balance ?: 0.0, 0.001)
    }

    @Test
    fun testTransferFundsBetweenAccounts() = runTest {
        val bank = AccountEntity(
            id = 1L,
            name = "Savings Account",
            type = AccountType.BANK,
            balance = 10000.0,
            colorHex = "#1976D2"
        )
        val creditCard = AccountEntity(
            id = 2L,
            name = "Credit Card",
            type = AccountType.CREDIT_CARD,
            balance = 2000.0, // liability
            colorHex = "#D32F2F"
        )
        repository.insertAccount(bank)
        repository.insertAccount(creditCard)

        // Transfer 1500 from Bank to pay Credit Card
        repository.transferFunds(fromAccountId = 1L, toAccountId = 2L, amount = 1500.0)

        val updatedBank = repository.getAccountById(1L)
        val updatedCard = repository.getAccountById(2L)

        assertEquals(8500.0, updatedBank?.balance ?: 0.0, 0.001)
        assertEquals(3500.0, updatedCard?.balance ?: 0.0, 0.001)
    }

    @Test
    fun testSetBalanceDirectly() = runTest {
        val account = AccountEntity(
            id = 1L,
            name = "UPI",
            type = AccountType.WALLET,
            balance = 500.0,
            colorHex = "#7B1FA2"
        )
        repository.insertAccount(account)

        repository.setAccountBalance(1L, 1200.0)
        val updated = repository.getAccountById(1L)
        assertEquals(1200.0, updated?.balance ?: 0.0, 0.001)
    }

    @Test
    fun testAllAccountsFlowEmitsSortedList() = runTest {
        repository.insertAccount(AccountEntity(id = 1L, name = "Zeta Bank", type = AccountType.BANK, balance = 100.0, colorHex = "#000"))
        repository.insertAccount(AccountEntity(id = 2L, name = "Alpha Cash", type = AccountType.CASH, balance = 200.0, colorHex = "#000"))

        val accounts = repository.allAccounts.first()
        assertEquals(2, accounts.size)
        assertEquals("Alpha Cash", accounts[0].name)
        assertEquals("Zeta Bank", accounts[1].name)
    }
}
