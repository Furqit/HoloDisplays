package dev.furq.holodisplays.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.furq.holodisplays.HoloDisplays
import dev.furq.holodisplays.gui.MainMenu
import dev.furq.holodisplays.utils.CommandUtils.requirePlayer
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

object MainCommand {
    private val commandAliases = listOf(
        HoloDisplays.MOD_ID.lowercase(),
        "hd",
        "holo"
    )

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        commandAliases.forEach { alias ->
            dispatcher.register(buildCommand(alias))
        }
    }

    private fun buildCommand(alias: String): LiteralArgumentBuilder<ServerCommandSource> {
        return CommandManager.literal(alias)
            .requires { it.hasPermissionLevel(2) }
            .executes { context ->
                requirePlayer(context)?.let {
                    MainMenu.openMainMenu(it)
                    1
                } ?: 0
            }
            .then(buildDisplayCommands())
            .then(buildHologramCommands())
            .then(ReloadCommand.register())
    }

    private fun buildDisplayCommands(): LiteralArgumentBuilder<ServerCommandSource> {
        return CommandManager.literal("display")
            .then(CreateCommand.registerDisplay())
            .then(ListCommand.registerDisplays())
            .then(DeleteCommand.registerDisplay())
            .then(DisplayEditCommand.register())
    }

    private fun buildHologramCommands(): LiteralArgumentBuilder<ServerCommandSource> {
        return CommandManager.literal("hologram")
            .then(CreateCommand.registerHologram())
            .then(ListCommand.registerHolograms())
            .then(DeleteCommand.registerHologram())
            .then(MoveCommand.register())
            .then(HologramEditCommand.register())
            .then(LineCommand.register())
    }
}