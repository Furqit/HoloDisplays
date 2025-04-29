package dev.furq.holodisplays.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.gui.DeleteConfirmation
import dev.furq.holodisplays.gui.MainMenu
import dev.furq.holodisplays.managers.FeedbackManager
import dev.furq.holodisplays.utils.CommandUtils
import dev.furq.holodisplays.utils.FeedbackType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

object DeleteCommand {
    fun registerDisplay(): ArgumentBuilder<ServerCommandSource, *> = CommandManager
        .argument("name", StringArgumentType.word())
        .suggests { _, builder -> CommandUtils.suggestDisplays(builder) }
        .executes { context -> executeDisplay(context) }

    fun registerHologram(): ArgumentBuilder<ServerCommandSource, *> = CommandManager
        .argument("name", StringArgumentType.word())
        .suggests { _, builder -> CommandUtils.suggestHolograms(builder) }
        .executes { context -> executeHologram(context) }

    private fun executeHologram(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val player = context.source.player ?: run {
            FeedbackManager.send(context.source, FeedbackType.PLAYER_ONLY)
            return 0
        }

        if (!HologramConfig.exists(name)) {
            FeedbackManager.send(context.source, FeedbackType.HOLOGRAM_NOT_FOUND, "name" to name)
            return 0
        }

        DeleteConfirmation.open(player, name, "hologram") {
            MainMenu.openMainMenu(player)
        }
        return 1
    }

    private fun executeDisplay(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val player = context.source.player ?: run {
            FeedbackManager.send(context.source, FeedbackType.PLAYER_ONLY)
            return 0
        }

        if (!DisplayConfig.exists(name)) {
            FeedbackManager.send(context.source, FeedbackType.DISPLAY_NOT_FOUND, "name" to name)
            return 0
        }

        DeleteConfirmation.open(player, name, "display") {
            MainMenu.openMainMenu(player)
        }
        return 1
    }
}