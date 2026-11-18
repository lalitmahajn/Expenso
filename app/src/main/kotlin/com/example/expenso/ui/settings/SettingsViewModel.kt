package com.example.expenso.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expenso.data.backup.BackupMetadata
import com.example.expenso.data.backup.BackupRestoreManager
import com.example.expenso.data.backup.BackupResult
import com.example.expenso.data.backup.RestorePreviewResult
import com.example.expenso.data.backup.RestoreResult
import com.example.expenso.data.model.CategoryEntity
import com.example.expenso.data.model.EventEntity
import com.example.expenso.data.model.ExpenseEntity
import com.example.expenso.data.model.PaymentMethod
import com.example.expenso.data.preferences.ThemeMode
import com.example.expenso.data.preferences.UserPreferences
import com.example.expenso.data.repository.AccountRepository
import com.example.expenso.data.repository.CategoryRepository
import com.example.expenso.data.repository.EventRepository
import com.example.expenso.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class SettingsViewModel(
    private val expenseRepository: ExpenseRepository,
    private val eventRepository: EventRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val userPreferences: UserPreferences,
    private val backupRestoreManager: BackupRestoreManager
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
                    description = "Wedding gifts, outfits & functions",
                    targetBudget = 5000.0
                )
            )

            val haldiId = eventRepository.insertEvent(
                EventEntity(
                    name = "Haldi",
                    targetBudget = 400.0,
                    parentEventId = event2Id,
                    colorHex = "#FF9800"
                )
            )

            val mehendiId = eventRepository.insertEvent(
                EventEntity(
                    name = "Mehendi",
                    targetBudget = 500.0,
                    parentEventId = event2Id,
                    colorHex = "#4CAF50"
                )
            )

            val sangeetId = eventRepository.insertEvent(
                EventEntity(
                    name = "Sangeet",
                    targetBudget = 800.0,
                    parentEventId = event2Id,
                    colorHex = "#E91E63"
                )
            )

            val receptionId = eventRepository.insertEvent(
                EventEntity(
                    name = "Reception",
                    targetBudget = 1200.0,
                    parentEventId = event2Id,
                    colorHex = "#9C27B0"
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

                // Sister's Wedding - General & Ceremonies
                ExpenseEntity(title = "Traditional Wedding Attire", amount = 650.0, categoryId = 5, eventId = event2Id, timestamp = now - oneDay * 6, paymentMethod = PaymentMethod.CARD),
                ExpenseEntity(title = "Jewelry & Gold Gift", amount = 950.0, categoryId = 5, eventId = event2Id, timestamp = now - oneDay * 5, paymentMethod = PaymentMethod.BANK_TRANSFER),
                ExpenseEntity(title = "Haldi Yellow Outfits & Flowers", amount = 220.0, categoryId = 5, eventId = haldiId, timestamp = now - oneDay * 4, paymentMethod = PaymentMethod.UPI),
                ExpenseEntity(title = "Bridal Mehendi Artist & Cones", amount = 310.0, categoryId = 5, eventId = mehendiId, timestamp = now - oneDay * 3, paymentMethod = PaymentMethod.UPI),
                ExpenseEntity(title = "Sangeet DJ, Lighting & Sound", amount = 650.0, categoryId = 6, eventId = sangeetId, timestamp = now - oneDay * 2, paymentMethod = PaymentMethod.BANK_TRANSFER),
                ExpenseEntity(title = "Reception Banquet Catering", amount = 850.0, categoryId = 1, eventId = receptionId, timestamp = now - oneDay * 1, paymentMethod = PaymentMethod.CARD),

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

    fun clearDemoData(resetAccounts: Boolean = true, onComplete: () -> Unit) {
        viewModelScope.launch {
            expenseRepository.deleteAllExpenses()
            eventRepository.deleteAllEvents()
            if (resetAccounts) {
                accountRepository.resetToDefaultAccounts()
            }
            onComplete()
        }
    }

    fun exportBackup(context: Context, uri: Uri, onResult: (BackupResult) -> Unit) {
        viewModelScope.launch {
            val result = backupRestoreManager.exportBackupToUri(context, uri)
            onResult(result)
        }
    }

    fun shareBackup(context: Context, onResult: (File?) -> Unit) {
        viewModelScope.launch {
            val file = backupRestoreManager.createShareableBackupFile(context)
            onResult(file)
        }
    }

    fun inspectBackup(context: Context, uri: Uri, onResult: (RestorePreviewResult) -> Unit) {
        viewModelScope.launch {
            val preview = backupRestoreManager.inspectBackupUri(context, uri)
            onResult(preview)
        }
    }

    fun restoreBackup(rawJson: String, onResult: (RestoreResult) -> Unit) {
        viewModelScope.launch {
            val result = backupRestoreManager.restoreFromRawJson(rawJson)
            onResult(result)
        }
    }

    companion object {
        fun provideFactory(
            expenseRepository: ExpenseRepository,
            eventRepository: EventRepository,
            categoryRepository: CategoryRepository,
            accountRepository: AccountRepository,
            userPreferences: UserPreferences,
            backupRestoreManager: BackupRestoreManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(
                    expenseRepository,
                    eventRepository,
                    categoryRepository,
                    accountRepository,
                    userPreferences,
                    backupRestoreManager
                ) as T
            }
        }
    }
}
