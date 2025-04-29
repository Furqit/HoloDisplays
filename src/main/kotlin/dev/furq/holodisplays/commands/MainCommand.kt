package dev.furq.holodisplays.commands

import com.mojang.brigadier.CommandDispatcher
import dev.furq.holodisplays.HoloDisplays
import dev.furq.holodisplays.gui.MainMenu
import dev.furq.holodisplays.managers.FeedbackManager
import dev.furq.holodisplays.utils.FeedbackType
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
                        if (context.source.player == null) {
                            FeedbackManager.send(context.source, FeedbackType.PLAYER_ONLY)
                            return@executes 0
                        }
                        MainMenu.openMainMenu(context.source.player!!)
                        1
                    }
                    .then(CreateCommand.register())
                    .then(
                        CommandManager.literal("display")
                            .then(ListCommand.registerDisplays())
                            .then(DeleteCommand.registerDisplay())
                            .then(DisplayEditCommand.register())
                    )
                    .then(
                        CommandManager.literal("hologram")
                            .then(ListCommand.registerHolograms())
                            .then(DeleteCommand.registerHologram())
                            .then(MoveCommand.register())
                            .then(HologramEditCommand.register())
                            .then(LineCommand.register())
                    )

                    .then(ReloadCommand.register())
            )
        }
    }
}