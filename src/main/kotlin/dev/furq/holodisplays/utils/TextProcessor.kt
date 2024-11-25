package dev.furq.holodisplays.utils

import dev.furq.holodisplays.HoloDisplays
import dev.furq.holodisplays.config.AnimationConfig
import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.data.DisplayData
import dev.furq.holodisplays.data.HologramData
import dev.furq.holodisplays.handlers.PacketHandler
import dev.furq.holodisplays.handlers.ViewerHandler
import eu.pb4.placeholders.api.PlaceholderContext
import eu.pb4.placeholders.api.Placeholders
import eu.pb4.placeholders.api.TextParserUtils
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

object TextProcessor {
    private const val ANIMATION_PATTERN = "<animation:([^>]+)>"
    private val animationRegex = ANIMATION_PATTERN.toRegex()
    private var ticks = 0
    private val processedAnimationCache = mutableMapOf<Pair<String, Int>, String>()

    fun init() = clearCache()

    fun clearCache() {
        processedAnimationCache.clear()
    }

    fun tick() {
        ticks++
    }

    private fun processAnimations(text: String): String =
        animationRegex.replace(text) { match ->
            val animationName = match.groupValues[1]
            val animation = AnimationConfig.getAnimation(animationName)
            val cacheKey = animationName to (ticks / (animation?.interval ?: 1))

            processedAnimationCache.getOrPut(cacheKey) {
                getNextTextFrame(animationName, ticks)
                    ?: "<red>Invalid animation: $animationName</red>"
            }
        }

    private fun getNextTextFrame(animationName: String, currentTick: Int): String? {
        val animation = AnimationConfig.getAnimation(animationName) ?: return null
        if (animation.frames.isEmpty()) return null

        val currentIndex = (currentTick / animation.interval) % animation.frames.size
        return animation.frames[currentIndex].text
    }

    fun updateAnimations() {
        val holograms = HologramConfig.getHolograms()
        if (holograms.isEmpty()) return

        holograms.forEach { (name, hologram) ->
            if (ViewerHandler.getObserverCount(name) == 0) return@forEach

            updateHologramAnimations(name, hologram)
        }
    }

    private fun updateHologramAnimations(name: String, hologram: HologramData) {
        hologram.displays.forEachIndexed { index, entity ->
            val display = DisplayConfig.getDisplay(entity.getReference()) ?: return@forEachIndexed
            if (display.displayType !is DisplayData.DisplayType.Text) return@forEachIndexed

            val text = display.displayType.lines.joinToString("\n")
            if (!shouldUpdate(text, hologram.updateRate)) return@forEachIndexed

            updateDisplayForObservers(name, entity.getReference(), index, text)
        }
    }

    private fun shouldUpdate(text: String, updateRate: Int): Boolean {
        val hasAnimation = animationRegex.containsMatchIn(text)
        val hasPlaceholder = text.contains("%")

        return when {
            hasAnimation -> {
                val animationIntervals = findAnimationIntervals(text)
                animationIntervals.any { interval -> ticks % interval == 0 }
            }

            hasPlaceholder -> ticks % updateRate == 0
            else -> false
        }
    }

    private fun findAnimationIntervals(text: String): List<Int> =
        animationRegex.findAll(text)
            .mapNotNull { match ->
                val animationName = match.groupValues[1]
                AnimationConfig.getAnimation(animationName)?.interval
            }
            .toList()

    private fun updateDisplayForObservers(name: String, displayRef: String, index: Int, text: String) {
        HoloDisplays.SERVER?.playerManager?.playerList
            ?.filter { ViewerHandler.isViewing(it, name) }
            ?.forEach { player ->
                val processedText = processText(text, player)
                PacketHandler.updateTextMetadata(player, name, displayRef, index, processedText)
            }
    }

    fun processText(text: String, player: ServerPlayerEntity): Text {
        val processedAnimations = processAnimations(text)
        val formattedText = Placeholders.parseText(
            Text.literal(processedAnimations),
            PlaceholderContext.of(player)
        )
        return TextParserUtils.formatText(formattedText.string)
    }
}