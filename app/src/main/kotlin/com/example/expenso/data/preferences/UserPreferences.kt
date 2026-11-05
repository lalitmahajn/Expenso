package com.example.expenso.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("expenso_prefs", Context.MODE_PRIVATE)

    private val _currency = MutableStateFlow(prefs.getString("currency_symbol", "$") ?: "$")
    val currency: StateFlow<String> = _currency.asStateFlow()

    fun setCurrency(symbol: String) {
        prefs.edit().putString("currency_symbol", symbol).apply()
        _currency.value = symbol
    }

    companion object {
        val SUPPORTED_CURRENCIES = listOf("$", "₹", "€", "£", "¥", "₩", "A$", "C$", "Fr")
    }
}
