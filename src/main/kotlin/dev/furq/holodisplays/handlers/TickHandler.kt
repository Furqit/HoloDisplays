package dev.furq.holodisplays.handlers

import dev.furq.holodisplays.HoloDisplays
import dev.furq.holodisplays.api.HoloDisplaysAPIImpl
import dev.furq.holodisplays.config.AnimationConfig
import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.data.HologramData
import dev.furq.holodisplays.data.display.TextDisplay
import eu.pb4.placeholders.api.PlaceholderContext
import eu.pb4.placeholders.api.Placeholders
import eu.pb4.placeholders.api.parsers.NodeParser
import eu.pb4.placeholders.api.parsers.TagParser
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

object TickHandler {
    private var ticks = 0
    private val animationCache = mutableMapOf<Pair<String, Int>, String>()
    private val placeholderParser by lazy {
        NodeParser.merge(TagParser.DEFAULT, Placeholders.DEFAULT_PLACEHOLDER_PARSER)
    }
    private val animationRegex = "<animation:([^>]+)>".toRegex()
    private val placeholderRegex = "%([^%:]+):([^%]+)%".toRegex()
    private val textCache = mutableMapOf<String, CachedTextInfo>()

    data class CachedTextInfo(
        val hasAnimation: Boolean,
        val hasPlaceholder: Boolean,
        val animationIntervals: List<Int>? = null
    )

    fun init() {
        animationCache.clear()
        ticks = 0
    }

    fun tick() {
        if (shouldProcessTick()) {
            processHolograms()
        }
        ticks++
    }

    private fun shouldProcessTick(): Boolean {
        return HoloDisplays.SERVER?.playerManager?.playerList?.isNotEmpty() == true &&
                (HologramConfig.getHolograms().isNotEmpty() || HoloDisplaysAPIImpl.INSTANCE.apiHolograms.isNotEmpty())
    }

    private fun processHolograms() {
        HologramConfig.getHolograms().forEach { (name, hologram) ->
            if (ViewerHandler.getObserverCount(name) == 0) return@forEach
            processHologramDisplays(name, hologram)
        }

        HoloDisplaysAPIImpl.INSTANCE.apiHolograms.forEach { (name, hologram) ->
            if (ViewerHandler.getObserverCount(name) == 0) return@forEach
            processHologramDisplays(name, hologram)
        }
    }

    private fun processHologramDisplays(name: String, hologram: HologramData) {
        hologram.displays.forEachIndexed { index, displayLine ->
            val display = DisplayConfig.getDisplayOrAPI(displayLine.displayId)?.type as? TextDisplay
                ?: return@forEachIndexed

            val text = display.getText()
            if (!shouldUpdateDisplay(text, hologram.updateRate)) return@forEachIndexed

            updateDisplayForViewers(name, displayLine.displayId, index, text)
        }
    }

    private fun shouldUpdateDisplay(text: String, updateRate: Int): Boolean {
        val info = textCache.getOrPut(text) {
            val hasAnimation = animationRegex.containsMatchIn(text)
            val hasPlaceholder = placeholderRegex.containsMatchIn(text)
            val intervals = if (hasAnimation) findAnimationIntervals(text) else null
            CachedTextInfo(hasAnimation, hasPlaceholder, intervals)
        }

        return when {
            info.hasAnimation -> info.animationIntervals?.any { interval -> ticks % interval == 0 } ?: false
            info.hasPlaceholder -> ticks % (if (updateRate <= 0) 20 else updateRate) == 0

            else -> false
        }
    }

    private fun processAnimations(text: String): String =
        animationRegex.replace(text) { match ->
            val animationName = match.groupValues[1]
            val animation = AnimationConfig.getAnimation(animationName)
            val cacheKey = animationName to ticks / (animation?.interval ?: 1)

            animationCache.getOrPut(cacheKey) {
                getAnimationFrame(animationName, ticks)
                    ?: "<red>Invalid animation: $animationName</red>"
            }
        }

    private fun getAnimationFrame(animationName: String, currentTick: Int): String? {
        val animation = AnimationConfig.getAnimation(animationName) ?: return null
        if (animation.frames.isEmpty()) return null

        val currentIndex = (currentTick / animation.interval) % animation.frames.size
        return animation.frames[currentIndex].text
    }

    private fun findAnimationIntervals(text: String): List<Int> =
        animationRegex.findAll(text)
            .mapNotNull { match ->
                val animationName = match.groupValues[1]
                AnimationConfig.getAnimation(animationName)?.interval
            }
            .toList()

    private fun processPlaceholders(text: String, player: ServerPlayerEntity): Text {
        val node = placeholderParser.parseNode(text)
        return node.toText(PlaceholderContext.of(player))
    }

    private fun updateDisplayForViewers(name: String, displayRef: String, index: Int, text: String) {
        val viewers = HoloDisplays.SERVER?.playerManager?.playerList
            ?.filter { ViewerHandler.isViewing(it, name) }
            ?: return

        viewers.forEach { player ->
            val processedText = processText(text, player)
            PacketHandler.updateTextMetadata(player, name, displayRef, index, processedText)
        }
    }

    fun processText(text: String, player: ServerPlayerEntity): Text {
        val processedAnimations = processAnimations(text)
        return processPlaceholders(processedAnimations, player)
    }
}