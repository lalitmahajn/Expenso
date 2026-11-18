package com.example.expenso.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
            container.accountRepository,
            container.userPreferences,
            container.backupRestoreManager
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

    val eventUiState by eventViewModel.uiState.collectAsStateWithLifecycle()
    val isEventDetailOpen = eventUiState.selectedEventId != null

    BackHandler(enabled = isEventDetailOpen) {
        eventViewModel.navigateBack()
    }

    BackHandler(enabled = !isEventDetailOpen && isStatementOpen) {
        isStatementOpen = false
    }

    BackHandler(enabled = !isEventDetailOpen && !isStatementOpen && currentScreen != BottomNavScreen.Expenses) {
        currentScreen = BottomNavScreen.Expenses
    }

    if (isEventDetailOpen) {
        // Open Dedicated Full-Screen for Event Detail & Analysis
        EventDetailScreen(
            viewModel = eventViewModel,
            onBack = { eventViewModel.navigateBack() }
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
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    shadowElevation = 10.dp
                ) {
                    Column {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp
                        ) {
                            BottomNavScreen.items.forEach { screen ->
                                val isSelected = currentScreen.route == screen.route
                                val iconScale by animateFloatAsState(
                                    targetValue = if (isSelected) 1.08f else 1.0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    ),
                                    label = "nav_icon_scale_${screen.route}"
                                )
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentScreen = screen },
                                    icon = {
                                        Icon(
                                            imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                            contentDescription = screen.title,
                                            modifier = Modifier
                                                .size(23.dp)
                                                .scale(iconScale)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = screen.title,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.5.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                letterSpacing = 0.15.sp
                                            ),
                                            maxLines = 1
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                )
                            }
                        }
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
