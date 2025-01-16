package dev.furq.holodisplays.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.gui.DeleteConfirmation
import dev.furq.holodisplays.gui.MainMenu
import dev.furq.holodisplays.utils.CommandUtils.playErrorSound
import dev.furq.holodisplays.utils.ErrorMessages
import dev.furq.holodisplays.utils.ErrorMessages.ErrorType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

object DeleteCommand {
    fun register(): LiteralArgumentBuilder<ServerCommandSource> = CommandManager.literal("delete")
        .then(
            CommandManager.literal("hologram")
                .then(
                    CommandManager.argument("name", StringArgumentType.word())
                        .executes { context -> executeHologram(context) }
                )
        )
        .then(
            CommandManager.literal("display")
                .then(
                    CommandManager.argument("name", StringArgumentType.word())
                        .executes { context -> executeDisplay(context) }
                )
        )

    private fun executeHologram(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        if (!HologramConfig.exists(name)) {
            ErrorMessages.sendError(context.source, ErrorType.HOLOGRAM_NOT_FOUND)
            playErrorSound(context.source)
            return 0
        }

        DeleteConfirmation.open(context.source.playerOrThrow, name, "hologram") {
            MainMenu.openMainMenu(context.source.playerOrThrow)
        }
        return 1
    }

    private fun executeDisplay(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        if (!DisplayConfig.exists(name)) {
            ErrorMessages.sendError(context.source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(context.source)
            return 0
        }

        DeleteConfirmation.open(context.source.playerOrThrow, name, "display") {
            MainMenu.openMainMenu(context.source.playerOrThrow)
        }
        return 1
    }
}