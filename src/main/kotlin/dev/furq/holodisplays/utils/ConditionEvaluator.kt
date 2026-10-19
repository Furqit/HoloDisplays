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
    private const val MAX_STATIC_RESULT_CACHE_SIZE = 256
    private val operatorRegex = "\\s(=|!=|>|<|>=|<=|contains|!contains|startsWith|endsWith)\\s".toRegex()

    private val NO_MATCH = Triple("", "", "")
    private val parsedConditionCache = object : LinkedHashMap<String, Triple<String, String, String>>(MAX_CONDITION_CACHE_SIZE, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Triple<String, String, String>>): Boolean =
            size > MAX_CONDITION_CACHE_SIZE
    }
    private val placeholderParser by lazy {
        //~ if >=26.1 'DEFAULT_PLACEHOLDER_PARSER' -> 'SERVER_PLACEHOLDER_PARSER'
        NodeParser.merge(TagParser.DEFAULT, Placeholders.SERVER_PLACEHOLDER_PARSER)
    }
    private val placeholderNodeCache = object : LinkedHashMap<String, TextNode>(MAX_PLACEHOLDER_NODE_CACHE_SIZE, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, TextNode>): Boolean =
            size > MAX_PLACEHOLDER_NODE_CACHE_SIZE
    }
    private val staticResultCache = object : LinkedHashMap<String, Boolean>(MAX_STATIC_RESULT_CACHE_SIZE, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Boolean>): Boolean =
            size > MAX_STATIC_RESULT_CACHE_SIZE
    }

    fun evaluate(condition: String?, player: ServerPlayer): Boolean {
        condition ?: return true

        val (placeholder, operator, value) = parseCondition(condition) ?: return true
        val trimmedPlaceholder = placeholder.trim()
        val node = nodeFor(trimmedPlaceholder)

        if ('%' !in trimmedPlaceholder && '<' !in trimmedPlaceholder && !node.isDynamic) {
            return staticResultCache.getOrPut(condition) {
                compareValues(operator.trim(), resolveNode(node, player), value)
            }
        }
        return compareValues(operator.trim(), resolveNode(node, player), value)
    }

    private fun nodeFor(text: String): TextNode =
        placeholderNodeCache.getOrPut(text) { placeholderParser.parseNode(text) }

    private fun resolveNode(node: TextNode, player: ServerPlayer): String {
        //~ if >=26.1 'toText(PlaceholderContext.of(player)).string' -> 'toComponent(ServerPlaceholderContext.of(player)).string'
        return node.toComponent(ServerPlaceholderContext.of(player)).string
    }

    private fun compareValues(operator: String, resolvedValue: String, value: String): Boolean = when (operator) {
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

    private inline fun compareNumbers(left: String, right: String, comparator: (Double, Double) -> Boolean): Boolean {
        val leftNum = left.toDoubleOrNull() ?: return false
        val rightNum = right.toDoubleOrNull() ?: return false
        return comparator(leftNum, rightNum)
    }

    fun parseCondition(condition: String): Triple<String, String, String>? {
        val cached = parsedConditionCache[condition]
        if (cached != null) return if (cached === NO_MATCH) null else cached
        val match = operatorRegex.find(condition)
        val result = if (match == null) NO_MATCH else {
            val operator = match.groupValues[1]
            val parts = condition.split(" $operator ")
            if (parts.size == 2) Triple(parts[0], operator, parts[1]) else NO_MATCH
        }
        parsedConditionCache[condition] = result
        return if (result === NO_MATCH) null else result
    }

    fun resetCaches() {
        parsedConditionCache.clear()
        placeholderNodeCache.clear()
        staticResultCache.clear()
    }
}
