package dev.furq.holodisplays.commands

import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.gui.HologramEdit
import dev.furq.holodisplays.handlers.HologramHandler
import dev.furq.holodisplays.utils.CommandUtils
import dev.furq.holodisplays.utils.CommandUtils.playErrorSound
import dev.furq.holodisplays.utils.CommandUtils.playSuccessSound
import dev.furq.holodisplays.utils.Messages
import dev.furq.holodisplays.utils.Messages.ErrorType
import dev.furq.holodisplays.utils.Utils
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import org.joml.Vector3f

object LineCommand {
    fun register(): LiteralArgumentBuilder<ServerCommandSource> = CommandManager.literal("line")
        .then(CommandManager.argument("hologram", StringArgumentType.word())
            .suggests { _, builder -> CommandUtils.suggestHolograms(builder) }
            .then(CommandManager.literal("add")
                .then(CommandManager.argument("display", StringArgumentType.word())
                    .suggests { _, builder -> CommandUtils.suggestDisplays(builder) }
                    .executes { context -> executeAdd(context) }
                )
            )
            .then(CommandManager.literal("remove")
                .then(CommandManager.argument("index", IntegerArgumentType.integer())
                    .executes { context -> executeRemove(context) }
                )
            )
            .then(
                CommandManager.literal("offset")
                    .then(
                        CommandManager.argument("index", IntegerArgumentType.integer())
                            .then(
                                CommandManager.argument("x", FloatArgumentType.floatArg())
                                    .then(
                                        CommandManager.argument("y", FloatArgumentType.floatArg())
                                            .then(CommandManager.argument("z", FloatArgumentType.floatArg())
                                                .executes { context -> executeOffset(context) })
                                    )
                            )
                    )
            )
        )

    private fun executeAdd(context: CommandContext<ServerCommandSource>): Int {
        val hologramName = StringArgumentType.getString(context, "hologram")
        val displayId = StringArgumentType.getString(context, "display")

        if (!Utils.addDisplayToHologram(hologramName, displayId, context.source)) {
            return 0
        }

        HologramEdit.open(context.source.playerOrThrow, hologramName)
        return 1
    }

    private fun executeRemove(context: CommandContext<ServerCommandSource>): Int {
        val hologramName = StringArgumentType.getString(context, "hologram")
        val index = IntegerArgumentType.getInteger(context, "index")

        if (!Utils.removeLineFromHologram(hologramName, index, context.source)) {
            return 0
        }

        HologramEdit.open(context.source.playerOrThrow, hologramName)
        return 1
    }

    private fun executeOffset(context: CommandContext<ServerCommandSource>): Int {
        val hologramName = StringArgumentType.getString(context, "hologram")
        val index = IntegerArgumentType.getInteger(context, "index")
        val x = FloatArgumentType.getFloat(context, "x")
        val y = FloatArgumentType.getFloat(context, "y")
        val z = FloatArgumentType.getFloat(context, "z")

        if (!HologramConfig.exists(hologramName)) {
            Messages.sendError(context.source, ErrorType.HOLOGRAM_NOT_FOUND)
            playErrorSound(context.source)
            return 0
        }

        val hologram = HologramConfig.getHologram(hologramName) ?: return 0
        if (index >= hologram.displays.size) {
            Messages.sendError(context.source, ErrorType.LINE_NOT_FOUND)
            playErrorSound(context.source)
            return 0
        }

        HologramHandler.updateHologramProperty(
            hologramName,
            HologramHandler.HologramProperty.LineOffset(index, Vector3f(x, y, z))
        )
        playSuccessSound(context.source)
        HologramEdit.open(context.source.playerOrThrow, hologramName)
        return 1
    }
}