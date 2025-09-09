package dev.furq.holodisplays.utils

import eu.pb4.placeholders.api.PlaceholderContext
import eu.pb4.placeholders.api.Placeholders
import eu.pb4.placeholders.api.parsers.NodeParser
import eu.pb4.placeholders.api.parsers.TagParser
import net.minecraft.server.network.ServerPlayerEntity

object ConditionEvaluator {
    private val operatorRegex = "\\s(=|!=|>|<|>=|<=|contains|!contains|startsWith|endsWith)\\s".toRegex()
    private val placeholderParser by lazy {
        NodeParser.merge(TagParser.DEFAULT, Placeholders.DEFAULT_PLACEHOLDER_PARSER)
    }

    fun evaluate(condition: String?, player: ServerPlayerEntity): Boolean {
        condition ?: return true

        val (placeholder, operator, value) = parseCondition(condition) ?: return true
        val resolvedValue = resolvePlaceholder(placeholder.trim(), player)

        return when (operator.trim()) {
            "=" -> resolvedValue == value
            "!=" -> resolvedValue != value
            ">" -> compareNumbers(resolvedValue, value) { a, b -> a > b }
            "<" -> compareNumbers(resolvedValue, value) { a, b -> a < b }
            ">=" -> compareNumbers(resolvedValue, value) { a, b -> a >= b }
            "<=" -> compareNumbers(resolvedValue, value) { a, b -> a <= b }
            "contains" -> value in resolvedValue
            "!contains" -> value !in resolvedValue
            "startsWith" -> resolvedValue.startsWith(value)
            "endsWith" -> resolvedValue.endsWith(value)
            else -> true
        }
    }

    private inline fun compareNumbers(left: String, right: String, comparator: (Double, Double) -> Boolean): Boolean {
        val leftNum = left.toDoubleOrNull() ?: return false
        val rightNum = right.toDoubleOrNull() ?: return false
        return comparator(leftNum, rightNum)
    }

    private fun parseCondition(condition: String): Triple<String, String, String>? {
        val match = operatorRegex.find(condition) ?: return null
        val operator = match.groupValues[1]
        val parts = condition.split(" $operator ")
        return if (parts.size == 2) Triple(parts[0], operator, parts[1]) else null
    }

    private fun resolvePlaceholder(placeholder: String, player: ServerPlayerEntity): String =
        placeholderParser.parseNode(placeholder).toText(PlaceholderContext.of(player)).string
}