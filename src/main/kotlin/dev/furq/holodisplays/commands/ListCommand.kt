package dev.furq.holodisplays.commands

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.furq.holodisplays.gui.HologramList
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

object ListCommand {
    fun register(): LiteralArgumentBuilder<ServerCommandSource> = CommandManager.literal("list")
        .then(
            CommandManager.literal("hologram")
                .then(CommandManager.argument("page", IntegerArgumentType.integer(1))
                    .executes { context -> executeHolograms(context) }
                )
                .executes { context -> executeHolograms(context) }
        )
        .then(CommandManager.literal("display")
            .then(CommandManager.argument("page", IntegerArgumentType.integer(1))
                .executes { context -> executeDisplays(context) }
            )
            .executes { context -> executeDisplays(context) }
        )

    private fun executeHolograms(context: CommandContext<ServerCommandSource>): Int {
        HologramList.open(context.source.playerOrThrow)
        return 1
    }

    private fun executeDisplays(context: CommandContext<ServerCommandSource>): Int {
        HologramList.open(context.source.playerOrThrow)
        return 1
    }
}