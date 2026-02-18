package com.yuki.baynote.ui.screen.noteedit.markdown

sealed class ContentSegment {
    data class Text(val text: String) : ContentSegment()
    data class Table(val rows: List<List<String>>) : ContentSegment()
}

object ContentSegmentParser {

    private val separatorRegex = Regex("^\\|\\s*:?-+:?\\s*(?:\\|\\s*:?-+:?\\s*)*\\|\\s*$")

    fun parseSegments(content: String): List<ContentSegment> {
        if (content.isEmpty()) return listOf(ContentSegment.Text(""))
        val lines = content.lines()
        val segments = mutableListOf<ContentSegment>()
        val textBuffer = mutableListOf<String>()
        var i = 0

        while (i < lines.size) {
            if (isTableLine(lines[i])) {
                if (textBuffer.isNotEmpty()) {
                    segments.add(ContentSegment.Text(textBuffer.joinToString("\n")))
                    textBuffer.clear()
                }
                val tableLines = mutableListOf<String>()
                while (i < lines.size && isTableLine(lines[i])) {
                    tableLines.add(lines[i])
                    i++
                }
                segments.add(parseTableBlock(tableLines))
            } else {
                textBuffer.add(lines[i])
                i++
            }
        }

        if (textBuffer.isNotEmpty()) {
            segments.add(ContentSegment.Text(textBuffer.joinToString("\n")))
        }

        return segments.ifEmpty { listOf(ContentSegment.Text("")) }
    }

    private fun isTableLine(line: String): Boolean {
        val trimmed = line.trim()
        return trimmed.length > 1 && trimmed.startsWith("|") && trimmed.endsWith("|")
    }

    private fun parseTableBlock(lines: List<String>): ContentSegment.Table {
        val dataRows = lines
            .filter { !separatorRegex.matches(it.trim()) }
            .map { line ->
                line.trim()
                    .removePrefix("|")
                    .removeSuffix("|")
                    .split("|")
                    .map { it.trim() }
            }
        return if (dataRows.isEmpty()) {
            createEmptyTable()
        } else {
            ContentSegment.Table(dataRows)
        }
    }

    fun segmentsToString(segments: List<ContentSegment>): String {
        if (segments.isEmpty()) return ""
        return segments.joinToString("\n") { segment ->
            when (segment) {
                is ContentSegment.Text -> segment.text
                is ContentSegment.Table -> tableToMarkdown(segment.rows)
            }
        }
    }

    private fun tableToMarkdown(rows: List<List<String>>): String {
        if (rows.isEmpty()) return ""
        val colCount = rows.maxOf { it.size }
        fun padRow(row: List<String>): List<String> =
            (0 until colCount).map { row.getOrElse(it) { "" } }

        val sb = StringBuilder()
        sb.appendLine(padRow(rows.first()).joinToString(" | ", "| ", " |"))
        sb.appendLine((1..colCount).joinToString(" | ", "| ", " |") { "---" })
        rows.drop(1).forEach { row ->
            sb.appendLine(padRow(row).joinToString(" | ", "| ", " |"))
        }
        return sb.toString().trimEnd()
    }

    fun createEmptyTable(): ContentSegment.Table {
        return ContentSegment.Table(
            rows = listOf(listOf("", ""), listOf("", ""))
        )
    }
}
