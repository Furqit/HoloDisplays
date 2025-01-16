package dev.furq.holodisplays.commands

import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.furq.holodisplays.gui.HologramEdit
import dev.furq.holodisplays.utils.CommandUtils
import dev.furq.holodisplays.utils.Utils
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import org.joml.Vector3f

object HologramEditCommand {
    fun register(): ArgumentBuilder<ServerCommandSource, *> = CommandManager
        .argument("name", StringArgumentType.word())
        .suggests { _, builder -> CommandUtils.suggestHolograms(builder) }
        .executes { context -> executeEdit(context) }
        .then(
            CommandManager.literal("scale")
                .then(
                    CommandManager.argument("x", FloatArgumentType.floatArg(0.1f))
                        .then(CommandManager.argument("y", FloatArgumentType.floatArg(0.1f))
                            .then(CommandManager.argument("z", FloatArgumentType.floatArg(0.1f))
                                .executes { context -> executeScale(context) }
                            )
                        )
                )
        )
        .then(CommandManager.literal("billboard")
            .then(CommandManager.argument("mode", StringArgumentType.word())
                .suggests { _, builder ->
                    BillboardMode.entries.forEach { builder.suggest(it.name.lowercase()) }
                    builder.buildFuture()
                }
                .executes { context -> executeBillboard(context) })
        )
        .then(
            CommandManager.literal("updateRate")
                .then(CommandManager.argument("ticks", IntegerArgumentType.integer(20))
                    .executes { context -> executeUpdateRate(context) })
        )
        .then(
            CommandManager.literal("viewRange")
                .then(CommandManager.argument("blocks", FloatArgumentType.floatArg(1f, 128f))
                    .executes { context -> executeViewRange(context) })
        )
        .then(
            CommandManager.literal("rotation")
                .then(
                    CommandManager.argument("pitch", FloatArgumentType.floatArg(-180f, 180f))
                        .then(CommandManager.argument("yaw", FloatArgumentType.floatArg(-180f, 180f))
                            .then(CommandManager.argument("roll", FloatArgumentType.floatArg(-180f, 180f))
                                .executes { context -> executeRotation(context) }
                            )
                        )
                )
        )

    private fun executeEdit(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        HologramEdit.open(context.source.playerOrThrow, name)
        return 1
    }

    private fun executeScale(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val x = FloatArgumentType.getFloat(context, "x")
        val y = FloatArgumentType.getFloat(context, "y")
        val z = FloatArgumentType.getFloat(context, "z")

        return if (Utils.updateHologramScale(name, Vector3f(x, y, z), context.source)) 1 else 0
    }

    private fun executeBillboard(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val mode = StringArgumentType.getString(context, "mode").uppercase()
        return if (Utils.updateHologramBillboard(name, mode, context.source)) 1 else 0
    }

    private fun executeUpdateRate(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val ticks = IntegerArgumentType.getInteger(context, "ticks")
        return if (Utils.updateHologramUpdateRate(name, ticks, context.source)) 1 else 0
    }

    private fun executeViewRange(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val blocks = FloatArgumentType.getFloat(context, "blocks")
        return if (Utils.updateHologramViewRange(name, blocks, context.source)) 1 else 0
    }

    private fun executeRotation(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val pitch = FloatArgumentType.getFloat(context, "pitch")
        val yaw = FloatArgumentType.getFloat(context, "yaw")
        val roll = FloatArgumentType.getFloat(context, "roll")
        return if (Utils.updateHologramRotation(name, pitch, yaw, roll, context.source)) 1 else 0
    }
}