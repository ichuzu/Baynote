package com.yuki.baynote.ui.screen.noteedit

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class FormattingOption {
    H1, H2, H3, Bold, Italic, Table
}

@Composable
fun FormattingToolbar(
    onFormat: (FormattingOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            IconButton(onClick = { onFormat(FormattingOption.H1) }) {
                Text("H1", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = { onFormat(FormattingOption.H2) }) {
                Text("H2", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = { onFormat(FormattingOption.H3) }) {
                Text("H3", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.width(8.dp))

            IconButton(onClick = { onFormat(FormattingOption.Bold) }) {
                Icon(Icons.Filled.FormatBold, contentDescription = "Bold")
            }
            IconButton(onClick = { onFormat(FormattingOption.Italic) }) {
                Icon(Icons.Filled.FormatItalic, contentDescription = "Italic")
            }

            Spacer(Modifier.width(8.dp))

            IconButton(onClick = { onFormat(FormattingOption.Table) }) {
                Icon(Icons.Filled.TableChart, contentDescription = "Insert table")
            }
        }
    }
}
