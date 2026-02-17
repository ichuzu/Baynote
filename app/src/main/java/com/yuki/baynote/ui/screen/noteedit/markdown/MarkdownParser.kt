package com.yuki.baynote.ui.screen.noteedit.markdown

enum class MarkdownStyle {
    H1, H2, H3, Bold, Italic, SyntaxDim
}

data class MarkdownStyleRange(
    val style: MarkdownStyle,
    val start: Int,
    val end: Int
)

object MarkdownParser {

    private val boldRegex = Regex("\\*\\*(.+?)\\*\\*")
    private val italicRegex = Regex("(?<!\\*)\\*(?!\\*)(.+?)(?<!\\*)\\*(?!\\*)")

    fun parse(text: String): List<MarkdownStyleRange> {
        val ranges = mutableListOf<MarkdownStyleRange>()
        var offset = 0

        for (line in text.lines()) {
            val lineEnd = offset + line.length

            // Headings
            when {
                line.startsWith("### ") -> {
                    ranges.add(MarkdownStyleRange(MarkdownStyle.H3, offset, lineEnd))
                    ranges.add(MarkdownStyleRange(MarkdownStyle.SyntaxDim, offset, offset + 4))
                }
                line.startsWith("## ") -> {
                    ranges.add(MarkdownStyleRange(MarkdownStyle.H2, offset, lineEnd))
                    ranges.add(MarkdownStyleRange(MarkdownStyle.SyntaxDim, offset, offset + 3))
                }
                line.startsWith("# ") -> {
                    ranges.add(MarkdownStyleRange(MarkdownStyle.H1, offset, lineEnd))
                    ranges.add(MarkdownStyleRange(MarkdownStyle.SyntaxDim, offset, offset + 2))
                }
            }

            // Bold (before italic to avoid conflicts)
            for (match in boldRegex.findAll(line)) {
                val matchStart = offset + match.range.first
                val matchEnd = offset + match.range.last + 1
                ranges.add(MarkdownStyleRange(MarkdownStyle.Bold, matchStart, matchEnd))
                ranges.add(MarkdownStyleRange(MarkdownStyle.SyntaxDim, matchStart, matchStart + 2))
                ranges.add(MarkdownStyleRange(MarkdownStyle.SyntaxDim, matchEnd - 2, matchEnd))
            }

            // Italic (single * not preceded/followed by *)
            for (match in italicRegex.findAll(line)) {
                val matchStart = offset + match.range.first
                val matchEnd = offset + match.range.last + 1
                ranges.add(MarkdownStyleRange(MarkdownStyle.Italic, matchStart, matchEnd))
                ranges.add(MarkdownStyleRange(MarkdownStyle.SyntaxDim, matchStart, matchStart + 1))
                ranges.add(MarkdownStyleRange(MarkdownStyle.SyntaxDim, matchEnd - 1, matchEnd))
            }

            offset = lineEnd + 1 // +1 for newline
        }

        return ranges
    }

    fun stripMarkdown(text: String): String {
        return text.lines().joinToString("\n") { line ->
            val stripped = when {
                line.startsWith("### ") -> line.removePrefix("### ")
                line.startsWith("## ") -> line.removePrefix("## ")
                line.startsWith("# ") -> line.removePrefix("# ")
                else -> line
            }
            stripped
                .replace(boldRegex, "$1")
                .replace(italicRegex, "$1")
        }
    }
}
