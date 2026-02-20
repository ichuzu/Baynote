package com.yuki.baynote.ui.screen.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yuki.baynote.ui.theme.AppTheme
import com.yuki.baynote.ui.theme.CustomThemeColors
import com.yuki.baynote.ui.theme.DarkModePreference

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen(
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    darkMode: DarkModePreference,
    onDarkModeChange: (DarkModePreference) -> Unit,
    fontSize: Int,
    onFontSizeChange: (Int) -> Unit,
    headingMargin: Int,
    onHeadingMarginChange: (Int) -> Unit,
    customColors: CustomThemeColors? = null,
    savedThemes: List<CustomThemeColors> = emptyList(),
    onThemeCreatorClick: () -> Unit = {},
    onSavedThemeSelect: (CustomThemeColors) -> Unit = {},
    onSavedThemeEdit: (Int) -> Unit = {},
    onSavedThemeDelete: (Int) -> Unit = {},
    onNavigateBack: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {

            // ── Appearance ──────────────────────────────────────────────
            SettingsSectionHeader("Appearance")

            // Built-in themes
            SettingsItem(title = "Theme") {
                val scrollState = rememberScrollState()
                val bg = MaterialTheme.colorScheme.background
                Box {
                    Row(
                        modifier = Modifier.horizontalScroll(scrollState),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppTheme.entries.filter { it != AppTheme.CUSTOM }.forEach { theme ->
                            val isSelected = theme == currentTheme
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(theme.previewColor)
                                        .then(
                                            if (isSelected) Modifier.border(
                                                2.5.dp,
                                                MaterialTheme.colorScheme.onSurface,
                                                CircleShape
                                            ) else Modifier
                                        )
                                        .clickable { onThemeChange(theme) }
                                )
                                Text(
                                    theme.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    if (scrollState.value > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart).width(32.dp).fillMaxHeight()
                                .background(Brush.horizontalGradient(listOf(bg, Color.Transparent)))
                        )
                    }
                    if (scrollState.value < scrollState.maxValue) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd).width(32.dp).fillMaxHeight()
                                .background(Brush.horizontalGradient(listOf(Color.Transparent, bg)))
                        )
                    }
                }
            }

            // My (saved) themes
            SettingsItem(title = "My Themes") {
                val scrollState = rememberScrollState()
                val bg = MaterialTheme.colorScheme.background
                Box {
                    Row(
                        modifier = Modifier.horizontalScroll(scrollState),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        savedThemes.forEachIndexed { index, theme ->
                            val isSelected = currentTheme == AppTheme.CUSTOM && customColors == theme
                            var showMenu by remember { mutableStateOf(false) }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(theme.primary)
                                            .then(
                                                if (isSelected) Modifier.border(
                                                    2.5.dp,
                                                    MaterialTheme.colorScheme.onSurface,
                                                    CircleShape
                                                ) else Modifier
                                            )
                                            .combinedClickable(
                                                onClick = {
                                                    if (isSelected) onSavedThemeEdit(index)
                                                    else onSavedThemeSelect(theme)
                                                },
                                                onLongClick = { showMenu = true }
                                            )
                                    )
                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Edit") },
                                            onClick = { showMenu = false; onSavedThemeEdit(index) }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Filled.Delete,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            },
                                            onClick = { showMenu = false; onSavedThemeDelete(index) }
                                        )
                                    }
                                }
                                Text(
                                    theme.name.ifBlank { "Custom" },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 52.dp)
                                )
                            }
                        }

                        // "New" button — always at end
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { onThemeCreatorClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = "Create theme",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                "New",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (scrollState.value > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart).width(32.dp).fillMaxHeight()
                                .background(Brush.horizontalGradient(listOf(bg, Color.Transparent)))
                        )
                    }
                    if (scrollState.value < scrollState.maxValue) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd).width(32.dp).fillMaxHeight()
                                .background(Brush.horizontalGradient(listOf(Color.Transparent, bg)))
                        )
                    }
                }
            }

            SettingsItem(title = "Dark Mode") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DarkModePreference.entries.forEach { pref ->
                        FilterChip(
                            selected = darkMode == pref,
                            onClick = { onDarkModeChange(pref) },
                            label = {
                                Text(
                                    when (pref) {
                                        DarkModePreference.LIGHT -> "Light"
                                        DarkModePreference.DARK  -> "Dark"
                                    }
                                )
                            }
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // ── Typography ──────────────────────────────────────────────
            SettingsSectionHeader("Typography")

            SettingsItem(title = "Font Size") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(12, 14, 16, 18, 20).forEach { size ->
                        FilterChip(
                            selected = fontSize == size,
                            onClick = { onFontSizeChange(size) },
                            label = { Text("${size}sp") }
                        )
                    }
                }
            }

            SettingsItem(title = "Heading Spacing") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0 to "Compact", 1 to "Normal", 2 to "Spacious").forEach { (level, label) ->
                        FilterChip(
                            selected = headingMargin == level,
                            onClick = { onHeadingMarginChange(level) },
                            label = { Text(label) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
    )
}

@Composable
private fun SettingsItem(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        content()
    }
}
