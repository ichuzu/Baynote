package com.yuki.baynote.ui.screen.noteedit.markdown

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import com.yuki.baynote.ui.screen.noteedit.FormulaEvaluator

/**
 * Wraps MarkdownVisualTransformation and additionally renders inline math results
 * in the accent colour as live ghost text after any line ending with '='.
 *
 * E.g. typing "190000-90000=" shows "190000-90000=100000" where "100000" is
 * displayed in primary colour but is NOT part of the real text.  Pressing Enter
 * commits the result into the actual text.
 */
class MathAnnotationTransformation(
    private val markdown: MarkdownVisualTransformation,
    private val accentColor: Color
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        // 1. Apply markdown transformation first
        val mdResult = markdown.filter(text)
        val mdMapping = mdResult.offsetMapping

        // 2. Find all lines in original text that end with '=' and have a valid result
        val injections = computeInjections(text.text) // List<Pair<origPos, resultString>>
        if (injections.isEmpty()) return mdResult

        // 3. Map original injection positions into md-transformed space
        data class Injection(val mdPos: Int, val result: String, val cumBefore: Int)

        val mapped = mutableListOf<Injection>()
        var cumulative = 0
        for ((origPos, result) in injections.sortedBy { it.first }) {
            mapped.add(Injection(mdMapping.originalToTransformed(origPos), result, cumulative))
            cumulative += result.length
        }
        val totalInserted = cumulative

        // 4. Build the final annotated string with ghost text injected
        val builder = AnnotatedString.Builder()
        var lastMdPos = 0
        for (inj in mapped) {
            builder.append(mdResult.text.subSequence(lastMdPos, inj.mdPos))
            builder.withStyle(SpanStyle(color = accentColor)) { append(inj.result) }
            lastMdPos = inj.mdPos
        }
        builder.append(mdResult.text.subSequence(lastMdPos, mdResult.text.length))
        val finalText = builder.toAnnotatedString()

        // 5. Compose offset mappings
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val mdOffset = mdMapping.originalToTransformed(offset)
                var extra = 0
                for (inj in mapped) {
                    if (inj.mdPos <= mdOffset) extra = inj.cumBefore + inj.result.length
                    else break
                }
                return mdOffset + extra
            }

            override fun transformedToOriginal(offset: Int): Int {
                var subtracted = 0
                for (inj in mapped) {
                    val ghostStart = inj.mdPos + inj.cumBefore
                    val ghostEnd = ghostStart + inj.result.length
                    when {
                        offset < ghostStart -> break
                        offset < ghostEnd  -> return mdMapping.transformedToOriginal(inj.mdPos)
                        else               -> subtracted = inj.cumBefore + inj.result.length
                    }
                }
                return mdMapping.transformedToOriginal((offset - subtracted).coerceAtLeast(0))
            }
        }

        return TransformedText(finalText, offsetMapping)
    }

    private fun computeInjections(text: String): List<Pair<Int, String>> {
        val result = mutableListOf<Pair<Int, String>>()
        var offset = 0
        for (line in text.split('\n')) {
            val trimmed = line.trimEnd()
            if (trimmed.endsWith("=")) {
                val eqPos = offset + trimmed.length  // position just after '=' in original
                val expr = trimmed.dropLast(1)
                    .replace(",", "")
                    .replace(" ", "")
                    .trim()
                if (expr.isNotEmpty()) {
                    val mathResult = FormulaEvaluator.evaluate("=$expr", emptyList())
                    if (mathResult != null) {
                        result.add(Pair(eqPos, FormulaEvaluator.formatResult(mathResult)))
                    }
                }
            }
            offset += line.length + 1  // +1 for '\n'
        }
        return result
    }
}
