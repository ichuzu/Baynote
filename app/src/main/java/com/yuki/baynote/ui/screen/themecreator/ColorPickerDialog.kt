package com.yuki.baynote.ui.screen.themecreator

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas

@Composable
fun ColorPickerDialog(
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    val hsv = remember(initialColor) {
        val arr = FloatArray(3)
        android.graphics.Color.colorToHSV(initialColor.toArgb(), arr)
        arr
    }
    var hue        by remember { mutableFloatStateOf(hsv[0]) }
    var saturation by remember { mutableFloatStateOf(hsv[1]) }
    var value      by remember { mutableFloatStateOf(hsv[2]) }
    var hexText    by remember { mutableStateOf(initialColor.toHexString()) }
    var editingHex by remember { mutableStateOf(false) }

    val currentColor = Color.hsv(hue, saturation, value)

    LaunchedEffect(hue, saturation, value) {
        if (!editingHex) hexText = currentColor.toHexString()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pick a Color") },
        text = {
            Column {
                // Preview swatch
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(currentColor)
                )

                Spacer(Modifier.height(12.dp))

                // 2D SV panel
                SvPanel(
                    hue = hue,
                    saturation = saturation,
                    value = value,
                    onChanged = { s, v -> saturation = s; value = v },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(Modifier.height(10.dp))

                // Hue strip
                HueStrip(
                    hue = hue,
                    onChanged = { hue = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .clip(RoundedCornerShape(14.dp))
                )

                Spacer(Modifier.height(10.dp))

                // Hex input
                OutlinedTextField(
                    value = hexText,
                    onValueChange = { text ->
                        hexText = text
                        editingHex = true
                        val cleaned = text.removePrefix("#").trim()
                        if (cleaned.length == 6) {
                            parseHex(cleaned)?.let { color ->
                                val arr = FloatArray(3)
                                android.graphics.Color.colorToHSV(color.toArgb(), arr)
                                hue = arr[0]; saturation = arr[1]; value = arr[2]
                            }
                        }
                    },
                    label = { Text("Hex") },
                    prefix = { Text("#") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onColorSelected(currentColor); onDismiss() }) { Text("Apply") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun SvPanel(
    hue: Float,
    saturation: Float,
    value: Float,
    onChanged: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val hueColor = Color.hsv(hue, 1f, 1f)

    Canvas(
        modifier = modifier.pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                onChanged(
                    (down.position.x / size.width).coerceIn(0f, 1f),
                    1f - (down.position.y / size.height).coerceIn(0f, 1f)
                )
                do {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull() ?: break
                    if (change.pressed) {
                        onChanged(
                            (change.position.x / size.width).coerceIn(0f, 1f),
                            1f - (change.position.y / size.height).coerceIn(0f, 1f)
                        )
                    }
                } while (event.changes.any { it.pressed })
            }
        }
    ) {
        drawRect(Brush.horizontalGradient(listOf(Color.White, hueColor)))
        drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))

        val cx = saturation * size.width
        val cy = (1f - value) * size.height
        drawCircle(Color.White, radius = 9.dp.toPx(), center = Offset(cx, cy), style = Stroke(2.5.dp.toPx()))
        drawCircle(Color.Black, radius = 9.dp.toPx(), center = Offset(cx, cy), style = Stroke(1.dp.toPx()))
    }
}

@Composable
private fun HueStrip(
    hue: Float,
    onChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val hueColors = remember {
        listOf(
            Color.hsv(0f,   1f, 1f), Color.hsv(60f,  1f, 1f),
            Color.hsv(120f, 1f, 1f), Color.hsv(180f, 1f, 1f),
            Color.hsv(240f, 1f, 1f), Color.hsv(300f, 1f, 1f),
            Color.hsv(360f, 1f, 1f)
        )
    }

    Canvas(
        modifier = modifier.pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                onChanged((down.position.x / size.width).coerceIn(0f, 1f) * 360f)
                do {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull() ?: break
                    if (change.pressed) {
                        onChanged((change.position.x / size.width).coerceIn(0f, 1f) * 360f)
                    }
                } while (event.changes.any { it.pressed })
            }
        }
    ) {
        drawRect(Brush.horizontalGradient(hueColors))
        val tx = (hue / 360f) * size.width
        val r  = size.height / 2f - 2.dp.toPx()
        drawCircle(Color.White, radius = r, center = Offset(tx, size.height / 2f), style = Stroke(3.dp.toPx()))
        drawCircle(Color(0x66000000), radius = r, center = Offset(tx, size.height / 2f), style = Stroke(1.dp.toPx()))
    }
}

private fun Color.toHexString(): String =
    String.format("%06X", toArgb() and 0xFFFFFF)

private fun parseHex(hex: String): Color? = try {
    Color(0xFF000000.or(hex.toLong(16)).toInt())
} catch (_: NumberFormatException) { null }
