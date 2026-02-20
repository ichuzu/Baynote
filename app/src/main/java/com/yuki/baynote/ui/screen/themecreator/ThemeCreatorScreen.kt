package com.yuki.baynote.ui.screen.themecreator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yuki.baynote.ui.theme.CustomThemeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeCreatorScreen(
    initialColors: CustomThemeColors?,
    onSave: (CustomThemeColors) -> Unit,
    onNavigateBack: () -> Unit
) {
    var name       by rememberSaveable { mutableStateOf(initialColors?.name ?: "") }
    var primary    by remember { mutableStateOf(initialColors?.primary    ?: Color(0xFF6750A4)) }
    var background by remember { mutableStateOf(initialColors?.background ?: Color(0xFF1C1B1F)) }
    var surface    by remember { mutableStateOf(initialColors?.surface    ?: Color(0xFF2B2930)) }

    var pickerTarget  by remember { mutableStateOf<String?>(null) }
    var showImport    by remember { mutableStateOf(false) }
    var showExport    by remember { mutableStateOf(false) }
    var importError   by remember { mutableStateOf<String?>(null) }

    val exportCode = buildExportCode(name, primary, background, surface)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (initialColors != null) "Edit Theme" else "Create Theme") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            onSave(CustomThemeColors(
                                name = name.ifBlank { "Custom" },
                                primary = primary,
                                background = background,
                                surface = surface
                            ))
                        }
                    ) {
                        Text("Save")
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
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Name ────────────────────────────────────────────────────
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Theme Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            // ── Live Preview ─────────────────────────────────────────────
            Text("Preview", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            ThemePreview(primary = primary, background = background, surface = surface)

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            // ── Color Pickers ─────────────────────────────────────────────
            Text("Colors", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))

            ColorRow(label = "Primary (accent)", color = primary,    onClick = { pickerTarget = "primary" })
            Spacer(Modifier.height(10.dp))
            ColorRow(label = "Background",       color = background, onClick = { pickerTarget = "background" })
            Spacer(Modifier.height(10.dp))
            ColorRow(label = "Card Surface",     color = surface,    onClick = { pickerTarget = "surface" })

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            // ── Import / Export ───────────────────────────────────────────
            Text("Share", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { showExport = true }, modifier = Modifier.weight(1f)) {
                    Text("Export Code")
                }
                OutlinedButton(onClick = { showImport = true; importError = null }, modifier = Modifier.weight(1f)) {
                    Text("Import Code")
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // Color picker dialogs
    when (pickerTarget) {
        "primary"    -> ColorPickerDialog(primary,    { primary = it })    { pickerTarget = null }
        "background" -> ColorPickerDialog(background, { background = it }) { pickerTarget = null }
        "surface"    -> ColorPickerDialog(surface,    { surface = it })    { pickerTarget = null }
    }

    // Export dialog
    if (showExport) {
        var copied by remember { mutableStateOf(false) }
        val clipboard = LocalClipboardManager.current
        AlertDialog(
            onDismissRequest = { showExport = false },
            title = { Text("Export Code") },
            text = {
                Column {
                    Text(
                        "Share this code with others so they can import your theme:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            exportCode,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = null),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(4.dp))
                        IconButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(exportCode))
                                copied = true
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                if (copied) Icons.Filled.Check else Icons.Filled.ContentCopy,
                                contentDescription = "Copy code",
                                modifier = Modifier.size(18.dp),
                                tint = if (copied) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExport = false }) { Text("Close") }
            }
        )
    }

    // Import dialog
    if (showImport) {
        var importText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showImport = false },
            title = { Text("Import Code") },
            text = {
                Column {
                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it; importError = null },
                        label = { Text("Paste code here") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (importError != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(importError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val result = parseImportCode(importText.trim())
                    if (result != null) {
                        name = result.name
                        primary = result.primary
                        background = result.background
                        surface = result.surface
                        showImport = false
                    } else {
                        importError = "Invalid code. Expected: BAYNOTE-Name-RRGGBB-RRGGBB-RRGGBB"
                    }
                }) { Text("Import") }
            },
            dismissButton = {
                TextButton(onClick = { showImport = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ThemePreview(primary: Color, background: Color, surface: Color) {
    fun contrast(c: Color) = if (c.luminance() > 0.179f) Color(0xFF1A1A1A) else Color(0xFFF0F0F0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar mock
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(contrast(background).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("←", color = contrast(background), fontSize = 12.sp)
                }
                Spacer(Modifier.width(10.dp))
                Text("My Note", color = contrast(background), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(12.dp))

            // Note card mock
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(surface)
                    .padding(12.dp)
            ) {
                Column {
                    Text("Note Title", color = contrast(surface), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("Note content preview...", color = contrast(surface).copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    // Label chip mock
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(primary.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("label", color = primary, fontSize = 10.sp)
                    }
                }
            }
        }

        // FAB mock
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(primary)
                .align(Alignment.BottomEnd),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = contrast(primary), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun ColorRow(label: String, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "#${String.format("%06X", color.toArgb() and 0xFFFFFF)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
        }
    }
}

private fun buildExportCode(name: String, primary: Color, background: Color, surface: Color): String {
    val n = name.ifBlank { "Custom" }.replace(" ", "+")
    val p = String.format("%06X", primary.toArgb() and 0xFFFFFF)
    val b = String.format("%06X", background.toArgb() and 0xFFFFFF)
    val s = String.format("%06X", surface.toArgb() and 0xFFFFFF)
    return "BAYNOTE-$n-$p-$b-$s"
}

private fun parseImportCode(code: String): CustomThemeColors? {
    val parts = code.split("-")
    if (parts.size != 5 || parts[0] != "BAYNOTE") return null
    val name = parts[1].replace("+", " ")
    return try {
        val primary    = Color(0xFF000000.or(parts[2].toLong(16)).toInt())
        val background = Color(0xFF000000.or(parts[3].toLong(16)).toInt())
        val surface    = Color(0xFF000000.or(parts[4].toLong(16)).toInt())
        if (parts[2].length != 6 || parts[3].length != 6 || parts[4].length != 6) return null
        CustomThemeColors(name = name, primary = primary, background = background, surface = surface)
    } catch (_: NumberFormatException) { null }
}
