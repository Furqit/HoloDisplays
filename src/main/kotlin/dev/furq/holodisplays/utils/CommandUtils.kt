package dev.furq.holodisplays.utils

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.managers.FeedbackManager
import net.minecraft.commands.CommandSourceStack
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.ServerPlayer
import java.util.concurrent.CompletableFuture

object CommandUtils {

    fun suggestHolograms(builder: SuggestionsBuilder): CompletableFuture<Suggestions> =
        builder.apply { HologramConfig.getHolograms().keys.forEach(::suggest) }.buildFuture()

    fun suggestDisplays(builder: SuggestionsBuilder): CompletableFuture<Suggestions> =
        builder.apply { DisplayConfig.getDisplays().keys.forEach(::suggest) }.buildFuture()

    fun suggestItemIds(builder: SuggestionsBuilder): CompletableFuture<Suggestions> =
        builder.apply {
            BuiltInRegistries.ITEM.keySet().forEach { suggest(it.toString()) }
        }.buildFuture()

    fun suggestBlockIds(builder: SuggestionsBuilder): CompletableFuture<Suggestions> =
        builder.apply {
            BuiltInRegistries.BLOCK.keySet().forEach { suggest(it.toString()) }
        }.buildFuture()

    fun suggestEntityIds(builder: SuggestionsBuilder): CompletableFuture<Suggestions> =
        builder.apply {
            BuiltInRegistries.ENTITY_TYPE.keySet().forEach { suggest(it.toString()) }
        }.buildFuture()

    fun requirePlayer(context: CommandContext<CommandSourceStack>): ServerPlayer? {
        return context.source.player ?: run {
            FeedbackManager.send(context.source, FeedbackType.PLAYER_ONLY)
            null
        }
    }
}