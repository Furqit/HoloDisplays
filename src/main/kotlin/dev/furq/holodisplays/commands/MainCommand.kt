package dev.furq.holodisplays.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.furq.holodisplays.HoloDisplays
import dev.furq.holodisplays.gui.MainMenu
import dev.furq.holodisplays.utils.CommandUtils.requirePlayer
import me.lucko.fabric.api.permissions.v0.Permissions
//? if >=1.21.11
import net.minecraft.server.permissions.PermissionLevel
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

object MainCommand {
    private val commandAliases = listOf(
        HoloDisplays.MOD_ID.lowercase(),
        "hd",
        "holo"
    )

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        commandAliases.forEach { alias ->
            dispatcher.register(buildCommand(alias))
        }
    }

    private fun buildCommand(alias: String): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(alias)
            //~ if >=1.21.11 '2' -> 'PermissionLevel.GAMEMASTERS'
            .requires(Permissions.require("holodisplays.admin", PermissionLevel.GAMEMASTERS))
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

    private fun buildDisplayCommands(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("display")
            .then(CreateCommand.registerDisplay())
            .then(ListCommand.registerDisplays())
            .then(DeleteCommand.registerDisplay())
            .then(DisplayEditCommand.register())
    }

    private fun buildHologramCommands(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("hologram")
            .then(CreateCommand.registerHologram())
            .then(ListCommand.registerHolograms())
            .then(DeleteCommand.registerHologram())
            .then(MoveCommand.register())
            .then(HologramEditCommand.register())
            .then(LineCommand.register())
            .then(NearCommand.register())
    }
}