package com.example.expenso.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expenso.data.model.AccountEntity
import com.example.expenso.data.model.CategoryEntity
import com.example.expenso.data.model.CategorySpendSummary
import com.example.expenso.data.model.EventEntity
import com.example.expenso.data.model.EventSortOption
import com.example.expenso.data.model.EventSpendSummary
import com.example.expenso.data.model.ExpenseEntity
import com.example.expenso.data.model.ExpenseWithDetails
import com.example.expenso.data.model.OverallStats
import com.example.expenso.data.model.PaymentMethod
import com.example.expenso.data.model.PaymentMethodSummary
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

data class EventsUiState(
    val summaries: List<EventSpendSummary> = emptyList(),
    val currency: String = "$",
    val selectedEventId: Long? = null,
    val selectedEventSummary: EventSpendSummary? = null,
    val selectedEventExpenses: List<ExpenseWithDetails> = emptyList(),
    val filteredExpenses: List<ExpenseWithDetails> = emptyList(),
    val categoryBreakdowns: List<CategorySpendSummary> = emptyList(),
    val paymentBreakdowns: List<PaymentMethodSummary> = emptyList(),
    val stats: OverallStats = OverallStats(),
    val allCategories: List<CategoryEntity> = emptyList(),
    val allAccounts: List<AccountEntity> = emptyList(),
    val searchQuery: String = "",
    val filterCategoryId: Long? = null,
    val filterPaymentMethod: PaymentMethod? = null,
    val sortOption: EventSortOption = EventSortOption.NEWEST,
    val showArchived: Boolean = false,
    val activeCount: Int = 0,
    val archivedCount: Int = 0,
    val isLoading: Boolean = false
)

