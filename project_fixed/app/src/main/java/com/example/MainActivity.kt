package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainAppLayout
import com.example.ui.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.InventoryViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Keep track of dark mode in dynamic state
            val isDarkThemeByDefault = isSystemInDarkTheme()
            var darkModeState by remember { mutableStateOf(isDarkThemeByDefault) }

            MyApplicationTheme(darkTheme = darkModeState) {
                // Splash screen display state
                var showSplashScreen by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    delay(3000)
                    showSplashScreen = false
                }

                if (showSplashScreen) {
                    SplashScreen()
                } else {
                    // Compose ViewModel provider
                    val inventoryViewModel: InventoryViewModel = viewModel()

                    MainAppLayout(
                        viewModel = inventoryViewModel,
                        darkMode = darkModeState,
                        onToggleDarkMode = { darkModeState = !darkModeState }
                    )
                }
            }
        }
    }
}
