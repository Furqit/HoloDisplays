package dev.furq.holodisplays.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.furq.holodisplays.config.ConfigManager
import dev.furq.holodisplays.handlers.HologramHandler
import dev.furq.holodisplays.handlers.PacketHandler
import dev.furq.holodisplays.handlers.TickHandler
import dev.furq.holodisplays.handlers.ViewerHandler
import dev.furq.holodisplays.utils.CommandUtils.playSuccessSound
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object ReloadCommand {
    fun register(): LiteralArgumentBuilder<ServerCommandSource> = CommandManager.literal("reload")
        .requires { it.hasPermissionLevel(2) }
        .executes { context ->
            PacketHandler.resetEntityTracking()
            ViewerHandler.resetHologramObservers()
            TickHandler.init()
            ConfigManager.reload()
            HologramHandler.reinitialize()
            context.source.sendFeedback({
                Text.literal("Successfully reloaded all configurations!")
                    .formatted(Formatting.GREEN)
            }, false)
            playSuccessSound(context.source)
            1
        }
}