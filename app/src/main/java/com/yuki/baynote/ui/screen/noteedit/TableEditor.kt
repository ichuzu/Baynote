package com.yuki.baynote.ui.screen.noteedit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun TableEditor(
    rows: List<List<String>>,
    onRowsChange: (List<List<String>>) -> Unit,
    onDelete: () -> Unit,
    onUndoCheckpoint: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val borderColor = MaterialTheme.colorScheme.outlineVariant
    val colCount = rows.maxOfOrNull { it.size } ?: 2
    var focusCellRequest by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    fun updateCell(rowIndex: Int, colIndex: Int, value: String) {
        val newRows = rows.map { it.toMutableList() }.toMutableList()
        while (newRows[rowIndex].size <= colIndex) {
            newRows[rowIndex].add("")
        }
        newRows[rowIndex][colIndex] = value
        onRowsChange(newRows)
    }

    Column(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Remove table",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, borderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                rows.forEachIndexed { rowIndex, row ->
                    if (rowIndex > 0) {
                        HorizontalDivider(thickness = 1.dp, color = borderColor)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                    ) {
                        (0 until colCount).forEach { colIndex ->
                            if (colIndex > 0) {
                                VerticalDivider(thickness = 1.dp, color = borderColor)
                            }

                            key(rowIndex, colIndex) {
                                val cellValue = row.getOrElse(colIndex) { "" }
                                var cellTfv by remember {
                                    mutableStateOf(TextFieldValue(cellValue))
                                }
                                var enterCount by remember { mutableIntStateOf(0) }
                                var backspaceOnEmptyCount by remember { mutableIntStateOf(0) }
                                val cellFocusRequester = remember { FocusRequester() }

                                // Sync parent → local when cell value changes externally
                                LaunchedEffect(cellValue) {
                                    if (cellValue != cellTfv.text) {
                                        cellTfv = TextFieldValue(cellValue, TextRange(cellValue.length))
                                    }
                                }

                                // Request focus when this cell is targeted
                                LaunchedEffect(focusCellRequest) {
                                    if (focusCellRequest == Pair(rowIndex, colIndex)) {
                                        focusCellRequest = null
                                        cellFocusRequester.requestFocus()
                                    }
                                }

                                BasicTextField(
                                    value = cellTfv,
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Sentences
                                    ),
                                    onValueChange = { newTfv ->
                                        if ('\n' in newTfv.text) {
                                            val cleaned = newTfv.text.replace("\n", "")
                                            cellTfv = TextFieldValue(cleaned, TextRange(cleaned.length))
                                            enterCount++
                                            if (enterCount >= 2) {
                                                onUndoCheckpoint()
                                                val newRows = rows.toMutableList()
                                                newRows.add(rowIndex + 1, List(colCount) { "" })
                                                focusCellRequest = Pair(rowIndex + 1, 0)
                                                onRowsChange(newRows)
                                                enterCount = 0
                                            }
                                            if (cleaned != cellValue) {
                                                updateCell(rowIndex, colIndex, cleaned)
                                            }
                                        } else {
                                            // Word boundary detection for undo
                                            val oldText = cellTfv.text
                                            val newText = newTfv.text
                                            val shouldPush = when {
                                                newText.length > oldText.length + 1 -> true // paste
                                                newText.length == oldText.length + 1 -> {
                                                    val pos = newTfv.selection.start - 1
                                                    pos >= 0 && newText[pos] == ' '
                                                }
                                                else -> false
                                            }
                                            if (shouldPush) onUndoCheckpoint()

                                            cellTfv = newTfv
                                            enterCount = 0
                                            backspaceOnEmptyCount = 0
                                            if (newTfv.text != cellValue) {
                                                updateCell(rowIndex, colIndex, newTfv.text)
                                            }
                                        }
                                    },
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(12.dp)
                                        .focusRequester(cellFocusRequester)
                                        .onPreviewKeyEvent { event ->
                                            if (event.key == Key.Backspace &&
                                                event.type == KeyEventType.KeyDown &&
                                                cellTfv.text.isEmpty()
                                            ) {
                                                backspaceOnEmptyCount++
                                                if (backspaceOnEmptyCount >= 2 && rows.size > 1) {
                                                    onUndoCheckpoint()
                                                    val newRows = rows.toMutableList()
                                                    newRows.removeAt(rowIndex)
                                                    onRowsChange(newRows)
                                                    backspaceOnEmptyCount = 0
                                                    true
                                                } else {
                                                    false
                                                }
                                            } else {
                                                false
                                            }
                                        },
                                    decorationBox = { innerTextField ->
                                        Box {
                                            if (cellTfv.text.isEmpty() && rowIndex == 0) {
                                                Text(
                                                    "Header",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = {
                    onUndoCheckpoint()
                    focusCellRequest = Pair(rows.size, 0)
                    onRowsChange(rows + listOf(List(colCount) { "" }))
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(2.dp))
                Text("Row", style = MaterialTheme.typography.labelSmall)
            }
            TextButton(
                onClick = {
                    onUndoCheckpoint()
                    onRowsChange(rows.map { it + "" })
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(2.dp))
                Text("Column", style = MaterialTheme.typography.labelSmall)
            }
            if (rows.size > 1) {
                TextButton(
                    onClick = {
                        onUndoCheckpoint()
                        onRowsChange(rows.dropLast(1))
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(2.dp))
                    Text("Row", style = MaterialTheme.typography.labelSmall)
                }
            }
            if (colCount > 1) {
                TextButton(
                    onClick = {
                        onUndoCheckpoint()
                        onRowsChange(rows.map { it.dropLast(1) })
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(2.dp))
                    Text("Column", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
