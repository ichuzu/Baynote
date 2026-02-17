package com.yuki.baynote.ui.screen.noteedit.markdown

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp

class MarkdownVisualTransformation(
    private val syntaxDimColor: Color
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val ranges = MarkdownParser.parse(text.text)
        val builder = AnnotatedString.Builder(text)

        for (range in ranges) {
            val spanStyle = when (range.style) {
                MarkdownStyle.H1 -> SpanStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                )
                MarkdownStyle.H2 -> SpanStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.3).sp
                )
                MarkdownStyle.H3 -> SpanStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.3).sp
                )
                MarkdownStyle.Bold -> SpanStyle(fontWeight = FontWeight.Bold)
                MarkdownStyle.Italic -> SpanStyle(fontStyle = FontStyle.Italic)
                MarkdownStyle.SyntaxDim -> SpanStyle(color = syntaxDimColor)
            }
            builder.addStyle(spanStyle, range.start, range.end)
        }

        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}
