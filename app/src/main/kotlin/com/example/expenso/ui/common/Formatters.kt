package com.example.expenso.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CategoryIconOption(
    val name: String,
    val icon: ImageVector,
    val label: String
)

object Formatters {
    private val decimalFormat = DecimalFormat("#,##0.00")
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val fullDateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())

    val AVAILABLE_CATEGORY_ICONS = listOf(
        CategoryIconOption("Restaurant", Icons.Default.Restaurant, "Food"),
        CategoryIconOption("ShoppingCart", Icons.Default.ShoppingCart, "Groceries"),
        CategoryIconOption("Coffee", Icons.Default.Coffee, "Coffee & Snacks"),
        CategoryIconOption("DirectionsCar", Icons.Default.DirectionsCar, "Transport"),
        CategoryIconOption("LocalGasStation", Icons.Default.LocalGasStation, "Fuel"),
        CategoryIconOption("Receipt", Icons.Default.Receipt, "Bills"),
        CategoryIconOption("ShoppingBag", Icons.Default.ShoppingBag, "Shopping"),
        CategoryIconOption("Movie", Icons.Default.Movie, "Entertainment"),
        CategoryIconOption("SportsEsports", Icons.Default.SportsEsports, "Gaming"),
        CategoryIconOption("LocalHospital", Icons.Default.LocalHospital, "Health"),
        CategoryIconOption("Flight", Icons.Default.Flight, "Travel"),
        CategoryIconOption("School", Icons.Default.School, "Education"),
        CategoryIconOption("FitnessCenter", Icons.Default.FitnessCenter, "Fitness"),
        CategoryIconOption("Pets", Icons.Default.Pets, "Pets"),
        CategoryIconOption("Work", Icons.Default.Work, "Work"),
        CategoryIconOption("Home", Icons.Default.Home, "Housing"),
        CategoryIconOption("CardGiftcard", Icons.Default.CardGiftcard, "Gifts"),
        CategoryIconOption("Savings", Icons.Default.Savings, "Savings"),
        CategoryIconOption("Celebration", Icons.Default.Celebration, "Events"),
        CategoryIconOption("MoreHoriz", Icons.Default.MoreHoriz, "Other")
    )

    val AVAILABLE_CATEGORY_COLORS = listOf(
        "#FF5722", // Deep Orange
        "#FF9800", // Orange
        "#FFC107", // Amber
        "#4CAF50", // Green
        "#009688", // Teal
        "#00BCD4", // Cyan
        "#2196F3", // Blue
        "#3F51B5", // Indigo
        "#673AB7", // Deep Purple
        "#9C27B0", // Purple
        "#E91E63", // Pink
        "#F44336", // Red
        "#795548", // Brown
        "#607D8B"  // Blue Grey
    )

    fun formatAmount(amount: Double, currency: String = "$"): String {
        return "$currency${decimalFormat.format(amount)}"
    }

    fun formatDate(timestamp: Long): String {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = timestamp }

        return when {
            now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR) -> "Today"
            now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) - target.get(Calendar.DAY_OF_YEAR) == 1 -> "Yesterday"
            else -> dateFormat.format(Date(timestamp))
        }
    }

    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }

    fun formatFullDate(timestamp: Long): String {
        return fullDateFormat.format(Date(timestamp))
    }

    fun parseColor(hex: String, default: Color = Color(0xFF607D8B)): Color {
        return try {
            val cleanHex = hex.removePrefix("#")
            val colorLong = cleanHex.toLong(16)
            val argb = if (cleanHex.length == 6) {
                (0xFF000000L or colorLong).toInt()
            } else {
                colorLong.toInt()
            }
            Color(argb)
        } catch (e: Exception) {
            default
        }
    }

    fun getCategoryIcon(iconName: String): ImageVector {
        return when (iconName) {
            "Restaurant" -> Icons.Default.Restaurant
            "ShoppingCart" -> Icons.Default.ShoppingCart
            "Coffee" -> Icons.Default.Coffee
            "DirectionsCar" -> Icons.Default.DirectionsCar
            "LocalGasStation" -> Icons.Default.LocalGasStation
            "Receipt" -> Icons.Default.Receipt
            "ShoppingBag" -> Icons.Default.ShoppingBag
            "Movie" -> Icons.Default.Movie
            "SportsEsports" -> Icons.Default.SportsEsports
            "LocalHospital" -> Icons.Default.LocalHospital
            "Flight" -> Icons.Default.Flight
            "School" -> Icons.Default.School
            "FitnessCenter" -> Icons.Default.FitnessCenter
            "Pets" -> Icons.Default.Pets
            "Work" -> Icons.Default.Work
            "Home" -> Icons.Default.Home
            "CardGiftcard" -> Icons.Default.CardGiftcard
            "Savings" -> Icons.Default.Savings
            "Celebration" -> Icons.Default.Celebration
            else -> Icons.Default.MoreHoriz
        }
    }

    fun getAccountIcon(iconName: String): ImageVector {
        return when (iconName) {
            "AccountBalance" -> Icons.Default.AccountBalance
            "Payments" -> Icons.Default.Payments
            "Wallet" -> Icons.Default.AccountBalanceWallet
            "CreditCard" -> Icons.Default.CreditCard
            "Savings" -> Icons.Default.Savings
            else -> Icons.Default.AccountBalanceWallet
        }
    }
}
