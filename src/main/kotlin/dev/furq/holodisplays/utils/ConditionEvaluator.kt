package dev.furq.holodisplays.utils

import dev.furq.holodisplays.handlers.ErrorHandler
import eu.pb4.placeholders.api.PlaceholderContext
import eu.pb4.placeholders.api.Placeholders
import eu.pb4.placeholders.api.parsers.NodeParser
import eu.pb4.placeholders.api.parsers.TagParser
import net.minecraft.server.network.ServerPlayerEntity

object ConditionEvaluator {
    private val OPERATORS = listOf("=", "!=", ">", "<", ">=", "<=", "contains", "!contains", "startsWith", "endsWith")
    private val parser = NodeParser.merge(TagParser.DEFAULT, Placeholders.DEFAULT_PLACEHOLDER_PARSER)

    fun evaluate(condition: String?, player: ServerPlayerEntity): Boolean = ErrorHandler.withCatch<Boolean> {
        if (condition == null) {
            return@withCatch true
        }

        val parts = parseCondition(condition) ?: return@withCatch true
        val (placeholder, operator, value) = parts
        val resolvedPlaceholder = resolvePlaceholder(placeholder.trim(), player)

        when (operator.trim()) {
            "=" -> resolvedPlaceholder == value
            "!=" -> resolvedPlaceholder != value
            ">" -> resolvedPlaceholder.toDoubleOrNull()
                ?.let { it > (value.toDoubleOrNull() ?: return@withCatch false) } ?: false

            "<" -> resolvedPlaceholder.toDoubleOrNull()
                ?.let { it < (value.toDoubleOrNull() ?: return@withCatch false) } ?: false

            ">=" -> resolvedPlaceholder.toDoubleOrNull()
                ?.let { it >= (value.toDoubleOrNull() ?: return@withCatch false) } ?: false

            "<=" -> resolvedPlaceholder.toDoubleOrNull()
                ?.let { it <= (value.toDoubleOrNull() ?: return@withCatch false) } ?: false

            "contains" -> resolvedPlaceholder.contains(value)
            "!contains" -> !resolvedPlaceholder.contains(value)
            "startsWith" -> resolvedPlaceholder.startsWith(value)
            "endsWith" -> resolvedPlaceholder.endsWith(value)
            else -> true
        }
    } ?: true

    private fun parseCondition(condition: String): Triple<String, String, String>? =
        ErrorHandler.withCatch<Triple<String, String, String>?> {
            val operator = OPERATORS.find { condition.contains(" $it ") } ?: return@withCatch null
            val parts = condition.split(" $operator ")
            if (parts.size != 2) {
                return@withCatch null
            }
            Triple(parts[0], operator, parts[1])
        }

    private fun resolvePlaceholder(placeholder: String, player: ServerPlayerEntity): String =
        ErrorHandler.withCatch<String> {
            val node = parser.parseNode(placeholder)
            node.toText(PlaceholderContext.of(player)).string
        } ?: placeholder
}