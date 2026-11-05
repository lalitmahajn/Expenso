package com.example.expenso.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expenso.data.model.CategorySpendSummary
import com.example.expenso.data.model.EventSpendSummary
import com.example.expenso.data.model.OverallStats
import com.example.expenso.data.model.PaymentMethodSummary
import com.example.expenso.data.preferences.UserPreferences
import com.example.expenso.data.repository.AnalyticsRepository
import com.example.expenso.data.repository.TimePeriodFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class AnalyticsUiState(
    val selectedPeriod: TimePeriodFilter = TimePeriodFilter.THIS_MONTH,
    val stats: OverallStats = OverallStats(),
    val categoryBreakdown: List<CategorySpendSummary> = emptyList(),
    val eventBreakdown: List<EventSpendSummary> = emptyList(),
    val paymentMethodBreakdown: List<PaymentMethodSummary> = emptyList(),
    val currency: String = "$",
    val isLoading: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsViewModel(
    private val analyticsRepository: AnalyticsRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(TimePeriodFilter.THIS_MONTH)
    val selectedPeriod = _selectedPeriod.asStateFlow()

    val uiState: StateFlow<AnalyticsUiState> = combine(
        _selectedPeriod,
        userPreferences.currency
    ) { period, currency ->
        Pair(period, currency)
    }.flatMapLatest { (period, currency) ->
        val range = analyticsRepository.getTimeRange(period)
        combine(
            analyticsRepository.getOverallStats(range.startTime, range.endTime),
            analyticsRepository.getCategoryBreakdown(range.startTime, range.endTime),
            analyticsRepository.getEventBreakdown(range.startTime, range.endTime),
            analyticsRepository.getPaymentMethodBreakdown(range.startTime, range.endTime)
        ) { stats, categories, events, payments ->
            AnalyticsUiState(
                selectedPeriod = period,
                stats = stats,
                categoryBreakdown = categories,
                eventBreakdown = events,
                paymentMethodBreakdown = payments,
                currency = currency,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsUiState(isLoading = true)
    )

    fun selectPeriod(period: TimePeriodFilter) {
        _selectedPeriod.value = period
    }

    companion object {
        fun provideFactory(
            analyticsRepository: AnalyticsRepository,
            userPreferences: UserPreferences
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AnalyticsViewModel(analyticsRepository, userPreferences) as T
            }
        }
    }
}
