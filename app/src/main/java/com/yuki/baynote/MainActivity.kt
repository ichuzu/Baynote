package com.yuki.baynote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.yuki.baynote.ui.navigation.BaynoteNavGraph
import com.yuki.baynote.ui.theme.AppTheme
import com.yuki.baynote.ui.theme.BaynoteTheme
import com.yuki.baynote.ui.theme.CustomThemeColors
import com.yuki.baynote.ui.theme.DarkModePreference
import com.yuki.baynote.ui.theme.ThemePreferences

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var currentTheme  by mutableStateOf(ThemePreferences.getTheme(this))
        var darkMode      by mutableStateOf(ThemePreferences.getDarkMode(this))
        var fontSize      by mutableIntStateOf(ThemePreferences.getFontSize(this))
        var headingMargin by mutableIntStateOf(ThemePreferences.getHeadingMargin(this))
        var customColors  by mutableStateOf<CustomThemeColors?>(ThemePreferences.getCustomTheme(this))
        var savedThemes   by mutableStateOf(ThemePreferences.getSavedThemes(this))

        setContent {
            val isDark = when (darkMode) {
                DarkModePreference.DARK  -> true
                DarkModePreference.LIGHT -> false
            }
            BaynoteTheme(appTheme = currentTheme, customColors = customColors, darkTheme = isDark) {
                val navController = rememberNavController()
                BaynoteNavGraph(
                    navController = navController,
                    application = application,
                    currentTheme = currentTheme,
                    onThemeChange = { theme ->
                        currentTheme = theme
                        ThemePreferences.setTheme(this@MainActivity, theme)
                    },
                    darkMode = darkMode,
                    onDarkModeChange = { pref ->
                        darkMode = pref
                        ThemePreferences.setDarkMode(this@MainActivity, pref)
                    },
                    fontSize = fontSize,
                    onFontSizeChange = { size ->
                        fontSize = size
                        ThemePreferences.setFontSize(this@MainActivity, size)
                    },
                    headingMargin = headingMargin,
                    onHeadingMarginChange = { level ->
                        headingMargin = level
                        ThemePreferences.setHeadingMargin(this@MainActivity, level)
                    },
                    customColors = customColors,
                    savedThemes = savedThemes,
                    onCustomThemeSave = { colors, editIndex ->
                        val mutable = savedThemes.toMutableList()
                        if (editIndex != null && editIndex < mutable.size) {
                            mutable[editIndex] = colors
                        } else {
                            mutable.add(colors)
                        }
                        savedThemes = mutable
                        ThemePreferences.setSavedThemes(this@MainActivity, mutable)
                        // Also set as the active custom theme immediately
                        customColors = colors
                        ThemePreferences.saveCustomTheme(this@MainActivity, colors)
                        currentTheme = AppTheme.CUSTOM
                        ThemePreferences.setTheme(this@MainActivity, AppTheme.CUSTOM)
                    },
                    onSavedThemeSelect = { colors ->
                        customColors = colors
                        ThemePreferences.saveCustomTheme(this@MainActivity, colors)
                        currentTheme = AppTheme.CUSTOM
                        ThemePreferences.setTheme(this@MainActivity, AppTheme.CUSTOM)
                    },
                    onSavedThemeDelete = { index ->
                        val deleted = savedThemes.getOrNull(index)
                        val mutable = savedThemes.toMutableList()
                        mutable.removeAt(index)
                        savedThemes = mutable
                        ThemePreferences.setSavedThemes(this@MainActivity, mutable)
                        // If the deleted theme was active, fall back to DEFAULT
                        if (currentTheme == AppTheme.CUSTOM && deleted == customColors) {
                            currentTheme = AppTheme.DEFAULT
                            ThemePreferences.setTheme(this@MainActivity, AppTheme.DEFAULT)
                        }
                    }
                )
            }
        }
    }
}
