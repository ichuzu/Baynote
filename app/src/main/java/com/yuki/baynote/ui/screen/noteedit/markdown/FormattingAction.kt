package com.yuki.baynote.ui.screen.noteedit.markdown

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

object FormattingAction {

    fun toggleHeading(value: TextFieldValue, level: Int): TextFieldValue {
        val text = value.text
        val cursor = value.selection.start

        // Find the current line
        val lineStart = text.lastIndexOf('\n', cursor - 1) + 1
        val lineEnd = text.indexOf('\n', cursor).let { if (it == -1) text.length else it }
        val line = text.substring(lineStart, lineEnd)

        val prefix = "#".repeat(level) + " "

        // Determine existing heading level
        val existingLevel = when {
            line.startsWith("### ") -> 3
            line.startsWith("## ") -> 2
            line.startsWith("# ") -> 1
            else -> 0
        }
        val existingPrefix = if (existingLevel > 0) "#".repeat(existingLevel) + " " else ""

        val newLine: String
        val cursorDelta: Int
        if (existingLevel == level) {
            // Remove heading
            newLine = line.removePrefix(existingPrefix)
            cursorDelta = -existingPrefix.length
        } else {
            // Replace or add heading
            newLine = prefix + line.removePrefix(existingPrefix)
            cursorDelta = prefix.length - existingPrefix.length
        }

        val newText = text.substring(0, lineStart) + newLine + text.substring(lineEnd)
        val newCursor = (cursor + cursorDelta).coerceIn(0, newText.length)
        return TextFieldValue(newText, TextRange(newCursor))
    }

    fun toggleBold(value: TextFieldValue): TextFieldValue {
        return toggleWrap(value, "**")
    }

    fun toggleItalic(value: TextFieldValue): TextFieldValue {
        return toggleWrap(value, "*")
    }

    private fun toggleWrap(value: TextFieldValue, delimiter: String): TextFieldValue {
        val text = value.text
        val sel = value.selection
        val dLen = delimiter.length

        // Resolve the range to operate on
        val selStart: Int
        val selEnd: Int
        val autoDetected: Boolean

        if (sel.collapsed) {
            // No selection — expand to the non-whitespace word around the cursor
            val cursor = sel.start
            var ws = cursor
            var we = cursor
            while (ws > 0 && !text[ws - 1].isWhitespace()) ws--
            while (we < text.length && !text[we].isWhitespace()) we++
            selStart = ws
            selEnd = we
            autoDetected = true
        } else {
            selStart = sel.min
            selEnd = sel.max
            autoDetected = false
        }

        // Cursor was on whitespace — fall back to inserting empty markers with cursor inside
        if (selStart == selEnd) {
            val newText = text.substring(0, selStart) + delimiter + delimiter + text.substring(selStart)
            return TextFieldValue(newText, TextRange(selStart + dLen))
        }

        val selected = text.substring(selStart, selEnd)

        // The auto-detected word itself starts/ends with the delimiter (already wrapped) → unwrap
        if (selected.startsWith(delimiter) && selected.endsWith(delimiter) && selected.length > dLen * 2) {
            val inner = selected.substring(dLen, selected.length - dLen)
            val newText = text.substring(0, selStart) + inner + text.substring(selEnd)
            return TextFieldValue(newText, TextRange(selStart, selStart + inner.length))
        }

        // Check if delimiters are immediately outside the selection → unwrap
        val beforeHas = selStart >= dLen && text.substring(selStart - dLen, selStart) == delimiter
        val afterHas = selEnd + dLen <= text.length && text.substring(selEnd, selEnd + dLen) == delimiter

        return if (beforeHas && afterHas) {
            val newText = text.substring(0, selStart - dLen) + selected + text.substring(selEnd + dLen)
            TextFieldValue(newText, TextRange(selStart - dLen, selEnd - dLen))
        } else {
            val newText = text.substring(0, selStart) + delimiter + selected + delimiter + text.substring(selEnd)
            val newSel = if (autoDetected) {
                // Cursor lands after the closing delimiter — Enter creates a fresh plain line
                TextRange(selEnd + dLen * 2)
            } else {
                // Keep selection on the wrapped content
                TextRange(selStart + dLen, selEnd + dLen)
            }
            TextFieldValue(newText, newSel)
        }
    }

    fun insertTable(value: TextFieldValue): TextFieldValue {
        val text = value.text
        val cursor = value.selection.start

        val table = "| Header 1 | Header 2 |\n| --- | --- |\n| Cell 1 | Cell 2 |"

        // Insert on a new line if cursor isn't at start of line
        val needsNewline = cursor > 0 && text.getOrNull(cursor - 1) != '\n'
        val prefix = if (needsNewline) "\n" else ""
        val insertion = prefix + table

        val newText = text.substring(0, cursor) + insertion + text.substring(cursor)

        // Select "Header 1" for immediate editing
        val header1Start = cursor + prefix.length + 2 // after "| "
        val header1End = header1Start + 8 // "Header 1"
        return TextFieldValue(newText, TextRange(header1Start, header1End))
    }
}
