package com.yuki.baynote.ui.theme

import androidx.compose.ui.graphics.Color

data class CustomThemeColors(
    val name: String,
    val primary: Color,
    val background: Color,
    val surface: Color,
    val textColor: Color? = null   // null = auto-derived from background luminance
)
