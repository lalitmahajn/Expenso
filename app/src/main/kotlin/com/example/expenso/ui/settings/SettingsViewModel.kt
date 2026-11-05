package com.example.expenso.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expenso.data.model.CategoryEntity
import com.example.expenso.data.model.EventEntity
import com.example.expenso.data.model.ExpenseEntity
import com.example.expenso.data.model.PaymentMethod
import com.example.expenso.data.preferences.ThemeMode
import com.example.expenso.data.preferences.UserPreferences
import com.example.expenso.data.repository.CategoryRepository
import com.example.expenso.data.repository.EventRepository
import com.example.expenso.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val expenseRepository: ExpenseRepository,
    private val eventRepository: EventRepository,
    private val categoryRepository: CategoryRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val currency: StateFlow<String> = userPreferences.currency.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "$"
    )

    val themeMode: StateFlow<ThemeMode> = userPreferences.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemeMode.SYSTEM
    )

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.allCategories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addCategory(name: String, iconName: String, colorHex: String) {
        viewModelScope.launch {
            categoryRepository.insertCategory(
                CategoryEntity(name = name.trim(), iconName = iconName, colorHex = colorHex)
            )
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(category)
        }
    }

    fun setCurrency(symbol: String) {
        userPreferences.setCurrency(symbol)
    }

    fun setThemeMode(mode: ThemeMode) {
        userPreferences.setThemeMode(mode)
    }

    fun exportToCsv(context: Context, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val expenses = expenseRepository.getAllForExport()
            val success = CsvExportHelper.exportExpensesToCsv(context, expenses)
            onResult(success)
        }
    }

    fun seedSampleData(onComplete: () -> Unit) {
        viewModelScope.launch {
            val event1Id = eventRepository.insertEvent(
                EventEntity(
                    name = "Goa Road Trip",
                    description = "Weekend trip with college friends",
                    targetBudget = 800.0
                )
            )

            val event2Id = eventRepository.insertEvent(
                EventEntity(
                    name = "Sister's Wedding",
                    description = "Wedding gifts, outfits & travel",
                    targetBudget = 2500.0
                )
            )

            val event3Id = eventRepository.insertEvent(
                EventEntity(
                    name = "Apartment Setup",
                    description = "Decor, tools and furniture",
                    targetBudget = null // pure grouping tag mode
                )
            )

            val now = System.currentTimeMillis()
            val oneDay = 86_400_000L

            val sampleExpenses = listOf(
                ExpenseEntity(title = "Beach Resort Booking", amount = 350.0, categoryId = 8, eventId = event1Id, timestamp = now - oneDay * 2, paymentMethod = PaymentMethod.CARD),
                ExpenseEntity(title = "Fuel & Highway Tolls", amount = 95.0, categoryId = 3, eventId = event1Id, timestamp = now - oneDay * 2, paymentMethod = PaymentMethod.CARD),
                ExpenseEntity(title = "Seafood Shack Dinner", amount = 78.50, categoryId = 1, eventId = event1Id, timestamp = now - oneDay, paymentMethod = PaymentMethod.UPI),
                ExpenseEntity(title = "Water Sports & Scuba", amount = 140.0, categoryId = 6, eventId = event1Id, timestamp = now - oneDay, paymentMethod = PaymentMethod.CASH),

                ExpenseEntity(title = "Traditional Wedding Attire", amount = 650.0, categoryId = 5, eventId = event2Id, timestamp = now - oneDay * 5, paymentMethod = PaymentMethod.CARD),
                ExpenseEntity(title = "Jewelry & Gold Gift", amount = 950.0, categoryId = 5, eventId = event2Id, timestamp = now - oneDay * 3, paymentMethod = PaymentMethod.BANK_TRANSFER),
                ExpenseEntity(title = "Flight Tickets for Family", amount = 480.0, categoryId = 8, eventId = event2Id, timestamp = now - oneDay * 4, paymentMethod = PaymentMethod.CARD),

                ExpenseEntity(title = "Ergonomic Work Desk", amount = 220.0, categoryId = 5, eventId = event3Id, timestamp = now - oneDay * 7, paymentMethod = PaymentMethod.CARD),
                ExpenseEntity(title = "Curtains & Soft Lighting", amount = 85.0, categoryId = 5, eventId = event3Id, timestamp = now - oneDay * 6, paymentMethod = PaymentMethod.UPI),

                // Standalone Daily Expenses
                ExpenseEntity(title = "Whole Foods Supermarket", amount = 64.20, categoryId = 2, eventId = null, timestamp = now - 3600_000L * 4, paymentMethod = PaymentMethod.CARD),
                ExpenseEntity(title = "Coffee & Bakery", amount = 12.50, categoryId = 1, eventId = null, timestamp = now - 3600_000L * 2, paymentMethod = PaymentMethod.UPI),
                ExpenseEntity(title = "Metro Transit Pass", amount = 45.0, categoryId = 3, eventId = null, timestamp = now - oneDay * 1, paymentMethod = PaymentMethod.CARD),
                ExpenseEntity(title = "High-speed Internet Bill", amount = 60.0, categoryId = 4, eventId = null, timestamp = now - oneDay * 3, paymentMethod = PaymentMethod.UPI),
                ExpenseEntity(title = "Prescription Medicines", amount = 32.0, categoryId = 7, eventId = null, timestamp = now - oneDay * 4, paymentMethod = PaymentMethod.CASH),
                ExpenseEntity(title = "Cinema IMAX Tickets", amount = 36.0, categoryId = 6, eventId = null, timestamp = now - oneDay * 5, paymentMethod = PaymentMethod.UPI)
            )

            expenseRepository.insertAll(sampleExpenses)
            onComplete()
        }
    }

    companion object {
        fun provideFactory(
            expenseRepository: ExpenseRepository,
            eventRepository: EventRepository,
            categoryRepository: CategoryRepository,
            userPreferences: UserPreferences
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(
                    expenseRepository,
                    eventRepository,
                    categoryRepository,
                    userPreferences
                ) as T
            }
        }
    }
}
