package com.yuki.baynote.ui.theme

import android.content.Context

object ThemePreferences {
    private const val PREFS_NAME = "baynote_theme"
    private const val KEY_THEME = "selected_theme"

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
}
