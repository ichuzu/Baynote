package com.yuki.baynote.ui.screen.noteedit

object FormulaEvaluator {
    fun buildCellRef(row: Int, col: Int): String {
        val colLetter = ('A' + col).toString()
        val rowNumber = row + 1
        return "$colLetter$rowNumber"
    }

    fun parseCellRef(token: String): Pair<Int, Int>? {
        val match = Regex("^([A-Z])(\\d+)$").matchEntire(token) ?: return null
        val col = match.groupValues[1][0] - 'A'
        val row = match.groupValues[2].toIntOrNull()?.minus(1) ?: return null
        if (row < 0) return null
        return Pair(row, col)
    }

    fun evaluate(formula: String, rows: List<List<String>>): Double? {
        val expr = formula.removePrefix("=").trim()
        if (expr.isEmpty()) return null
        return try {
            ExpressionParser(expr, rows).parse()
        } catch (e: Exception) {
            null
        }
    }

    fun formatResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "ERR"
        return if (value % 1.0 == 0.0) {
            value.toLong().toString()
        } else {
            "%.4f".format(value).trimEnd('0').trimEnd('.')
        }
    }
}

private class ExpressionParser(
    private val input: String,
    private val rows: List<List<String>>
) {
    private var pos = 0

    fun parse(): Double? {
        skipWhitespace()
        if (pos >= input.length) return null
        val result = parseAddSub()
        skipWhitespace()
        return if (pos == input.length) result else null
    }

    private fun parseAddSub(): Double {
        var left = parseMulDiv()
        while (pos < input.length) {
            skipWhitespace()
            val op = when {
                pos < input.length && input[pos] == '+' -> '+'
                pos < input.length && input[pos] == '-' -> '-'
                else -> break
            }
            pos++
            skipWhitespace()
            val right = parseMulDiv()
            left = if (op == '+') left + right else left - right
        }
        return left
    }

    private fun parseMulDiv(): Double {
        var left = parseUnary()
        while (pos < input.length) {
            skipWhitespace()
            val op = when {
                pos < input.length && input[pos] == '*' -> '*'
                pos < input.length && input[pos] == '/' -> '/'
                else -> break
            }
            pos++
            skipWhitespace()
            val right = parseUnary()
            left = if (op == '*') left * right else left / right
        }
        return left
    }

    private fun parseUnary(): Double {
        skipWhitespace()
        return when {
            pos < input.length && input[pos] == '-' -> { pos++; -parsePrimary() }
            pos < input.length && input[pos] == '+' -> { pos++; parsePrimary() }
            else -> parsePrimary()
        }
    }

    private fun parsePrimary(): Double {
        skipWhitespace()
        if (pos >= input.length) error("Unexpected end of expression")

        if (input[pos] == '(') {
            pos++
            val result = parseAddSub()
            skipWhitespace()
            if (pos >= input.length || input[pos] != ')') error("Missing closing parenthesis")
            pos++
            return result
        }

        if (input[pos].isUpperCase()) {
            val start = pos
            while (pos < input.length && input[pos].isLetter()) pos++
            while (pos < input.length && input[pos].isDigit()) pos++
            val token = input.substring(start, pos)
            val ref = FormulaEvaluator.parseCellRef(token)
                ?: error("Invalid cell reference: $token")
            val (row, col) = ref
            return rows.getOrNull(row)?.getOrNull(col)?.toDoubleOrNull() ?: 0.0
        }

        if (input[pos].isDigit() || input[pos] == '.') {
            val start = pos
            while (pos < input.length && (input[pos].isDigit() || input[pos] == '.')) pos++
            return input.substring(start, pos).toDouble()
        }

        error("Unexpected character: '${input[pos]}'")
    }

    private fun skipWhitespace() {
        while (pos < input.length && input[pos].isWhitespace()) pos++
    }
}
