package com.example.expenso.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expenso.data.model.AccountEntity
import com.example.expenso.data.model.CategoryEntity
import com.example.expenso.data.model.EventEntity
import com.example.expenso.data.model.ExpenseEntity
import com.example.expenso.data.model.ExpenseWithDetails
import com.example.expenso.data.model.PaymentMethod
import com.example.expenso.data.preferences.UserPreferences
import com.example.expenso.data.repository.AccountRepository
import com.example.expenso.data.repository.CategoryRepository
import com.example.expenso.data.repository.EventRepository
import com.example.expenso.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.expenso.data.model.TransactionType

data class ExpensesUiState(
    val expenses: List<ExpenseWithDetails> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val events: List<EventEntity> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedCategoryId: Long? = null,
    val selectedEventId: Long? = null,
    val currency: String = "$",
    val totalAmount: Double = 0.0,
    val totalIncome: Double = 0.0,
    val isLoading: Boolean = false
)

class ExpenseViewModel(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val eventRepository: EventRepository,
    private val accountRepository: AccountRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId = _selectedCategoryId.asStateFlow()

    private val _selectedEventId = MutableStateFlow<Long?>(null)
    val selectedEventId = _selectedEventId.asStateFlow()

    private data class FilterParams(
        val query: String,
        val categoryId: Long?,
        val eventId: Long?
    )

    private val filterParams = combine(_searchQuery, _selectedCategoryId, _selectedEventId) { query, catId, evId ->
        FilterParams(query, catId, evId)
    }

    private data class AccountAndCurrency(
        val accounts: List<AccountEntity>,
        val currency: String
    )

    private val accountAndCurrency = combine(accountRepository.allAccounts, userPreferences.currency) { acc, curr ->
        AccountAndCurrency(acc, curr)
    }

    val uiState: StateFlow<ExpensesUiState> = combine(
        expenseRepository.allExpenses,
        categoryRepository.allCategories,
        eventRepository.activeEvents,
        filterParams,
        accountAndCurrency
    ) { expenses, categories, events, filters, accCurr ->
        val filtered = expenses.filter { item ->
            val matchesQuery = filters.query.isBlank() ||
                item.expense.title.contains(filters.query, ignoreCase = true) ||
                (item.category?.name?.contains(filters.query, ignoreCase = true) == true) ||
                (item.event?.name?.contains(filters.query, ignoreCase = true) == true) ||
                item.expense.note.contains(filters.query, ignoreCase = true)

            val matchesCategory = filters.categoryId == null || item.expense.categoryId == filters.categoryId
            val matchesEvent = filters.eventId == null || item.expense.eventId == filters.eventId

            matchesQuery && matchesCategory && matchesEvent
        }

        val totalExpenses = filtered.filter { it.expense.transactionType == TransactionType.EXPENSE }.sumOf { it.expense.amount }
        val totalIncome = filtered.filter { it.expense.transactionType == TransactionType.INCOME }.sumOf { it.expense.amount }

        ExpensesUiState(
            expenses = filtered,
            categories = categories,
            events = events,
            accounts = accCurr.accounts,
            searchQuery = filters.query,
            selectedCategoryId = filters.categoryId,
            selectedEventId = filters.eventId,
            currency = accCurr.currency,
            totalAmount = totalExpenses,
            totalIncome = totalIncome,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExpensesUiState(isLoading = true)
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(categoryId: Long?) {
        _selectedCategoryId.value = if (_selectedCategoryId.value == categoryId) null else categoryId
    }

    fun selectEvent(eventId: Long?) {
        _selectedEventId.value = if (_selectedEventId.value == eventId) null else eventId
    }

    fun addExpense(
        title: String,
        amount: Double,
        categoryId: Long,
        eventId: Long?,
        paymentMethod: PaymentMethod,
        note: String = "",
        accountId: Long? = null,
        timestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            val expense = ExpenseEntity(
                title = title.trim(),
                amount = amount,
                categoryId = categoryId,
                eventId = eventId,
                accountId = accountId,
                paymentMethod = paymentMethod,
                note = note.trim(),
                timestamp = timestamp
            )
            expenseRepository.insertExpense(expense)

            if (accountId != null) {
                accountRepository.debitAccount(accountId, amount)
            }
        }
    }

    fun updateExpense(
        original: ExpenseEntity,
        title: String,
        amount: Double,
        categoryId: Long,
        eventId: Long?,
        paymentMethod: PaymentMethod,
        note: String = "",
        accountId: Long? = null
    ) {
        viewModelScope.launch {
            if (original.accountId != null) {
                accountRepository.creditAccount(original.accountId, original.amount)
            }
            if (accountId != null) {
                accountRepository.debitAccount(accountId, amount)
            }
            val updated = original.copy(
                title = title.trim(),
                amount = amount,
                categoryId = categoryId,
                eventId = eventId,
                accountId = accountId,
                paymentMethod = paymentMethod,
                note = note.trim()
            )
            expenseRepository.updateExpense(updated)
        }
    }

    fun addCategory(
        name: String,
        iconName: String,
        colorHex: String,
        onCreated: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val category = CategoryEntity(
                name = name.trim(),
                iconName = iconName,
                colorHex = colorHex
            )
            val newId = categoryRepository.insertCategory(category)
            onCreated(newId)
        }
    }

    fun addIncome(
        title: String,
        amount: Double,
        categoryId: Long,
        accountId: Long?,
        paymentMethod: PaymentMethod = PaymentMethod.BANK_TRANSFER,
        note: String = "",
        eventId: Long? = null,
        timestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            val income = ExpenseEntity(
                title = title.trim(),
                amount = amount,
                categoryId = categoryId,
                eventId = eventId,
                accountId = accountId,
                toAccountId = null,
                transactionType = TransactionType.INCOME,
                paymentMethod = paymentMethod,
                note = note.trim(),
                timestamp = timestamp
            )
            expenseRepository.insertExpense(income)

            if (accountId != null) {
                accountRepository.creditAccount(accountId, amount)
            }
        }
    }

    fun addTransfer(
        amount: Double,
        fromAccountId: Long,
        toAccountId: Long,
        note: String = "",
        timestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            val fromAcc = accountRepository.getAccountById(fromAccountId)
            val toAcc = accountRepository.getAccountById(toAccountId)
            val transferTitle = "Transfer: ${fromAcc?.name ?: "Account"} ➔ ${toAcc?.name ?: "Account"}"

            val transfer = ExpenseEntity(
                title = transferTitle,
                amount = amount,
                categoryId = 10,
                eventId = null,
                accountId = fromAccountId,
                toAccountId = toAccountId,
                transactionType = TransactionType.TRANSFER,
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                note = note.trim(),
                timestamp = timestamp
            )
            expenseRepository.insertExpense(transfer)
            accountRepository.transferFunds(fromAccountId, toAccountId, amount)
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expense)
            when (expense.transactionType) {
                TransactionType.EXPENSE -> {
                    if (expense.accountId != null) {
                        accountRepository.creditAccount(expense.accountId, expense.amount)
                    }
                }
                TransactionType.INCOME -> {
                    if (expense.accountId != null) {
                        accountRepository.debitAccount(expense.accountId, expense.amount)
                    }
                }
                TransactionType.TRANSFER -> {
                    if (expense.accountId != null && expense.toAccountId != null) {
                        accountRepository.transferFunds(expense.toAccountId, expense.accountId, expense.amount)
                    }
                }
            }
        }
    }

    companion object {
        fun provideFactory(
            expenseRepository: ExpenseRepository,
            categoryRepository: CategoryRepository,
            eventRepository: EventRepository,
            accountRepository: AccountRepository,
            userPreferences: UserPreferences
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ExpenseViewModel(
                    expenseRepository,
                    categoryRepository,
                    eventRepository,
                    accountRepository,
                    userPreferences
                ) as T
            }
        }
    }
}
