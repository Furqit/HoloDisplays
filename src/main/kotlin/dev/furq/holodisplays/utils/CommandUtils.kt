package dev.furq.holodisplays.utils

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.managers.FeedbackManager
import net.minecraft.registry.Registries
import net.minecraft.server.command.ServerCommandSource
import java.util.concurrent.CompletableFuture

object CommandUtils {
    fun suggestHolograms(builder: SuggestionsBuilder): CompletableFuture<Suggestions> =
        builder.apply { HologramConfig.getHolograms().keys.forEach(::suggest) }.buildFuture()

    fun suggestDisplays(builder: SuggestionsBuilder): CompletableFuture<Suggestions> =
        builder.apply { DisplayConfig.getDisplays().keys.forEach(::suggest) }.buildFuture()

    fun suggestItemIds(builder: SuggestionsBuilder): CompletableFuture<Suggestions> =
        builder.apply { Registries.ITEM.ids.forEach { suggest(it.toString()) } }.buildFuture()

    fun suggestBlockIds(builder: SuggestionsBuilder): CompletableFuture<Suggestions> =
        builder.apply { Registries.BLOCK.ids.forEach { suggest(it.toString()) } }.buildFuture()

    fun suggestEntityIds(builder: SuggestionsBuilder): CompletableFuture<Suggestions> =
        builder.apply { Registries.ENTITY_TYPE.ids.forEach { suggest(it.toString()) } }.buildFuture()

    fun requirePlayer(context: CommandContext<ServerCommandSource>): net.minecraft.server.network.ServerPlayerEntity? {
        return context.source.player ?: run {
            FeedbackManager.send(context.source, FeedbackType.PLAYER_ONLY)
            null
        }
    }
}