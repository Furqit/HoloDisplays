package dev.furq.holodisplays.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

object EditCommand {
    fun register(): LiteralArgumentBuilder<ServerCommandSource> = CommandManager.literal("edit")
        .then(
            CommandManager.literal("hologram")
                .then(HologramEditCommand.register())
        )
        .then(
            CommandManager.literal("display")
                .then(DisplayEditCommand.register())
        )
}