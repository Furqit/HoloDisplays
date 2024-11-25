package dev.furq.holodisplays.utils

import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.config.HologramConfig
import net.minecraft.registry.Registries
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import java.util.concurrent.CompletableFuture

object CommandUtils {
    fun suggestHolograms(builder: SuggestionsBuilder): CompletableFuture<Suggestions> = builder.apply {
        HologramConfig.getHolograms().keys.forEach(::suggest)
    }.buildFuture()

    fun suggestDisplays(builder: SuggestionsBuilder): CompletableFuture<Suggestions> = builder.apply {
        DisplayConfig.getDisplays().keys.forEach(::suggest)
    }.buildFuture()

    fun suggestItemIds(builder: SuggestionsBuilder): CompletableFuture<Suggestions> = builder.apply {
        Registries.ITEM.ids.forEach { suggest(it.toString()) }
    }.buildFuture()

    fun suggestBlockIds(builder: SuggestionsBuilder): CompletableFuture<Suggestions> = builder.apply {
        Registries.BLOCK.ids.forEach { suggest(it.toString()) }
    }.buildFuture()

    fun playErrorSound(source: ServerCommandSource) {
        source.player?.playSoundToPlayer(
            SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(),
            SoundCategory.MASTER,
            1f,
            0.5f
        )
    }

    fun playSuccessSound(source: ServerCommandSource) {
        source.player?.playSoundToPlayer(
            SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
            SoundCategory.MASTER,
            0.8f,
            1f
        )
    }
}