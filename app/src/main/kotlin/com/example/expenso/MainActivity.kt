package com.example.expenso

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.expenso.data.preferences.ThemeMode
import com.example.expenso.ui.main.ExpensoMainScreen
import com.example.expenso.ui.theme.ExpensoTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val app = application as ExpensoApp
    val userPreferences = app.container.userPreferences

    enableEdgeToEdge()
    setContent {
      val themeMode by userPreferences.themeMode.collectAsState()
      val systemInDark = isSystemInDarkTheme()
      val useDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemInDark
      }

      ExpensoTheme(darkTheme = useDarkTheme) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          ExpensoMainScreen()
        }
      }
    }
  }
}
