package dev.furq.holodisplays.commands

import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.furq.holodisplays.gui.DisplayList
import dev.furq.holodisplays.gui.HologramList
import dev.furq.holodisplays.managers.FeedbackManager
import dev.furq.holodisplays.utils.FeedbackType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

object ListCommand {

    fun registerDisplays(): ArgumentBuilder<ServerCommandSource, *> = CommandManager
        .literal("list")
        .executes { context -> executeDisplays(context) }

    fun registerHolograms(): ArgumentBuilder<ServerCommandSource, *> = CommandManager
        .literal("list")
        .executes { context -> executeHolograms(context) }

    fun executeDisplays(context: CommandContext<ServerCommandSource>): Int {
        val player = context.source.player ?: run {
            FeedbackManager.send(context.source, FeedbackType.PLAYER_ONLY)
            return 0
        }
        DisplayList.open(player)
        return 1
    }

    fun executeHolograms(context: CommandContext<ServerCommandSource>): Int {
        val player = context.source.player ?: run {
            FeedbackManager.send(context.source, FeedbackType.PLAYER_ONLY)
            return 0
        }
        HologramList.open(player)
        return 1
    }
}