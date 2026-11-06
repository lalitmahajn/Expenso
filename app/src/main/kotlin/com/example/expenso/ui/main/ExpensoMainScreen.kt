package com.example.expenso.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expenso.ExpensoApp
import com.example.expenso.ui.accounts.AccountViewModel
import com.example.expenso.ui.accounts.AccountsScreen
import com.example.expenso.ui.analytics.AnalyticsScreen
import com.example.expenso.ui.analytics.AnalyticsViewModel
import com.example.expenso.ui.events.EventDetailScreen
import com.example.expenso.ui.events.EventViewModel
import com.example.expenso.ui.events.EventsScreen
import com.example.expenso.ui.expenses.ExpenseViewModel
import com.example.expenso.ui.expenses.ExpensesScreen
import com.example.expenso.ui.navigation.BottomNavScreen
import com.example.expenso.ui.settings.SettingsScreen
import com.example.expenso.ui.settings.SettingsViewModel
import com.example.expenso.ui.statement.GenerateStatementScreen
import com.example.expenso.ui.statement.StatementViewModel

@Composable
fun ExpensoMainScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as ExpensoApp
    val container = app.container

    var currentScreen by remember { mutableStateOf<BottomNavScreen>(BottomNavScreen.Expenses) }

    val expenseViewModel: ExpenseViewModel = viewModel(
        factory = ExpenseViewModel.provideFactory(
            container.expenseRepository,
            container.categoryRepository,
            container.eventRepository,
            container.accountRepository,
            container.userPreferences
        )
    )

    val eventViewModel: EventViewModel = viewModel(
        factory = EventViewModel.provideFactory(
            container.eventRepository,
            container.expenseRepository,
            container.categoryRepository,
            container.accountRepository,
            container.userPreferences
        )
    )

    val accountViewModel: AccountViewModel = viewModel(
        factory = AccountViewModel.provideFactory(
            container.accountRepository,
            container.userPreferences
        )
    )

    val analyticsViewModel: AnalyticsViewModel = viewModel(
        factory = AnalyticsViewModel.provideFactory(
            container.analyticsRepository,
            container.userPreferences
        )
    )

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.provideFactory(
            container.expenseRepository,
            container.eventRepository,
            container.categoryRepository,
            container.userPreferences
        )
    )

    val statementViewModel: StatementViewModel = viewModel(
        factory = StatementViewModel.provideFactory(
            container.expenseRepository,
            container.accountRepository,
            container.eventRepository,
            container.categoryRepository,
            container.userPreferences
        )
    )

    var isStatementOpen by remember { mutableStateOf(false) }

    val eventUiState by eventViewModel.uiState.collectAsState()
    val isEventDetailOpen = eventUiState.selectedEventId != null

    if (isEventDetailOpen) {
        // Open Dedicated Full-Screen for Event Detail & Analysis
        EventDetailScreen(
            viewModel = eventViewModel,
            onBack = { eventViewModel.selectEvent(null) }
        )
    } else if (isStatementOpen) {
        // Open Dedicated Full-Screen for Generate Financial Statement
        GenerateStatementScreen(
            viewModel = statementViewModel,
            onBack = { isStatementOpen = false }
        )
    } else {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                NavigationBar {
                    BottomNavScreen.items.forEach { screen ->
                        val isSelected = currentScreen.route == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentScreen = screen },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) }
                        )
                    }
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = padding.calculateBottomPadding())
            ) {
                when (currentScreen) {
                    is BottomNavScreen.Expenses -> ExpensesScreen(viewModel = expenseViewModel)
                    is BottomNavScreen.Events -> EventsScreen(viewModel = eventViewModel)
                    is BottomNavScreen.Accounts -> AccountsScreen(viewModel = accountViewModel)
                    is BottomNavScreen.Analytics -> AnalyticsScreen(viewModel = analyticsViewModel)
                    is BottomNavScreen.Settings -> SettingsScreen(
                        viewModel = settingsViewModel,
                        onNavigateToStatement = { isStatementOpen = true }
                    )
                }
            }
        }
    }
}
