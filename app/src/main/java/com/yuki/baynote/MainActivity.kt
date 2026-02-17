package com.yuki.baynote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.yuki.baynote.ui.navigation.BaynoteNavGraph
import com.yuki.baynote.ui.theme.AppTheme
import com.yuki.baynote.ui.theme.BaynoteTheme
import com.yuki.baynote.ui.theme.ThemePreferences

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var currentTheme by mutableStateOf(ThemePreferences.getTheme(this))

        setContent {
            BaynoteTheme(appTheme = currentTheme) {
                val navController = rememberNavController()
                BaynoteNavGraph(
                    navController = navController,
                    application = application,
                    currentTheme = currentTheme,
                    onThemeChange = { theme ->
                        currentTheme = theme
                        ThemePreferences.setTheme(this@MainActivity, theme)
                    }
                )
            }
        }
    }
}
