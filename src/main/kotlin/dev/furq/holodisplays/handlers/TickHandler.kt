package dev.furq.holodisplays.handlers

import dev.furq.holodisplays.api.HoloDisplaysAPIInternal
import dev.furq.holodisplays.config.AnimationConfig
import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.data.HologramData
import dev.furq.holodisplays.data.display.TextDisplay
import eu.pb4.placeholders.api.PlaceholderContext
import eu.pb4.placeholders.api.Placeholders
import eu.pb4.placeholders.api.node.TextNode
import eu.pb4.placeholders.api.parsers.NodeParser
import eu.pb4.placeholders.api.parsers.TagParser
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBundlePacket
//? if >=26.1
import eu.pb4.placeholders.api.ServerPlaceholderContext
import net.minecraft.server.level.ServerPlayer

object TickHandler {
    private var ticks = 0
    private const val MAX_ANIMATION_CACHE_SIZE = 256
    private const val MAX_TEXT_CACHE_SIZE = 512
    private const val MAX_PLACEHOLDER_NODE_CACHE_SIZE = 128
    private val animationCache = object : LinkedHashMap<Pair<String, Int>, String>(MAX_ANIMATION_CACHE_SIZE, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Pair<String, Int>, String>): Boolean {
            return size > MAX_ANIMATION_CACHE_SIZE
        }
    }
    private val placeholderParser by lazy {
        //~ if >=26.1 'DEFAULT_PLACEHOLDER_PARSER' -> 'SERVER_PLACEHOLDER_PARSER'
        NodeParser.merge(TagParser.DEFAULT, Placeholders.SERVER_PLACEHOLDER_PARSER)
    }
    private val animationRegex = "<animation:([^>]+)>".toRegex()
    private val textCache = object : LinkedHashMap<String, CachedTextInfo>(MAX_TEXT_CACHE_SIZE, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, CachedTextInfo>): Boolean {
            return size > MAX_TEXT_CACHE_SIZE
        }
    }
    private val placeholderNodeCache = object : LinkedHashMap<String, TextNode>(MAX_PLACEHOLDER_NODE_CACHE_SIZE, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, TextNode>): Boolean {
            return size > MAX_PLACEHOLDER_NODE_CACHE_SIZE
        }
    }

    data class CachedTextInfo(
        val hasAnimation: Boolean,
        val hasPlaceholder: Boolean,
        val animationIntervals: List<Int>? = null,
        val hasUnknownAnimation: Boolean = false
    )

    fun init() {
        animationCache.clear()
        textCache.clear()
        ticks = 0
    }

    fun tick(players: List<ServerPlayer>) {
        processHolograms(players)
        ticks++
    }

    private fun processHolograms(players: List<ServerPlayer>) {
        val bundled = mutableMapOf<ServerPlayer, MutableList<Packet<in ClientGamePacketListener>>>()
        HologramConfig.getHolograms().forEach { (name, hologram) ->
            if (ViewerHandler.getObserverCount(name) == 0) return@forEach
            processHologramDisplays(name, hologram, players, bundled)
        }

        HoloDisplaysAPIInternal.forEachApiHologramUnchecked { name, hologram ->
            if (ViewerHandler.getObserverCount(name) == 0) return@forEachApiHologramUnchecked
            processHologramDisplays(name, hologram, players, bundled)
        }
        bundled.forEach { (player, packets) ->
            if (packets.isNotEmpty()) {
                player.connection.send(ClientboundBundlePacket(packets))
            }
        }
    }

    private fun processHologramDisplays(name: String, hologram: HologramData, players: List<ServerPlayer>, bundled: MutableMap<ServerPlayer, MutableList<Packet<in ClientGamePacketListener>>>) {
        var viewers: List<ServerPlayer>? = null
        hologram.displays.forEachIndexed { index, displayLine ->
            val display = DisplayConfig.getDisplayOrAPI(displayLine.name)?.type as? TextDisplay
                ?: return@forEachIndexed

            val text = display.getText()
            if (!shouldUpdateDisplay(text, hologram.updateRate)) return@forEachIndexed

            if (viewers == null) {
                viewers = players.filter { ViewerHandler.isViewing(it, name) }
            }
            val hologramViewers = viewers!!
            if (hologramViewers.isEmpty()) return

            val animated = processAnimations(text)
            hologramViewers.forEach { player ->
                val processedText = processPlaceholders(animated, player)
                PacketHandler.buildTextUpdatePacket(player, name, displayLine.name, index, processedText)?.let { packet ->
                    bundled.getOrPut(player) { mutableListOf() }.add(packet)
                }
            }
        }
    }

    private fun shouldUpdateDisplay(text: String, updateRate: Int): Boolean {
        val info = textCache.getOrPut(text) {
            val hasAnimation = animationRegex.containsMatchIn(text)
            val intervals: List<Int>?
            val unknown: Boolean
            if (hasAnimation) {
                val found = findAnimationIntervals(text)
                intervals = found.first
                unknown = found.second
            } else {
                intervals = null
                unknown = false
            }
            CachedTextInfo(hasAnimation, hasDynamicPlaceholders(text), intervals, unknown)
        }

        val rate = if (updateRate <= 0) 20 else updateRate
        return when {
            info.hasAnimation && info.animationIntervals?.any { interval -> interval > 0 && ticks % interval == 0 } == true -> true
            info.hasUnknownAnimation && ticks % rate == 0 -> true
            info.hasPlaceholder && ticks % rate == 0 -> true

            else -> false
        }
    }

    private fun processAnimations(text: String): String =
        animationRegex.replace(text) { match ->
            val animationName = match.groupValues[1]
            val animation = AnimationConfig.getAnimation(animationName)
            val safeInterval = animation?.interval?.takeIf { it > 0 } ?: 1
            val cacheKey = animationName to ticks / safeInterval

            animationCache.getOrPut(cacheKey) {
                getAnimationFrame(animationName, ticks)
                    ?: "<red>Invalid animation: $animationName</red>"
            }
        }

    private fun getAnimationFrame(animationName: String, currentTick: Int): String? {
        val animation = AnimationConfig.getAnimation(animationName) ?: return null
        if (animation.frames.isEmpty()) return null
        val safeInterval = if (animation.interval <= 0) 1 else animation.interval

        val currentIndex = (currentTick / safeInterval) % animation.frames.size
        return animation.frames[currentIndex]
    }

    private fun findAnimationIntervals(text: String): Pair<List<Int>, Boolean> {
        var unknown = false
        val intervals = animationRegex.findAll(text)
            .mapNotNull { match ->
                val animationName = match.groupValues[1]
                AnimationConfig.getAnimation(animationName)?.interval?.takeIf { it > 0 }
                    ?: run { unknown = true; null }
            }
            .toList()
        return intervals to unknown
    }

    private fun placeholderNode(text: String): TextNode =
        placeholderNodeCache.getOrPut(text) { placeholderParser.parseNode(text) }

    private fun hasDynamicPlaceholders(text: String): Boolean {
        val stripped = if (animationRegex.containsMatchIn(text)) animationRegex.replace(text, "") else text
        return placeholderNode(stripped).isDynamic
    }

    private fun processPlaceholders(text: String, player: ServerPlayer): Component {
        return resolveNode(placeholderNode(text), player)
    }

    private fun resolveNode(node: TextNode, player: ServerPlayer): Component {
        //~ if >=26.1 'toText(PlaceholderContext.of(player))' -> 'toComponent(ServerPlaceholderContext.of(player))'
        return node.toComponent(ServerPlaceholderContext.of(player))
    }

    fun processText(text: String, player: ServerPlayer): Component {
        val processedAnimations = processAnimations(text)
        return processPlaceholders(processedAnimations, player)
    }
}