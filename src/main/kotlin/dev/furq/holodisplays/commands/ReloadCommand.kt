package dev.furq.holodisplays.commands

import com.mojang.brigadier.builder.ArgumentBuilder
import dev.furq.holodisplays.config.ConfigManager
import dev.furq.holodisplays.handlers.HologramHandler
import dev.furq.holodisplays.handlers.PacketHandler
import dev.furq.holodisplays.handlers.TickHandler
import dev.furq.holodisplays.handlers.ViewerHandler
import dev.furq.holodisplays.managers.FeedbackManager
import dev.furq.holodisplays.utils.FeedbackType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

object ReloadCommand {
    fun register(): ArgumentBuilder<ServerCommandSource, *> = CommandManager.literal("reload")
        .requires { it.hasPermissionLevel(2) }
        .executes { context ->
            performReload(context.source)
            1
        }

    private fun performReload(source: ServerCommandSource) {
        PacketHandler.resetEntityTracking()
        ViewerHandler.resetHologramObservers()
        TickHandler.init()
        ConfigManager.reload()
        HologramHandler.reinitialize()
        FeedbackManager.send(source, FeedbackType.RELOAD_SUCCESS)
    }
}