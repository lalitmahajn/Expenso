package com.example.expenso.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavScreen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Expenses : BottomNavScreen(
        route = "expenses",
        title = "Expenses",
        selectedIcon = Icons.AutoMirrored.Filled.ReceiptLong,
        unselectedIcon = Icons.AutoMirrored.Outlined.ReceiptLong
    )
    data object Events : BottomNavScreen(
        route = "events",
        title = "Events",
        selectedIcon = Icons.Default.Event,
        unselectedIcon = Icons.Outlined.Event
    )
    data object Accounts : BottomNavScreen(
        route = "accounts",
        title = "Accounts",
        selectedIcon = Icons.Default.AccountBalance,
        unselectedIcon = Icons.Outlined.AccountBalance
    )
    data object Analytics : BottomNavScreen(
        route = "analytics",
        title = "Analytics",
        selectedIcon = Icons.Default.Assessment,
        unselectedIcon = Icons.Outlined.Assessment
    )
    data object Settings : BottomNavScreen(
        route = "settings",
        title = "Settings",
        selectedIcon = Icons.Default.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    val icon: ImageVector get() = selectedIcon

    companion object {
        val items: List<BottomNavScreen>
            get() = listOf(Expenses, Events, Accounts, Analytics, Settings)
    }
}
