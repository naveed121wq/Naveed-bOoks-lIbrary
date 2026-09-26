package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.presentation.navigation.AppNavHost
import com.example.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as BooksApplication
        val preferencesRepository = app.userPreferencesRepository

        setContent {
            val themeMode by preferencesRepository.themeModeFlow.collectAsState(initial = "SYSTEM")
            val isDark = when (themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            AppTheme(darkTheme = isDark) {
                AppNavHost(app = app)
            }
        }
    }
}
