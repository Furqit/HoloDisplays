package dev.furq.holodisplays.commands

import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.gui.HologramEdit
import dev.furq.holodisplays.managers.FeedbackManager
import dev.furq.holodisplays.managers.HologramManager
import dev.furq.holodisplays.utils.CommandUtils
import dev.furq.holodisplays.utils.FeedbackType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import org.joml.Vector3f

object LineCommand {
    private val hologramManager = HologramManager()

    fun register(): ArgumentBuilder<ServerCommandSource, *> = CommandManager.literal("line")
        .then(CommandManager.argument("hologram", StringArgumentType.word())
            .suggests { _, builder -> CommandUtils.suggestHolograms(builder) }
            .then(CommandManager.literal("add")
                .then(CommandManager.argument("display", StringArgumentType.word())
                    .suggests { _, builder -> CommandUtils.suggestDisplays(builder) }
                    .executes { context -> executeAdd(context) }
                )
            )
            .then(CommandManager.literal("remove")
                .then(CommandManager.argument("display", StringArgumentType.word())
                    .executes { context -> executeRemove(context) }
                )
            )
            .then(
                CommandManager.literal("offset")
                    .then(
                        CommandManager.argument("display", StringArgumentType.word())
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

        hologramManager.addDisplayToHologram(hologramName, displayId, context.source)
        HologramEdit.open(context.source.playerOrThrow, hologramName)
        return 1
    }

    private fun executeRemove(context: CommandContext<ServerCommandSource>): Int {
        val hologramName = StringArgumentType.getString(context, "hologram")
        val display = StringArgumentType.getString(context, "display")

        hologramManager.removeDisplayFromHologram(hologramName, display, context.source)
        HologramEdit.open(context.source.playerOrThrow, hologramName)
        return 1
    }

    private fun executeOffset(context: CommandContext<ServerCommandSource>): Int {
        val hologramName = StringArgumentType.getString(context, "hologram")
        val display = StringArgumentType.getString(context, "display")
        val x = FloatArgumentType.getFloat(context, "x")
        val y = FloatArgumentType.getFloat(context, "y")
        val z = FloatArgumentType.getFloat(context, "z")

        if (!HologramConfig.exists(hologramName)) {
            FeedbackManager.send(context.source, FeedbackType.HOLOGRAM_NOT_FOUND, "name" to hologramName)
            return 0
        }

        hologramManager.updateDisplayOffset(hologramName, display, Vector3f(x, y, z), context.source)
        HologramEdit.open(context.source.playerOrThrow, hologramName)
        return 1
    }
}