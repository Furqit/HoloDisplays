package dev.furq.holodisplays.commands

import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.furq.holodisplays.gui.DisplayList
import dev.furq.holodisplays.gui.HologramList
import dev.furq.holodisplays.utils.CommandUtils.requirePlayer
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
        return requirePlayer(context)?.let { player ->
            DisplayList.open(player)
            1
        } ?: 0
    }

    fun executeHolograms(context: CommandContext<ServerCommandSource>): Int {
        return requirePlayer(context)?.let { player ->
            HologramList.open(player)
            1
        } ?: 0
    }
}