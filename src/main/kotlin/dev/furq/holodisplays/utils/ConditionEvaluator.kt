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
        if (condition.isNullOrBlank()) return true

        val parts = parseCondition(condition) ?: return true
        val (rawPlaceholder, rawOperator, rawValue) = parts

        val placeholder = rawPlaceholder.trim()
        val operator = rawOperator.trim()
        val value = rawValue.trim()

        val resolved = resolvePlaceholder(placeholder, player)

        // Parse numéricos solo una vez
        val resolvedNum = resolved.toDoubleOrNull()
        val valueNum = value.toDoubleOrNull()

        return when (operator) {
            "="  -> resolved == value
            "!=" -> resolved != value
            ">"  -> resolvedNum != null && valueNum != null && resolvedNum > valueNum
            "<"  -> resolvedNum != null && valueNum != null && resolvedNum < valueNum
            ">=" -> resolvedNum != null && valueNum != null && resolvedNum >= valueNum
            "<=" -> resolvedNum != null && valueNum != null && resolvedNum <= valueNum
            "contains"    -> resolved.contains(value)
            "!contains"   -> !resolved.contains(value)
            "startsWith"  -> resolved.startsWith(value)
            "endsWith"    -> resolved.endsWith(value)
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