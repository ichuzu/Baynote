package com.yuki.baynote.ui.screen.noteedit.markdown

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp

class MarkdownVisualTransformation : VisualTransformation {

    private val boldRegex   = Regex("\\*\\*(.*?)\\*\\*")
    private val italicRegex = Regex("(?<!\\*)\\*(?!\\*)(.*?)(?<!\\*)\\*(?!\\*)")

    override fun filter(text: AnnotatedString): TransformedText {
        val source = text.text

        val hidden = BooleanArray(source.length)
        var lineStart = 0
        for (line in source.lines()) {
            val lineEnd = lineStart + line.length

            val prefixLen = when {
                line.startsWith("### ") -> 4
                line.startsWith("## ")  -> 3
                line.startsWith("# ")   -> 2
                else -> 0
            }
            for (k in 0 until prefixLen) hidden[lineStart + k] = true

            for (match in boldRegex.findAll(line)) {
                val s = lineStart + match.range.first
                val e = lineStart + match.range.last + 1
                if (e - s >= 4) {
                    hidden[s] = true; hidden[s + 1] = true
                    hidden[e - 2] = true; hidden[e - 1] = true
                }
            }

            for (match in italicRegex.findAll(line)) {
                val s = lineStart + match.range.first
                val e = lineStart + match.range.last + 1
                if (e - s >= 2 && !hidden[s]) {
                    hidden[s] = true
                    hidden[e - 1] = true
                }
            }

            // Hide empty italic pairs ** (two adjacent single stars, not part of bold ****)
            var i = 0
            while (i < line.length - 1) {
                val abs = lineStart + i
                if (line[i] == '*' && line[i + 1] == '*' && !hidden[abs] && !hidden[abs + 1]) {
                    val prevStar = i > 0 && line[i - 1] == '*'
                    val nextStar = i + 2 < line.length && line[i + 2] == '*'
                    if (!prevStar && !nextStar) {
                        hidden[abs] = true
                        hidden[abs + 1] = true
                    }
                }
                i++
            }

            lineStart = lineEnd + 1
        }

        val visual = StringBuilder()
        val origToVis = IntArray(source.length + 1)
        val visToOrig = mutableListOf<Int>()
        var vPos = 0

        for (i in source.indices) {
            origToVis[i] = vPos
            if (!hidden[i]) {
                visual.append(source[i])
                visToOrig.add(i)
                vPos++
            }
        }
        origToVis[source.length] = vPos
        visToOrig.add(source.length)

        val visToOrigArr = visToOrig.toIntArray()

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                origToVis[offset.coerceIn(0, source.length)]
            override fun transformedToOriginal(offset: Int): Int =
                visToOrigArr[offset.coerceIn(0, visToOrig.size - 1)]
        }

        val builder = AnnotatedString.Builder(visual.toString())
        lineStart = 0

        for (line in source.lines()) {
            val lineEnd = lineStart + line.length

            when {
                line.startsWith("### ") -> {
                    val vs = origToVis[lineStart + 4]
                    val ve = origToVis[lineEnd]
                    if (vs < ve) builder.addStyle(
                        SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.3).sp), vs, ve)
                }
                line.startsWith("## ") -> {
                    val vs = origToVis[lineStart + 3]
                    val ve = origToVis[lineEnd]
                    if (vs < ve) builder.addStyle(
                        SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.3).sp), vs, ve)
                }
                line.startsWith("# ") -> {
                    val vs = origToVis[lineStart + 2]
                    val ve = origToVis[lineEnd]
                    if (vs < ve) builder.addStyle(
                        SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp), vs, ve)
                }
            }

            for (match in boldRegex.findAll(line)) {
                val g = match.groups[1]!!
                if (g.range.last >= g.range.first) {
                    val vs = origToVis[lineStart + g.range.first]
                    val ve = origToVis[lineStart + g.range.last + 1]
                    if (vs < ve) builder.addStyle(SpanStyle(fontWeight = FontWeight.Bold), vs, ve)
                }
            }

            for (match in italicRegex.findAll(line)) {
                val g = match.groups[1]!!
                if (g.range.last >= g.range.first) {
                    val vs = origToVis[lineStart + g.range.first]
                    val ve = origToVis[lineStart + g.range.last + 1]
                    if (vs < ve) builder.addStyle(SpanStyle(fontStyle = FontStyle.Italic), vs, ve)
                }
            }

            lineStart = lineEnd + 1
        }

        return TransformedText(builder.toAnnotatedString(), offsetMapping)
    }

}
