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
import com.example.expenso.data.model.TransactionType
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
    val selectedParentEvent: EventEntity? = null,
    val selectedEventSummary: EventSpendSummary? = null,
    val selectedEventExpenses: List<ExpenseWithDetails> = emptyList(),
    val filteredExpenses: List<ExpenseWithDetails> = emptyList(),
    val categoryBreakdowns: List<CategorySpendSummary> = emptyList(),
    val paymentBreakdowns: List<PaymentMethodSummary> = emptyList(),
    val stats: OverallStats = OverallStats(),
    val allCategories: List<CategoryEntity> = emptyList(),
    val allAccounts: List<AccountEntity> = emptyList(),
    val allEvents: List<EventEntity> = emptyList(),
    val searchQuery: String = "",
    val filterCategoryId: Long? = null,
    val filterPaymentMethod: PaymentMethod? = null,
    val filterSubEventId: Long? = null,
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
    private val _detailSubEventId = MutableStateFlow<Long?>(null)
    private val _detailSortOption = MutableStateFlow(EventSortOption.NEWEST)

    private data class DetailFilterParams(
        val selectedId: Long?,
        val query: String,
        val categoryId: Long?,
        val paymentMethod: PaymentMethod?,
        val subEventId: Long?,
        val sortOption: EventSortOption,
        val showArchived: Boolean
    )

    private data class DetailSearchFilter(
        val selectedId: Long?,
        val query: String,
        val categoryId: Long?,
        val paymentMethod: PaymentMethod?,
        val subEventId: Long?
    )

    private val _detailSearchFilter = combine(
        _selectedEventId,
        _detailSearchQuery,
        _detailCategoryId,
        _detailPaymentMethod,
        _detailSubEventId
    ) { id, query, catId, pm, subId ->
        DetailSearchFilter(id, query, catId, pm, subId)
    }

    private val detailFilters = combine(
        _detailSearchFilter,
        _detailSortOption,
        _showArchived
    ) { sf, sort, archived ->
        DetailFilterParams(sf.selectedId, sf.query, sf.categoryId, sf.paymentMethod, sf.subEventId, sort, archived)
    }

    private data class MetaAndCurrency(
        val categories: List<CategoryEntity>,
        val accounts: List<AccountEntity>,
        val allEvents: List<EventEntity>,
        val currency: String
    )

    private val metaAndCurrency = combine(
        categoryRepository.allCategories,
        accountRepository.allAccounts,
        eventRepository.allEvents,
        userPreferences.currency
    ) { cats, accs, evts, curr ->
        MetaAndCurrency(cats, accs, evts, curr)
    }

    val uiState: StateFlow<EventsUiState> = combine(
        eventRepository.getAllEventSummaries(),
        expenseRepository.allExpenses,
        metaAndCurrency,
        detailFilters
    ) { summaries, allExpenses, meta, filters ->
        val selectedId = filters.selectedId
        val selectedSummary = summaries.find { it.event.id == selectedId }
            ?: summaries.flatMap { it.subEventSummaries }.find { it.event.id == selectedId }
        val childIds = selectedSummary?.subEventSummaries?.map { it.event.id }?.toSet() ?: emptySet()

        val eventExpenses = if (selectedId != null) {
            allExpenses.filter { it.expense.eventId == selectedId || it.expense.eventId in childIds }
        } else {
            emptyList()
        }

        val selectedParentEvent = if (selectedSummary?.event?.parentEventId != null) {
            meta.allEvents.find { it.id == selectedSummary.event.parentEventId }
        } else {
            null
        }

        val activeSummaries = summaries.filter { !it.event.isArchived }
        val archivedSummaries = summaries.filter { it.event.isArchived }
        val displayedSummaries = if (filters.showArchived) archivedSummaries else activeSummaries

        // Category Breakdown within this event (and its sub-events)
        val expenseItemsOnly = eventExpenses.filter { it.expense.transactionType == TransactionType.EXPENSE }
        val categoryBreakdowns = expenseItemsOnly
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
        val paymentBreakdowns = expenseItemsOnly
            .groupBy { it.expense.paymentMethod }
            .map { (method, items) ->
                PaymentMethodSummary(
                    paymentMethod = method,
                    totalAmount = items.sumOf { it.expense.amount },
                    count = items.size
                )
            }
            .sortedByDescending { it.totalAmount }

        // Stats (rolled-up spend)
        val total = expenseItemsOnly.sumOf { it.expense.amount }
        val count = expenseItemsOnly.size
        val avg = if (count > 0) total / count else 0.0
        val highest = expenseItemsOnly.maxOfOrNull { it.expense.amount } ?: 0.0
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

            val matchesSub = when (filters.subEventId) {
                null -> true
                -1L -> item.expense.eventId == selectedId // Direct parent event only
                else -> item.expense.eventId == filters.subEventId // Specific sub-event
            }

            matchesQuery && matchesCat && matchesPm && matchesSub
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
            selectedParentEvent = selectedParentEvent,
            selectedEventSummary = selectedSummary,
            selectedEventExpenses = eventExpenses,
            filteredExpenses = filtered,
            categoryBreakdowns = categoryBreakdowns,
            paymentBreakdowns = paymentBreakdowns,
            stats = stats,
            allCategories = meta.categories,
            allAccounts = meta.accounts,
            allEvents = meta.allEvents,
            searchQuery = filters.query,
            filterCategoryId = filters.categoryId,
            filterPaymentMethod = filters.paymentMethod,
            filterSubEventId = filters.subEventId,
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
        _detailSubEventId.value = null
        clearDetailFilters()
    }

    fun navigateBack() {
        val currentEvent = uiState.value.selectedEventSummary?.event
        if (currentEvent?.parentEventId != null) {
            selectEvent(currentEvent.parentEventId)
        } else {
            selectEvent(null)
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

    fun setDetailSubEvent(subEventId: Long?) {
        _detailSubEventId.value = if (_detailSubEventId.value == subEventId) null else subEventId
    }

    fun setDetailSortOption(option: EventSortOption) {
        _detailSortOption.value = option
    }

    fun clearDetailFilters() {
        _detailSearchQuery.value = ""
        _detailCategoryId.value = null
        _detailPaymentMethod.value = null
        _detailSubEventId.value = null
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

    fun createSubEvent(
        parentEventId: Long,
        name: String,
        targetBudget: Double? = null,
        startDate: Long = System.currentTimeMillis(),
        colorHex: String? = null
    ) {
        viewModelScope.launch {
            val subEvent = EventEntity(
                name = name.trim(),
                description = "",
                targetBudget = targetBudget,
                startDate = startDate,
                parentEventId = parentEventId,
                colorHex = colorHex
            )
            eventRepository.insertEvent(subEvent)
        }
    }

    // Option B: Deleting an event deletes all child sub-events AND deletes all expenses
    // associated with the parent and child sub-events, reverting account balance debits/credits.
    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch {
            if (_selectedEventId.value == event.id) {
                if (event.parentEventId != null) {
                    _selectedEventId.value = event.parentEventId
                } else {
                    _selectedEventId.value = null
                }
            }
            val expenses = expenseRepository.getExpensesForEventAndChildrenSync(event.id)
            for (item in expenses) {
                val exp = item.expense
                if (exp.accountId != null) {
                    when (exp.transactionType) {
                        TransactionType.EXPENSE -> accountRepository.creditAccount(exp.accountId, exp.amount)
                        TransactionType.INCOME -> accountRepository.debitAccount(exp.accountId, exp.amount)
                        TransactionType.TRANSFER -> {
                            accountRepository.creditAccount(exp.accountId, exp.amount)
                            if (exp.toAccountId != null) {
                                accountRepository.debitAccount(exp.toAccountId, exp.amount)
                            }
                        }
                    }
                }
            }
            expenseRepository.deleteExpensesForEventAndChildren(event.id)
            eventRepository.deleteEventAndChildren(event.id)
        }
    }

    fun updateEvent(
        event: EventEntity,
        name: String,
        targetBudget: Double?,
        description: String = event.description,
        colorHex: String? = event.colorHex
    ) {
        viewModelScope.launch {
            eventRepository.updateEvent(
                event.copy(
                    name = name.trim(),
                    targetBudget = targetBudget,
                    description = description.trim(),
                    colorHex = colorHex
                )
            )
        }
    }

    // Option B for sub-event: Delete sub-event, delete sub-event expenses, revert account balances.
    fun deleteSubEvent(subEvent: EventEntity) {
        viewModelScope.launch {
            val expenses = expenseRepository.getExpensesByEventSync(subEvent.id)
            for (item in expenses) {
                val exp = item.expense
                if (exp.accountId != null) {
                    when (exp.transactionType) {
                        TransactionType.EXPENSE -> accountRepository.creditAccount(exp.accountId, exp.amount)
                        TransactionType.INCOME -> accountRepository.debitAccount(exp.accountId, exp.amount)
                        TransactionType.TRANSFER -> {
                            accountRepository.creditAccount(exp.accountId, exp.amount)
                            if (exp.toAccountId != null) {
                                accountRepository.debitAccount(exp.toAccountId, exp.amount)
                            }
                        }
                    }
                }
            }
            expenseRepository.deleteExpensesByEvent(subEvent.id)
            eventRepository.deleteEvent(subEvent)
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expense)
            if (expense.accountId != null) {
                when (expense.transactionType) {
                    TransactionType.EXPENSE -> accountRepository.creditAccount(expense.accountId, expense.amount)
                    TransactionType.INCOME -> accountRepository.debitAccount(expense.accountId, expense.amount)
                    TransactionType.TRANSFER -> {
                        accountRepository.creditAccount(expense.accountId, expense.amount)
                        if (expense.toAccountId != null) {
                            accountRepository.debitAccount(expense.toAccountId, expense.amount)
                        }
                    }
                }
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
        timestamp: Long = System.currentTimeMillis(),
        eventId: Long? = null
    ) {
        val targetEventId = eventId ?: _selectedEventId.value ?: return
        viewModelScope.launch {
            val expense = ExpenseEntity(
                title = title.trim(),
                amount = amount,
                categoryId = categoryId,
                eventId = targetEventId,
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
        timestamp: Long = original.timestamp,
        eventId: Long? = null
    ) {
        viewModelScope.launch {
            if (original.accountId != null) {
                when (original.transactionType) {
                    TransactionType.EXPENSE -> accountRepository.creditAccount(original.accountId, original.amount)
                    TransactionType.INCOME -> accountRepository.debitAccount(original.accountId, original.amount)
                    TransactionType.TRANSFER -> {
                        accountRepository.creditAccount(original.accountId, original.amount)
                        if (original.toAccountId != null) {
                            accountRepository.debitAccount(original.toAccountId, original.amount)
                        }
                    }
                }
            }
            if (accountId != null) {
                accountRepository.debitAccount(accountId, amount)
            }
            val targetEventId = eventId
            val updated = original.copy(
                title = title.trim(),
                amount = amount,
                categoryId = categoryId,
                eventId = targetEventId,
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
