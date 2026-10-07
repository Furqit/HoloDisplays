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

    private fun matchesPrefix(candidate: String, remaining: String): Boolean {
        if (remaining.isEmpty()) return true
        return candidate.startsWith(remaining, ignoreCase = true) ||
            candidate.substringAfter(':').startsWith(remaining.substringAfter(':'), ignoreCase = true)
    }

    fun suggestHolograms(builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val remaining = builder.remaining.lowercase()
        HologramConfig.getHolograms().keys.toList()
            .filter { matchesPrefix(it, remaining) }
            .forEach(builder::suggest)
        return builder.buildFuture()
    }

    fun suggestDisplays(builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val remaining = builder.remaining.lowercase()
        DisplayConfig.getDisplays().keys.toList()
            .filter { matchesPrefix(it, remaining) }
            .forEach(builder::suggest)
        return builder.buildFuture()
    }

    fun suggestItemIds(builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val remaining = builder.remaining.lowercase()
        BuiltInRegistries.ITEM.keySet()
            .asSequence()
            .map { it.toString() }
            .filter { matchesPrefix(it, remaining) }
            .forEach(builder::suggest)
        return builder.buildFuture()
    }

    fun suggestBlockIds(builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val remaining = builder.remaining.lowercase()
        BuiltInRegistries.BLOCK.keySet()
            .asSequence()
            .map { it.toString() }
            .filter { matchesPrefix(it, remaining) }
            .forEach(builder::suggest)
        return builder.buildFuture()
    }

    fun suggestEntityIds(builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val remaining = builder.remaining.lowercase()
        BuiltInRegistries.ENTITY_TYPE.keySet()
            .asSequence()
            .map { it.toString() }
            .filter { matchesPrefix(it, remaining) }
            .forEach(builder::suggest)
        return builder.buildFuture()
    }

    fun requirePlayer(context: CommandContext<CommandSourceStack>): ServerPlayer? {
        return context.source.player ?: run {
            FeedbackManager.send(context.source, FeedbackType.PLAYER_ONLY)
            null
        }
    }
}