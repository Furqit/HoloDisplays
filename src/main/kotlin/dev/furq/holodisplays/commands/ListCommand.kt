package dev.furq.holodisplays.commands

import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.furq.holodisplays.gui.DisplayList
import dev.furq.holodisplays.gui.HologramList
import dev.furq.holodisplays.utils.CommandUtils.requirePlayer
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

object ListCommand {
    fun registerDisplays(): ArgumentBuilder<CommandSourceStack, *> = Commands
        .literal("list")
        .executes { context -> executeDisplays(context) }

    fun registerHolograms(): ArgumentBuilder<CommandSourceStack, *> = Commands
        .literal("list")
        .executes { context -> executeHolograms(context) }

    fun executeDisplays(context: CommandContext<CommandSourceStack>): Int {
        return requirePlayer(context)?.let { player ->
            DisplayList.open(player)
            1
        } ?: 0
    }

    fun executeHolograms(context: CommandContext<CommandSourceStack>): Int {
        return requirePlayer(context)?.let { player ->
            HologramList.open(player)
            1
        } ?: 0
    }
}