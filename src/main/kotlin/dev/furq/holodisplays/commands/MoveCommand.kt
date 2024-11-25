package dev.furq.holodisplays.commands

import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.data.HologramData
import dev.furq.holodisplays.handlers.HologramHandler
import dev.furq.holodisplays.menu.EditMenu
import dev.furq.holodisplays.utils.CommandUtils
import dev.furq.holodisplays.utils.CommandUtils.playErrorSound
import dev.furq.holodisplays.utils.CommandUtils.playSuccessSound
import dev.furq.holodisplays.utils.ErrorMessages
import dev.furq.holodisplays.utils.ErrorMessages.ErrorType
import dev.furq.holodisplays.utils.HandlerUtils.HologramProperty
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.math.Vec3d

object MoveCommand {
    fun register(): LiteralArgumentBuilder<ServerCommandSource> = CommandManager.literal("move")
        .then(CommandManager.argument("name", StringArgumentType.word())
            .suggests { _, builder -> CommandUtils.suggestHolograms(builder) }
            .executes { context -> executeMove(context) }
            .then(
                CommandManager.argument("x", FloatArgumentType.floatArg())
                    .then(
                        CommandManager.argument("y", FloatArgumentType.floatArg())
                            .then(CommandManager.argument("z", FloatArgumentType.floatArg())
                                .executes { context -> executeMove(context) })
                    )
            )
        )

    private fun executeMove(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")

        if (!HologramConfig.exists(name)) {
            ErrorMessages.sendError(context.source, ErrorType.HOLOGRAM_NOT_FOUND)
            playErrorSound(context.source)
            return 0
        }

        val worldId = context.source.world.registryKey.value.toString()
        val pos = try {
            Vec3d(
                FloatArgumentType.getFloat(context, "x").toDouble(),
                FloatArgumentType.getFloat(context, "y").toDouble(),
                FloatArgumentType.getFloat(context, "z").toDouble()
            )
        } catch (e: IllegalArgumentException) {
            context.source.position
        }

        val property = HologramProperty.Position(
            HologramData.Position(
                worldId,
                String.format("%.3f", pos.x).toFloat(),
                String.format("%.3f", pos.y).toFloat(),
                String.format("%.3f", pos.z).toFloat()
            ),
        )

        HologramHandler.updateHologramProperty(name, property)
        playSuccessSound(context.source)
        EditMenu.showHologram(context.source, name)
        return 1
    }
}