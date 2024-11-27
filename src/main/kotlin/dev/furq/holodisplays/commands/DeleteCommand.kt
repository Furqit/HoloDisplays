package dev.furq.holodisplays.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.handlers.HologramHandler
import dev.furq.holodisplays.handlers.ViewerHandler
import dev.furq.holodisplays.menu.DeleteConfirmMenu
import dev.furq.holodisplays.utils.CommandUtils
import dev.furq.holodisplays.utils.CommandUtils.playErrorSound
import dev.furq.holodisplays.utils.CommandUtils.playSuccessSound
import dev.furq.holodisplays.utils.ErrorMessages
import dev.furq.holodisplays.utils.ErrorMessages.ErrorType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

object DeleteCommand {
    fun register(): LiteralArgumentBuilder<ServerCommandSource> = CommandManager.literal("delete")
        .then(CommandManager.literal("hologram")
            .then(CommandManager.argument("name", StringArgumentType.word())
                .suggests { _, builder -> CommandUtils.suggestHolograms(builder) }
                .executes { context -> executeDeleteHologram(context) }
                .then(CommandManager.literal("confirm")
                    .executes { context -> executeDeleteHologram(context, true) }
                )
            )
        )
        .then(CommandManager.literal("display")
            .then(CommandManager.argument("displayId", StringArgumentType.word())
                .suggests { _, builder -> CommandUtils.suggestDisplays(builder) }
                .executes { context -> executeDeleteDisplay(context) }
                .then(CommandManager.literal("confirm")
                    .executes { context -> executeDeleteDisplay(context, true) }
                )
            )
        )

    private fun executeDeleteHologram(context: CommandContext<ServerCommandSource>, confirm: Boolean = false): Int {
        val name = StringArgumentType.getString(context, "name")

        if (!HologramConfig.exists(name)) {
            ErrorMessages.sendError(context.source, ErrorType.HOLOGRAM_NOT_FOUND)
            playErrorSound(context.source)
            return 0
        }

        if (!confirm) {
            DeleteConfirmMenu.showHologram(context.source, name)
            return 1
        }

        HologramHandler.deleteHologram(name)
        playSuccessSound(context.source)
        return 1
    }

    private fun executeDeleteDisplay(context: CommandContext<ServerCommandSource>, confirm: Boolean = false): Int {
        val displayId = StringArgumentType.getString(context, "displayId")

        if (!DisplayConfig.exists(displayId)) {
            ErrorMessages.sendError(context.source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(context.source)
            return 0
        }

        if (!confirm) {
            DeleteConfirmMenu.showDisplay(context.source, displayId)
            return 1
        }

        val affectedHolograms = HologramConfig.getHolograms()
            .filter { (_, hologram) ->
                hologram.displays.any { it.displayId == displayId }
            }

        affectedHolograms.forEach { (name, hologram) ->
            hologram.displays.removeAll { it.displayId == displayId }
            HologramConfig.saveHologram(name, hologram)
            ViewerHandler.respawnForAllObservers(name)
        }

        DisplayConfig.deleteDisplay(displayId)
        playSuccessSound(context.source)
        return 1
    }
}