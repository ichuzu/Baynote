package com.yuki.baynote.ui.screen.noteedit.markdown

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

object FormattingAction {

    fun toggleHeading(value: TextFieldValue, level: Int): TextFieldValue {
        val text = value.text
        val cursor = value.selection.start

        val lineStart = text.lastIndexOf('\n', cursor - 1) + 1
        val lineEnd = text.indexOf('\n', cursor).let { if (it == -1) text.length else it }
        val line = text.substring(lineStart, lineEnd)

        val prefix = "#".repeat(level) + " "

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
            newLine = line.removePrefix(existingPrefix)
            cursorDelta = -existingPrefix.length
        } else {
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

    /**
     * Returns true only if [text] is wrapped with exactly [delimiter] at both ends.
     * For single-star italic, distinguishes from double-star bold by counting leading/trailing
     * stars — italic is present when the count is odd (1 = italic, 3 = bold+italic).
     */
    private fun isExactlyWrapped(text: String, delimiter: String): Boolean {
        val dLen = delimiter.length
        if (text.length <= dLen * 2) return false
        if (!text.startsWith(delimiter) || !text.endsWith(delimiter)) return false
        if (delimiter == "*") {
            val leading = text.takeWhile { it == '*' }.length
            val trailing = text.takeLastWhile { it == '*' }.length
            // Even star count = bold only (no italic layer)
            if (leading % 2 == 0 || trailing % 2 == 0) return false
        }
        return true
    }

    private fun toggleWrap(value: TextFieldValue, delimiter: String): TextFieldValue {
        val text = value.text
        val sel = value.selection
        val dLen = delimiter.length

        if (sel.collapsed) {
            val cursor = sel.start
            val lineStart = text.lastIndexOf('\n', cursor - 1) + 1
            val lineEnd = text.indexOf('\n', cursor).let { if (it == -1) text.length else it }
            val line = text.substring(lineStart, lineEnd)
            val lineContent = line.trimStart('#', ' ')

            if (lineContent.isEmpty()) {
                val actualLineStart = lineStart + (line.length - lineContent.length).coerceAtLeast(0)
                val newText = text.substring(0, actualLineStart) + delimiter + delimiter + text.substring(lineEnd)
                return TextFieldValue(newText, TextRange(actualLineStart + dLen))
            }

            val selStart = lineStart
            val selEnd = lineEnd
            val selected = text.substring(selStart, selEnd)

            if (isExactlyWrapped(selected, delimiter)) {
                val inner = selected.substring(dLen, selected.length - dLen)
                val newText = text.substring(0, selStart) + inner + text.substring(selEnd)
                val newCursor = (cursor - dLen).coerceIn(selStart, selStart + inner.length)
                return TextFieldValue(newText, TextRange(newCursor))
            }

            // Wrap the line content; do NOT strip existing markers (e.g. bold stays intact)
            val newText = text.substring(0, selStart) + delimiter + selected + delimiter + text.substring(selEnd)
            return TextFieldValue(newText, TextRange(selStart + dLen + selected.length + dLen))

        } else {
            val selStart = sel.min
            val selEnd = sel.max
            val selected = text.substring(selStart, selEnd)

            if (isExactlyWrapped(selected, delimiter)) {
                val inner = selected.substring(dLen, selected.length - dLen)
                val newText = text.substring(0, selStart) + inner + text.substring(selEnd)
                return TextFieldValue(newText, TextRange(selStart + inner.length))
            }

            // For italic (*), don't mistake a bold (**) boundary for an italic one
            val beforeHas = selStart >= dLen &&
                text.substring(selStart - dLen, selStart) == delimiter &&
                (delimiter != "*" || selStart < dLen + 1 || text[selStart - dLen - 1] != '*')
            val afterHas = selEnd + dLen <= text.length &&
                text.substring(selEnd, selEnd + dLen) == delimiter &&
                (delimiter != "*" || selEnd + dLen >= text.length || text[selEnd + dLen] != '*')

            return if (beforeHas && afterHas) {
                val newText = text.substring(0, selStart - dLen) + selected + text.substring(selEnd + dLen)
                TextFieldValue(newText, TextRange(selStart - dLen + selected.length))
            } else {
                val newText = text.substring(0, selStart) + delimiter + selected + delimiter + text.substring(selEnd)
                TextFieldValue(newText, TextRange(selStart + dLen, selEnd + dLen))
            }
        }
    }

    fun smartBackspace(value: TextFieldValue): TextFieldValue? {
        val text = value.text
        val cursor = value.selection.start
        if (!value.selection.collapsed || cursor == 0) return null

        val lineStart = text.lastIndexOf('\n', cursor - 1) + 1
        val lineEnd = text.indexOf('\n', cursor).let { if (it == -1) text.length else it }
        val line = text.substring(lineStart, lineEnd)

        // Heading: backspace at or inside the prefix removes the whole prefix
        val headingLevel = when {
            line.startsWith("### ") -> 3
            line.startsWith("## ") -> 2
            line.startsWith("# ") -> 1
            else -> 0
        }
        if (headingLevel > 0) {
            val prefixLen = headingLevel + 1
            val contentStart = lineStart + prefixLen
            if (cursor <= contentStart) {
                val newText = text.substring(0, lineStart) + line.substring(prefixLen) + text.substring(lineEnd)
                return TextFieldValue(newText, TextRange(lineStart))
            }
        }

        // Bold/italic: work on the full line to find any open+close pair the cursor sits inside
        for (delim in listOf("**", "*")) {
            val dLen = delim.length
            val beforeCursor = text.substring(lineStart, cursor)
            val afterCursor = text.substring(cursor, lineEnd)

            // Special case: cursor is right after a closing delimiter.
            // e.g. "*hello*|" — find the matching open and strip both delimiters.
            if (beforeCursor.length >= dLen &&
                beforeCursor.takeLast(dLen) == delim &&
                (dLen > 1 || beforeCursor.length < dLen + 1 || beforeCursor[beforeCursor.length - dLen - 1] != '*')
            ) {
                val beforeClose = beforeCursor.dropLast(dLen)
                val openIdx2 = findLastDelim(beforeClose, delim)
                if (openIdx2 >= 0) {
                    val inner = beforeClose.substring(openIdx2 + dLen)
                    val removeStart = lineStart + openIdx2
                    val newText = text.substring(0, removeStart) + inner + text.substring(cursor)
                    return TextFieldValue(newText, TextRange(removeStart + inner.length))
                }
            }

            // Find the innermost open delimiter before cursor
            val openIdx = findLastDelim(beforeCursor, delim)
            if (openIdx < 0) continue

            val innerContent = beforeCursor.substring(openIdx + dLen)
            val closeIdx = findFirstDelim(afterCursor, delim)

            if (closeIdx >= 0) {
                // Cursor is inside a complete pair
                return when {
                    innerContent.isEmpty() && closeIdx == 0 -> {
                        // Empty pair (**|**) — remove all 4 chars
                        val removeStart = lineStart + openIdx
                        val removeEnd = cursor + dLen
                        val newText = text.substring(0, removeStart) + text.substring(removeEnd)
                        TextFieldValue(newText, TextRange(removeStart))
                    }
                    innerContent.length == 1 && closeIdx == 0 -> {
                        // Single char left (**a|**) — remove everything
                        val removeStart = lineStart + openIdx
                        val removeEnd = cursor + closeIdx + dLen
                        val newText = text.substring(0, removeStart) + text.substring(removeEnd)
                        TextFieldValue(newText, TextRange(removeStart))
                    }
                    else -> {
                        // Normal delete inside bold/italic
                        val newText = text.substring(0, cursor - 1) + text.substring(cursor)
                        TextFieldValue(newText, TextRange(cursor - 1))
                    }
                }
            } else {
                // No closing delimiter found — dangling open marker
                // If cursor is right after the open marker (content empty), remove the marker
                if (innerContent.isEmpty()) {
                    val removeStart = lineStart + openIdx
                    val newText = text.substring(0, removeStart) + text.substring(cursor)
                    return TextFieldValue(newText, TextRange(removeStart))
                }
                // If only one char of content before cursor and no close, remove marker + char
                if (innerContent.length == 1) {
                    val removeStart = lineStart + openIdx
                    val newText = text.substring(0, removeStart) + text.substring(cursor)
                    return TextFieldValue(newText, TextRange(removeStart))
                }
                // Otherwise just delete the char before cursor normally
                val newText = text.substring(0, cursor - 1) + text.substring(cursor)
                return TextFieldValue(newText, TextRange(cursor - 1))
            }
        }

        return null
    }

    private fun findLastDelim(text: String, delim: String): Int {
        val dLen = delim.length
        var i = text.length - dLen
        while (i >= 0) {
            if (text.substring(i, i + dLen) == delim) {
                // For single-star italic, make sure it's not part of **
                if (dLen == 1) {
                    val prevStar = i > 0 && text[i - 1] == '*'
                    val nextStar = i + 1 < text.length && text[i + 1] == '*'
                    if (prevStar || nextStar) { i--; continue }
                }
                return i
            }
            i--
        }
        return -1
    }

    private fun findFirstDelim(text: String, delim: String): Int {
        val dLen = delim.length
        var i = 0
        while (i <= text.length - dLen) {
            if (text.substring(i, i + dLen) == delim) {
                if (dLen == 1) {
                    val prevStar = i > 0 && text[i - 1] == '*'
                    val nextStar = i + 1 < text.length && text[i + 1] == '*'
                    if (prevStar || nextStar) { i++; continue }
                }
                return i
            }
            i++
        }
        return -1
    }

    fun smartEnter(value: TextFieldValue): TextFieldValue? {
        val text = value.text
        val cursor = value.selection.start
        if (cursor == 0 || text[cursor - 1] != '\n') return null

        val newlinePos = cursor - 1
        val afterNewline = text.substring(cursor)

        // If a closing delimiter is sitting right after the newline, pull it back before the newline
        // so the formatted span stays on one line and the new line starts clean.
        for (delim in listOf("***", "**", "*")) {
            if (afterNewline.startsWith(delim)) {
                val newText = text.substring(0, newlinePos) + delim + "\n" + afterNewline.substring(delim.length)
                return TextFieldValue(newText, TextRange(newlinePos + delim.length + 1))
            }
        }

        // Check if the line before the newline has an unclosed formatting delimiter.
        // e.g. the user typed "*hello" (italic) and pressed Enter → close the span before the newline
        // so the next line starts clean instead of showing a dangling "*".
        val lineStart = text.lastIndexOf('\n', newlinePos - 1) + 1
        val lineBeforeNewline = text.substring(lineStart, newlinePos)

        for (delim in listOf("***", "**", "*")) {
            val openIdx = findLastDelim(lineBeforeNewline, delim)
            if (openIdx >= 0) {
                // Verify there is no matching close after the open on the same line
                val afterOpen = lineBeforeNewline.substring(openIdx + delim.length)
                val closeIdx = findFirstDelim(afterOpen, delim)
                if (closeIdx < 0) {
                    // Unclosed span — insert the closing delimiter before the newline
                    val newText = text.substring(0, newlinePos) + delim + "\n" + afterNewline
                    return TextFieldValue(newText, TextRange(newlinePos + delim.length + 1))
                }
            }
        }

        return null
    }

    fun insertTable(value: TextFieldValue): TextFieldValue {
        val text = value.text
        val cursor = value.selection.start

        val table = "| Header 1 | Header 2 |\n| --- | --- |\n| Cell 1 | Cell 2 |"

        val needsNewline = cursor > 0 && text.getOrNull(cursor - 1) != '\n'
        val prefix = if (needsNewline) "\n" else ""
        val insertion = prefix + table

        val newText = text.substring(0, cursor) + insertion + text.substring(cursor)

        val header1Start = cursor + prefix.length + 2
        val header1End = header1Start + 8
        return TextFieldValue(newText, TextRange(header1Start, header1End))
    }
}
