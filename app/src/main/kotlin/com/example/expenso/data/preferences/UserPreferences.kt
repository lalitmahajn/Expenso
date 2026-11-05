package com.example.expenso.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode(val displayName: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark")
}

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("expenso_prefs", Context.MODE_PRIVATE)

    private val _currency = MutableStateFlow(prefs.getString("currency_symbol", "$") ?: "$")
    val currency: StateFlow<String> = _currency.asStateFlow()

    private val savedTheme = try {
        ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
    } catch (_: Exception) {
        ThemeMode.SYSTEM
    }
    private val _themeMode = MutableStateFlow(savedTheme)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setCurrency(symbol: String) {
        prefs.edit().putString("currency_symbol", symbol).apply()
        _currency.value = symbol
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _themeMode.value = mode
    }

    companion object {
        val SUPPORTED_CURRENCIES = listOf("$", "₹", "€", "£", "¥", "₩", "A$", "C$", "Fr")
    }
}
