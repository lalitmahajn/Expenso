package com.example.expenso.di

import android.content.Context
import com.example.expenso.data.local.AppDatabase
import com.example.expenso.data.repository.AccountRepository
import com.example.expenso.data.repository.AnalyticsRepository
import com.example.expenso.data.repository.CategoryRepository
import com.example.expenso.data.repository.EventRepository
import com.example.expenso.data.repository.ExpenseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

interface AppContainer {
    val database: AppDatabase
    val expenseRepository: ExpenseRepository
    val eventRepository: EventRepository
    val categoryRepository: CategoryRepository
    val accountRepository: AccountRepository
    val analyticsRepository: AnalyticsRepository
    val userPreferences: com.example.expenso.data.preferences.UserPreferences
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val userPreferences: com.example.expenso.data.preferences.UserPreferences by lazy {
        com.example.expenso.data.preferences.UserPreferences(context)
    }

    override val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context, applicationScope)
    }

    override val expenseRepository: ExpenseRepository by lazy {
        ExpenseRepository(database.expenseDao())
    }

    override val eventRepository: EventRepository by lazy {
        EventRepository(database.eventDao(), database.expenseDao())
    }

    override val categoryRepository: CategoryRepository by lazy {
        CategoryRepository(database.categoryDao())
    }

    override val accountRepository: AccountRepository by lazy {
        AccountRepository(database.accountDao())
    }

    override val analyticsRepository: AnalyticsRepository by lazy {
        AnalyticsRepository(
            expenseDao = database.expenseDao(),
            categoryDao = database.categoryDao(),
            eventDao = database.eventDao()
        )
    }

    init {
        applicationScope.launch {
            categoryRepository.ensureDefaultCategories()
        }
    }
}
