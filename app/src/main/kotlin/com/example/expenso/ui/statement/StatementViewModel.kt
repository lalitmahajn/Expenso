package com.example.expenso.ui.statement

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expenso.data.model.AccountEntity
import com.example.expenso.data.model.CategoryEntity
import com.example.expenso.data.model.EventEntity
import com.example.expenso.data.model.ExpenseWithDetails
import com.example.expenso.data.preferences.UserPreferences
import com.example.expenso.data.repository.AccountRepository
import com.example.expenso.data.repository.CategoryRepository
import com.example.expenso.data.repository.EventRepository
import com.example.expenso.data.repository.ExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class StatementViewModel(
    private val expenseRepository: ExpenseRepository,
    private val accountRepository: AccountRepository,
    private val eventRepository: EventRepository,
    private val categoryRepository: CategoryRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(StatementPeriod.THIS_MONTH)
    private val _customStartDate = MutableStateFlow<Long?>(null)
    private val _customEndDate = MutableStateFlow<Long?>(null)
    private val _selectedAccountId = MutableStateFlow<Long?>(null)
    private val _selectedEventId = MutableStateFlow<Long?>(null)
    private val _selectedCategoryIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _selectedFormat = MutableStateFlow(StatementFormat.PDF)
    private val _isGenerating = MutableStateFlow(false)
    private val _userMessage = MutableStateFlow<String?>(null)

    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private data class FilterConfig(
        val period: StatementPeriod,
        val customStart: Long?,
        val customEnd: Long?,
        val accountId: Long?,
        val eventId: Long?,
        val categoryIds: Set<Long>,
        val format: StatementFormat
    )

    private val filtersFlow = combine(
        _selectedPeriod,
        _customStartDate,
        _customEndDate,
        _selectedAccountId,
        _selectedEventId
    ) { period, customStart, customEnd, accountId, eventId ->
        FilterConfig(
            period = period,
            customStart = customStart,
            customEnd = customEnd,
            accountId = accountId,
            eventId = eventId,
            categoryIds = _selectedCategoryIds.value,
            format = _selectedFormat.value
        )
    }

    private data class MetaConfig(
        val accounts: List<AccountEntity>,
        val events: List<EventEntity>,
        val categories: List<CategoryEntity>,
        val currency: String
    )

    private val metaFlow = combine(
        accountRepository.allAccounts,
        eventRepository.allEvents,
        categoryRepository.allCategories,
        userPreferences.currency
    ) { accounts, events, categories, currency ->
        MetaConfig(accounts, events, categories, currency)
    }

    val uiState: StateFlow<StatementUiState> = combine(
        expenseRepository.allExpenses,
        filtersFlow,
        metaFlow,
        _selectedCategoryIds,
        _selectedFormat
    ) { allExpenses, filters, meta, catIds, format ->
        val (startTime, endTime) = computeTimeRange(filters.period, filters.customStart, filters.customEnd)

        val filtered = allExpenses.filter { item ->
            val timestamp = item.expense.timestamp
            val inTime = timestamp in startTime..endTime
            val matchAccount = filters.accountId == null || item.expense.accountId == filters.accountId
            val matchEvent = filters.eventId == null || item.expense.eventId == filters.eventId
            val matchCategory = catIds.isEmpty() || item.expense.categoryId in catIds
            inTime && matchAccount && matchEvent && matchCategory
        }

        val total = filtered.sumOf { it.expense.amount }

        StatementUiState(
            selectedPeriod = filters.period,
            customStartDate = filters.customStart,
            customEndDate = filters.customEnd,
            selectedAccountId = filters.accountId,
            selectedEventId = filters.eventId,
            selectedCategoryIds = catIds,
            selectedFormat = format,
            currency = meta.currency,
            accounts = meta.accounts,
            events = meta.events,
            categories = meta.categories,
            filteredExpenses = filtered,
            totalAmount = total,
            transactionCount = filtered.size,
            isGenerating = _isGenerating.value,
            userMessage = _userMessage.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatementUiState()
    )

    fun setPeriod(period: StatementPeriod) {
        _selectedPeriod.value = period
    }

    fun setCustomDateRange(start: Long, end: Long) {
        _selectedPeriod.value = StatementPeriod.CUSTOM
        _customStartDate.value = start
        _customEndDate.value = end
    }

    fun setSelectedAccount(accountId: Long?) {
        _selectedAccountId.value = accountId
    }

    fun setSelectedEvent(eventId: Long?) {
        _selectedEventId.value = eventId
    }

    fun toggleCategory(categoryId: Long) {
        val current = _selectedCategoryIds.value.toMutableSet()
        if (current.contains(categoryId)) {
            current.remove(categoryId)
        } else {
            current.add(categoryId)
        }
        _selectedCategoryIds.value = current
    }

    fun clearCategoryFilters() {
        _selectedCategoryIds.value = emptySet()
    }

    fun setFormat(format: StatementFormat) {
        _selectedFormat.value = format
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun generateAndShare(context: Context) {
        val state = uiState.value
        if (state.filteredExpenses.isEmpty()) {
            _userMessage.value = "No transactions found for the selected scope to export."
            return
        }

        viewModelScope.launch {
            _isGenerating.value = true
            val uri = generateStatementFile(context, state)
            _isGenerating.value = false

            if (uri != null) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = state.selectedFormat.mimeType
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Expenso Statement - ${state.selectedPeriod.displayName}")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(shareIntent, "Share Financial Statement via")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } else {
                _userMessage.value = "Failed to generate statement file."
            }
        }
    }

    fun generateAndView(context: Context) {
        val state = uiState.value
        if (state.filteredExpenses.isEmpty()) {
            _userMessage.value = "No transactions found for the selected scope to preview."
            return
        }

        viewModelScope.launch {
            _isGenerating.value = true
            val uri = generateStatementFile(context, state)
            _isGenerating.value = false

            if (uri != null) {
                try {
                    val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, state.selectedFormat.mimeType)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(viewIntent)
                } catch (e: Exception) {
                    // Fallback to chooser or share
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = state.selectedFormat.mimeType
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Open Statement via"))
                }
            } else {
                _userMessage.value = "Failed to create statement."
            }
        }
    }

    private suspend fun generateStatementFile(context: Context, state: StatementUiState): Uri? {
        return withContext(Dispatchers.IO) {
            val periodLabel = state.selectedPeriod.displayName
            val scopeParts = mutableListOf<String>()

            val selectedAcc = state.accounts.firstOrNull { it.id == state.selectedAccountId }
            if (selectedAcc != null) {
                scopeParts.add("Account: ${selectedAcc.name}")
            } else {
                scopeParts.add("All Accounts")
            }

            val selectedEvt = state.events.firstOrNull { it.id == state.selectedEventId }
            if (selectedEvt != null) {
                scopeParts.add("Event: ${selectedEvt.name}")
            }

            if (state.selectedCategoryIds.isNotEmpty()) {
                scopeParts.add("${state.selectedCategoryIds.size} Categories")
            }

            val scopeLabel = scopeParts.joinToString(" • ")

            when (state.selectedFormat) {
                StatementFormat.PDF -> {
                    StatementPdfHelper.generatePdf(
                        context = context,
                        expenses = state.filteredExpenses,
                        currency = state.currency,
                        periodLabel = periodLabel,
                        scopeLabel = scopeLabel,
                        totalAmount = state.totalAmount
                    )
                }
                StatementFormat.CSV -> {
                    StatementCsvHelper.generateCsv(
                        context = context,
                        expenses = state.filteredExpenses,
                        currency = state.currency,
                        periodLabel = periodLabel,
                        scopeLabel = scopeLabel,
                        totalAmount = state.totalAmount
                    )
                }
            }
        }
    }

    private fun computeTimeRange(period: StatementPeriod, customStart: Long?, customEnd: Long?): Pair<Long, Long> {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        return when (period) {
            StatementPeriod.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.add(Calendar.MONTH, 1)
                val end = cal.timeInMillis - 1
                start to end
            }
            StatementPeriod.LAST_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val end = cal.timeInMillis - 1
                cal.add(Calendar.MONTH, -1)
                val start = cal.timeInMillis
                start to end
            }
            StatementPeriod.LAST_30_DAYS -> (now - 30L * 86_400_000L) to now
            StatementPeriod.LAST_90_DAYS -> (now - 90L * 86_400_000L) to now
            StatementPeriod.THIS_YEAR -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.add(Calendar.YEAR, 1)
                val end = cal.timeInMillis - 1
                start to end
            }
            StatementPeriod.ALL_TIME -> 0L to Long.MAX_VALUE
            StatementPeriod.CUSTOM -> {
                val start = customStart ?: (now - 30L * 86_400_000L)
                val end = customEnd ?: now
                start to end
            }
        }
    }

    companion object {
        fun provideFactory(
            expenseRepository: ExpenseRepository,
            accountRepository: AccountRepository,
            eventRepository: EventRepository,
            categoryRepository: CategoryRepository,
            userPreferences: UserPreferences
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StatementViewModel(
                    expenseRepository,
                    accountRepository,
                    eventRepository,
                    categoryRepository,
                    userPreferences
                ) as T
            }
        }
    }
}
