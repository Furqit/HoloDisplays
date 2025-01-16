package dev.furq.holodisplays.commands

import com.mojang.brigadier.CommandDispatcher
import dev.furq.holodisplays.HoloDisplays
import dev.furq.holodisplays.gui.MainMenu
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
            dispatcher.register(
                CommandManager.literal(alias)
                    .requires { it.hasPermissionLevel(2) }
                    .executes { context ->
                        MainMenu.openMainMenu(context.source.player!!)
                        1
                    }
                    .then(CreateCommand.register())
                    .then(LineCommand.register())
                    .then(ListCommand.register())
                    .then(MoveCommand.register())
                    .then(DeleteCommand.register())
                    .then(EditCommand.register())
                    .then(ReloadCommand.register())
            )
        }
    }
}