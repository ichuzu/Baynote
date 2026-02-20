package com.yuki.baynote.ui.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

object ThemePreferences {
    private const val PREFS_NAME = "baynote_theme"
    private const val KEY_THEME = "selected_theme"
    private const val KEY_DARK_MODE = "dark_mode"
    private const val KEY_FONT_SIZE = "font_size"
    private const val KEY_HEADING_MARGIN = "heading_margin"
    private const val KEY_CUSTOM_NAME = "custom_theme_name"
    private const val KEY_CUSTOM_PRIMARY = "custom_primary"
    private const val KEY_CUSTOM_BACKGROUND = "custom_background"
    private const val KEY_CUSTOM_SURFACE = "custom_surface"
    private const val KEY_SAVED_THEMES = "saved_themes"

    // Font size: 12, 14, 16 (default), 18, 20
    fun getFontSize(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_FONT_SIZE, 16)
    }

    fun setFontSize(context: Context, size: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_FONT_SIZE, size).apply()
    }

    // Heading margin multiplier: 0 = compact, 1 = normal (default), 2 = spacious
    fun getHeadingMargin(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_HEADING_MARGIN, 1)
    }

    fun setHeadingMargin(context: Context, level: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_HEADING_MARGIN, level).apply()
    }

    fun getTheme(context: Context): AppTheme {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_THEME, AppTheme.DEFAULT.name) ?: AppTheme.DEFAULT.name
        return try {
            AppTheme.valueOf(name)
        } catch (_: IllegalArgumentException) {
            AppTheme.DEFAULT
        }
    }

    fun setTheme(context: Context, theme: AppTheme) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME, theme.name)
            .apply()
    }

    fun getDarkMode(context: Context): DarkModePreference {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_DARK_MODE, DarkModePreference.DARK.name) ?: DarkModePreference.DARK.name
        return try {
            DarkModePreference.valueOf(name)
        } catch (_: IllegalArgumentException) {
            DarkModePreference.DARK
        }
    }

    fun setDarkMode(context: Context, pref: DarkModePreference) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_DARK_MODE, pref.name)
            .apply()
    }

    fun getCustomTheme(context: Context): CustomThemeColors? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_CUSTOM_NAME, null) ?: return null
        val primary    = prefs.getInt(KEY_CUSTOM_PRIMARY,    Color(0xFF6750A4).toArgb())
        val background = prefs.getInt(KEY_CUSTOM_BACKGROUND, Color(0xFF1C1B1F).toArgb())
        val surface    = prefs.getInt(KEY_CUSTOM_SURFACE,    Color(0xFF110F13).toArgb())
        return CustomThemeColors(
            name = name,
            primary = Color(primary),
            background = Color(background),
            surface = Color(surface)
        )
    }

    fun saveCustomTheme(context: Context, colors: CustomThemeColors) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CUSTOM_NAME, colors.name)
            .putInt(KEY_CUSTOM_PRIMARY,    colors.primary.toArgb())
            .putInt(KEY_CUSTOM_BACKGROUND, colors.background.toArgb())
            .putInt(KEY_CUSTOM_SURFACE,    colors.surface.toArgb())
            .apply()
    }

    fun getSavedThemes(context: Context): List<CustomThemeColors> {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SAVED_THEMES, null) ?: return emptyList()
        return try {
            val array = org.json.JSONArray(json)
            List(array.length()) { i ->
                val obj = array.getJSONObject(i)
                CustomThemeColors(
                    name       = obj.getString("name"),
                    primary    = Color(obj.getInt("primary")),
                    background = Color(obj.getInt("background")),
                    surface    = Color(obj.getInt("surface"))
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    fun setSavedThemes(context: Context, themes: List<CustomThemeColors>) {
        val array = org.json.JSONArray()
        themes.forEach { theme ->
            val obj = org.json.JSONObject().apply {
                put("name",       theme.name)
                put("primary",    theme.primary.toArgb())
                put("background", theme.background.toArgb())
                put("surface",    theme.surface.toArgb())
            }
            array.put(obj)
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_SAVED_THEMES, array.toString()).apply()
    }
}