class EventViewModel(
    private val eventRepository: EventRepository,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _selectedEventId = MutableStateFlow<Long?>(null)
    val selectedEventId = _selectedEventId.asStateFlow()

    private val _showArchived = MutableStateFlow(false)
    val showArchived = _showArchived.asStateFlow()

    private val _detailSearchQuery = MutableStateFlow("")
    private val _detailCategoryId = MutableStateFlow<Long?>(null)
    private val _detailPaymentMethod = MutableStateFlow<PaymentMethod?>(null)
    private val _detailSortOption = MutableStateFlow(EventSortOption.NEWEST)

    private data class DetailFilterParams(
        val selectedId: Long?,
        val query: String,
        val categoryId: Long?,
        val paymentMethod: PaymentMethod?,
        val sortOption: EventSortOption,
        val showArchived: Boolean
    )

    private data class DetailSearchFilter(
        val selectedId: Long?,
        val query: String,
        val categoryId: Long?,
        val paymentMethod: PaymentMethod?
    )

    private val _detailSearchFilter = combine(
        _selectedEventId,
        _detailSearchQuery,
        _detailCategoryId,
        _detailPaymentMethod
    ) { id, query, catId, pm ->
        DetailSearchFilter(id, query, catId, pm)
    }

    private val detailFilters = combine(
        _detailSearchFilter,
        _detailSortOption,
        _showArchived
    ) { sf, sort, archived ->
        DetailFilterParams(sf.selectedId, sf.query, sf.categoryId, sf.paymentMethod, sort, archived)
    }

    private data class MetaAndCurrency(
        val categories: List<CategoryEntity>,
        val accounts: List<AccountEntity>,
        val currency: String
    )

    private val metaAndCurrency = combine(
        categoryRepository.allCategories,
        accountRepository.allAccounts,
        userPreferences.currency
    ) { cats, accs, curr ->
        MetaAndCurrency(cats, accs, curr)
    }

    val uiState: StateFlow<EventsUiState> = combine(
        eventRepository.getAllEventSummaries(),
        expenseRepository.allExpenses,
        metaAndCurrency,
        detailFilters
    ) { summaries, allExpenses, meta, filters ->
        val selectedId = filters.selectedId
        val selectedSummary = summaries.find { it.event.id == selectedId }
        val eventExpenses = if (selectedId != null) {
            allExpenses.filter { it.expense.eventId == selectedId }
        } else {
            emptyList()
        }

        val activeSummaries = summaries.filter { !it.event.isArchived }
        val archivedSummaries = summaries.filter { it.event.isArchived }
        val displayedSummaries = if (filters.showArchived) archivedSummaries else activeSummaries

        // Category Breakdown within this event
        val categoryBreakdowns = eventExpenses
            .filter { it.category != null }
            .groupBy { it.category!! }
            .map { (cat, items) ->
                CategorySpendSummary(
                    categoryId = cat.id,
                    categoryName = cat.name,
                    colorHex = cat.colorHex,
                    iconName = cat.iconName,
                    totalAmount = items.sumOf { it.expense.amount },
                    count = items.size
                )
            }
            .sortedByDescending { it.totalAmount }

        // Payment Method Breakdown within this event
        val paymentBreakdowns = eventExpenses
            .groupBy { it.expense.paymentMethod }
            .map { (method, items) ->
                PaymentMethodSummary(
                    paymentMethod = method,
                    totalAmount = items.sumOf { it.expense.amount },
                    count = items.size
                )
            }
            .sortedByDescending { it.totalAmount }

        // Stats
        val total = eventExpenses.sumOf { it.expense.amount }
        val count = eventExpenses.size
        val avg = if (count > 0) total / count else 0.0
        val highest = eventExpenses.maxOfOrNull { it.expense.amount } ?: 0.0
        val stats = OverallStats(
            totalSpent = total,
            transactionCount = count,
            averageTransaction = avg,
            highestExpense = highest
        )

        // Filter & Sort Transactions
        var filtered = eventExpenses.filter { item ->
            val matchesQuery = filters.query.isBlank() ||
                item.expense.title.contains(filters.query, ignoreCase = true) ||
                (item.category?.name?.contains(filters.query, ignoreCase = true) == true) ||
                item.expense.note.contains(filters.query, ignoreCase = true)

            val matchesCat = filters.categoryId == null || item.expense.categoryId == filters.categoryId
            val matchesPm = filters.paymentMethod == null || item.expense.paymentMethod == filters.paymentMethod

            matchesQuery && matchesCat && matchesPm
        }

        filtered = when (filters.sortOption) {
            EventSortOption.NEWEST -> filtered.sortedByDescending { it.expense.timestamp }
            EventSortOption.OLDEST -> filtered.sortedBy { it.expense.timestamp }
            EventSortOption.HIGHEST_AMOUNT -> filtered.sortedByDescending { it.expense.amount }
            EventSortOption.LOWEST_AMOUNT -> filtered.sortedBy { it.expense.amount }
        }

        EventsUiState(
            summaries = displayedSummaries,
            currency = meta.currency,
            selectedEventId = selectedId,
            selectedEventSummary = selectedSummary,
            selectedEventExpenses = eventExpenses,
            filteredExpenses = filtered,
            categoryBreakdowns = categoryBreakdowns,
            paymentBreakdowns = paymentBreakdowns,
            stats = stats,
            allCategories = meta.categories,
            allAccounts = meta.accounts,
            searchQuery = filters.query,
            filterCategoryId = filters.categoryId,
            filterPaymentMethod = filters.paymentMethod,
            sortOption = filters.sortOption,
            showArchived = filters.showArchived,
            activeCount = activeSummaries.size,
            archivedCount = archivedSummaries.size,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EventsUiState(isLoading = true)
    )

    fun selectEvent(eventId: Long?) {
        _selectedEventId.value = eventId
        if (eventId == null) {
            clearDetailFilters()
        }
    }

    fun setShowArchived(show: Boolean) {
        _showArchived.value = show
    }

    fun toggleArchiveEvent(event: EventEntity) {
        viewModelScope.launch {
            eventRepository.setEventArchived(event.id, !event.isArchived)
        }
    }

    fun setDetailSearchQuery(query: String) {
        _detailSearchQuery.value = query
    }

    fun setDetailCategory(categoryId: Long?) {
        _detailCategoryId.value = if (_detailCategoryId.value == categoryId) null else categoryId
    }

    fun setDetailPaymentMethod(method: PaymentMethod?) {
        _detailPaymentMethod.value = if (_detailPaymentMethod.value == method) null else method
    }

    fun setDetailSortOption(option: EventSortOption) {
        _detailSortOption.value = option
    }

    fun clearDetailFilters() {
        _detailSearchQuery.value = ""
        _detailCategoryId.value = null
        _detailPaymentMethod.value = null
        _detailSortOption.value = EventSortOption.NEWEST
    }

    fun createEvent(
        name: String,
        description: String = "",
        targetBudget: Double? = null
    ) {
        viewModelScope.launch {
            val event = EventEntity(
                name = name.trim(),
                description = description.trim(),
                targetBudget = targetBudget
            )
            eventRepository.insertEvent(event)
        }
    }

    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch {
            if (_selectedEventId.value == event.id) {
                _selectedEventId.value = null
            }
            eventRepository.deleteEvent(event)
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expense)
            if (expense.accountId != null) {
                accountRepository.creditAccount(expense.accountId, expense.amount)
            }
        }
    }

    fun addExpenseToEvent(
        title: String,
        amount: Double,
        categoryId: Long,
        paymentMethod: PaymentMethod,
        note: String = "",
        accountId: Long? = null,
        timestamp: Long = System.currentTimeMillis()
    ) {
        val eventId = _selectedEventId.value ?: return
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

    fun updateEventExpense(
        original: ExpenseEntity,
        title: String,
        amount: Double,
        categoryId: Long,
        paymentMethod: PaymentMethod,
        note: String = "",
        accountId: Long? = null,
        timestamp: Long = original.timestamp
    ) {
        viewModelScope.launch {
            // Refund original account if was debited
            if (original.accountId != null) {
                accountRepository.creditAccount(original.accountId, original.amount)
            }
            // Debit new account if selected
            if (accountId != null) {
                accountRepository.debitAccount(accountId, amount)
            }
            val updated = original.copy(
                title = title.trim(),
                amount = amount,
                categoryId = categoryId,
                accountId = accountId,
                paymentMethod = paymentMethod,
                note = note.trim(),
                timestamp = timestamp
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

    companion object {
        fun provideFactory(
            eventRepository: EventRepository,
            expenseRepository: ExpenseRepository,
            categoryRepository: CategoryRepository,
            accountRepository: AccountRepository,
            userPreferences: UserPreferences
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return EventViewModel(
                    eventRepository,
                    expenseRepository,
                    categoryRepository,
                    accountRepository,
                    userPreferences
                ) as T
            }
        }
    }
}
