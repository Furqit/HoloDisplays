package dev.furq.holodisplays.utils

import eu.pb4.placeholders.api.PlaceholderContext
import eu.pb4.placeholders.api.Placeholders
import eu.pb4.placeholders.api.parsers.NodeParser
import eu.pb4.placeholders.api.parsers.TagParser
import net.minecraft.server.network.ServerPlayerEntity

object ConditionEvaluator {
    private val OPERATORS = listOf("=", "!=", ">", "<", ">=", "<=", "contains", "!contains", "startsWith", "endsWith")
    private val placeholderParser by lazy {
        NodeParser.merge(TagParser.DEFAULT, Placeholders.DEFAULT_PLACEHOLDER_PARSER)
    }

    fun evaluate(condition: String?, player: ServerPlayerEntity): Boolean {
        if (condition == null) {
            return true
        }

        val parts = parseCondition(condition) ?: return true
        val (placeholder, operator, value) = parts
        val resolvedPlaceholder = resolvePlaceholder(placeholder.trim(), player)

        return when (operator.trim()) {
            "=" -> resolvedPlaceholder == value
            "!=" -> resolvedPlaceholder != value
            ">" -> resolvedPlaceholder.toDoubleOrNull()?.let {
                it > (value.toDoubleOrNull() ?: return false)
            } ?: false

            "<" -> resolvedPlaceholder.toDoubleOrNull()
                ?.let { it < (value.toDoubleOrNull() ?: return false) } ?: false

            ">=" -> resolvedPlaceholder.toDoubleOrNull()
                ?.let { it >= (value.toDoubleOrNull() ?: return false) } ?: false

            "<=" -> resolvedPlaceholder.toDoubleOrNull()
                ?.let { it <= (value.toDoubleOrNull() ?: return false) } ?: false

            "contains" -> resolvedPlaceholder.contains(value)
            "!contains" -> !resolvedPlaceholder.contains(value)
            "startsWith" -> resolvedPlaceholder.startsWith(value)
            "endsWith" -> resolvedPlaceholder.endsWith(value)
            else -> true
        }
    }

    private fun parseCondition(condition: String): Triple<String, String, String>? {
        val operator = OPERATORS.find { condition.contains(" $it ") } ?: return null
        val parts = condition.split(" $operator ")
        if (parts.size != 2) {
            return null
        }
        return Triple(parts[0], operator, parts[1])
    }

    private fun resolvePlaceholder(placeholder: String, player: ServerPlayerEntity): String {
        val node = placeholderParser.parseNode(placeholder)
        return node.toText(PlaceholderContext.of(player)).string
    }
}