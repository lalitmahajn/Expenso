package com.example.expenso.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavScreen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Expenses : BottomNavScreen("expenses", "Expenses", Icons.AutoMirrored.Filled.ReceiptLong)
    data object Events : BottomNavScreen("events", "Events", Icons.Default.Event)
    data object Accounts : BottomNavScreen("accounts", "Accounts", Icons.Default.AccountBalance)
    data object Analytics : BottomNavScreen("analytics", "Analytics", Icons.Default.Assessment)
    data object Settings : BottomNavScreen("settings", "Settings", Icons.Default.Settings)

    companion object {
        val items: List<BottomNavScreen>
            get() = listOf(Expenses, Events, Accounts, Analytics, Settings)
    }
}
