package dev.furq.holodisplays.utils

import eu.pb4.placeholders.api.PlaceholderContext
import eu.pb4.placeholders.api.Placeholders
import eu.pb4.placeholders.api.node.TextNode
import eu.pb4.placeholders.api.parsers.NodeParser
import eu.pb4.placeholders.api.parsers.TagParser
import net.minecraft.server.level.ServerPlayer
//? if >=26.1
import eu.pb4.placeholders.api.ServerPlaceholderContext

object ConditionEvaluator {
    private const val MAX_CONDITION_CACHE_SIZE = 256
    private const val MAX_PLACEHOLDER_NODE_CACHE_SIZE = 128
    private val operatorRegex = "\\s(=|!=|>|<|>=|<=|contains|!contains|startsWith|endsWith)\\s".toRegex()
    private val parsedConditionCache = object : LinkedHashMap<String, Triple<String, String, String>?>(MAX_CONDITION_CACHE_SIZE, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Triple<String, String, String>?>): Boolean {
            return size > MAX_CONDITION_CACHE_SIZE
        }
    }
    private val placeholderParser by lazy {
        //~ if >=26.1 'DEFAULT_PLACEHOLDER_PARSER' -> 'SERVER_PLACEHOLDER_PARSER'
        NodeParser.merge(TagParser.DEFAULT, Placeholders.SERVER_PLACEHOLDER_PARSER)
    }
    private val placeholderNodeCache = object : LinkedHashMap<String, TextNode>(MAX_PLACEHOLDER_NODE_CACHE_SIZE, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, TextNode>): Boolean {
            return size > MAX_PLACEHOLDER_NODE_CACHE_SIZE
        }
    }

    fun evaluate(condition: String?, player: ServerPlayer): Boolean {
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

    fun parseCondition(condition: String): Triple<String, String, String>? {
        if (parsedConditionCache.containsKey(condition)) {
            return parsedConditionCache[condition]
        }
        val match = operatorRegex.find(condition)
        if (match == null) {
            parsedConditionCache[condition] = null
            return null
        }
        val operator = match.groupValues[1]
        val parts = condition.split(" $operator ")
        val result = if (parts.size == 2) Triple(parts[0], operator, parts[1]) else null
        parsedConditionCache[condition] = result
        return result
    }

    private fun resolvePlaceholder(placeholder: String, player: ServerPlayer): String {
        val node = placeholderNodeCache.getOrPut(placeholder) { placeholderParser.parseNode(placeholder) }
        //~ if >=26.1 'toText(PlaceholderContext.of(player)).string' -> 'toComponent(ServerPlaceholderContext.of(player)).string'
        return node.toComponent(ServerPlaceholderContext.of(player)).string
    }
}