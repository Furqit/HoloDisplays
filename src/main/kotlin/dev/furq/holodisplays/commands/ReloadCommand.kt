package dev.furq.holodisplays.commands

import com.mojang.brigadier.builder.ArgumentBuilder
import dev.furq.holodisplays.config.ConfigManager
import dev.furq.holodisplays.handlers.HologramHandler
import dev.furq.holodisplays.handlers.PacketHandler
import dev.furq.holodisplays.handlers.TickHandler
import dev.furq.holodisplays.handlers.ViewerHandler
import dev.furq.holodisplays.managers.FeedbackManager
import dev.furq.holodisplays.utils.FeedbackType
//? if >=1.21.11
import net.minecraft.server.permissions.PermissionLevel
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

object ReloadCommand {
    fun register(): ArgumentBuilder<CommandSourceStack, *> = Commands.literal("reload")
        //~ if >=1.21.11 '2' -> 'PermissionLevel.GAMEMASTERS'
        .requires(Permissions.require("holodisplays.admin", PermissionLevel.GAMEMASTERS))
        .executes { context ->
            performReload(context.source)
            1
        }

    private fun performReload(source: CommandSourceStack) {
        ViewerHandler.resetAllObservers()
        PacketHandler.resetEntityTracking()
        ViewerHandler.clearTrackers()
        TickHandler.init()
        ConfigManager.reload()
        HologramHandler.reinitialize()
        FeedbackManager.send(source, FeedbackType.RELOAD_SUCCESS)
    }
}