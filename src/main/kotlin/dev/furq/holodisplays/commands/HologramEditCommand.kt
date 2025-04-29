package dev.furq.holodisplays.commands

import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.gui.HologramEdit
import dev.furq.holodisplays.managers.FeedbackManager
import dev.furq.holodisplays.managers.HologramManager
import dev.furq.holodisplays.utils.CommandUtils
import dev.furq.holodisplays.utils.FeedbackType
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import org.joml.Vector3f

object HologramEditCommand {
    private val hologramManager = HologramManager()

    fun register(): ArgumentBuilder<ServerCommandSource, *> = CommandManager
        .literal("edit")
        .then(CommandManager.argument("name", StringArgumentType.word())
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
                    .then(CommandManager.literal("reset")
                        .executes { context -> executeResetScale(context) })
            )
            .then(CommandManager.literal("billboard")
                .then(CommandManager.argument("mode", StringArgumentType.word())
                    .suggests { _, builder ->
                        BillboardMode.entries.forEach { builder.suggest(it.name.lowercase()) }
                        builder.buildFuture()
                    }
                    .executes { context -> executeBillboard(context) })
                .then(CommandManager.literal("reset")
                    .executes { context -> executeResetBillboard(context) })
            )
            .then(
                CommandManager.literal("updateRate")
                    .then(CommandManager.argument("ticks", IntegerArgumentType.integer(1, 100))
                        .executes { context -> executeUpdateRate(context) })
                    .then(CommandManager.literal("reset")
                        .executes { context -> executeResetUpdateRate(context) })
            )
            .then(
                CommandManager.literal("viewRange")
                    .then(CommandManager.argument("blocks", FloatArgumentType.floatArg(1f, 128f))
                        .executes { context -> executeViewRange(context) })
                    .then(CommandManager.literal("reset")
                        .executes { context -> executeResetViewRange(context) })
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
                    .then(CommandManager.literal("reset")
                        .executes { context -> executeResetRotation(context) })
            )
            .then(
                CommandManager.literal("condition")
                    .then(
                        CommandManager.argument("condition", StringArgumentType.greedyString())
                            .executes { context -> executeCondition(context) }
                    )
                    .then(
                        CommandManager.literal("remove")
                            .executes { context -> executeConditionRemove(context) }
                    )
            )
        )

    private fun executeEdit(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        if (!HologramConfig.exists(name)) {
            FeedbackManager.send(context.source, FeedbackType.HOLOGRAM_NOT_FOUND, "name" to name)
            return 0
        }
        HologramEdit.open(context.source.player!!, name)
        return 1
    }

    private fun executeScale(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val x = FloatArgumentType.getFloat(context, "x")
        val y = FloatArgumentType.getFloat(context, "y")
        val z = FloatArgumentType.getFloat(context, "z")
        hologramManager.updateScale(name, Vector3f(x, y, z), context.source)
        return 1
    }

    private fun executeResetScale(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        hologramManager.updateScale(name, Vector3f(1f), context.source)
        return 1
    }

    private fun executeBillboard(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val mode = StringArgumentType.getString(context, "mode")
        hologramManager.updateBillboard(name, mode, context.source)
        return 1
    }

    private fun executeResetBillboard(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        hologramManager.updateBillboard(name, BillboardMode.CENTER.name, context.source)
        return 1
    }

    private fun executeUpdateRate(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val ticks = IntegerArgumentType.getInteger(context, "ticks")
        hologramManager.updateUpdateRate(name, ticks, context.source)
        return 1
    }

    private fun executeResetUpdateRate(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        hologramManager.updateUpdateRate(name, 20, context.source)
        return 1
    }

    private fun executeViewRange(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val blocks = FloatArgumentType.getFloat(context, "blocks")
        hologramManager.updateViewRange(name, blocks, context.source)
        return 1
    }

    private fun executeResetViewRange(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        hologramManager.updateViewRange(name, 16f, context.source)
        return 1
    }

    private fun executeRotation(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val pitch = FloatArgumentType.getFloat(context, "pitch")
        val yaw = FloatArgumentType.getFloat(context, "yaw")
        val roll = FloatArgumentType.getFloat(context, "roll")

        if (pitch < -180f || pitch > 180f || yaw < -180f || yaw > 180f || roll < -180f || roll > 180f) {
            FeedbackManager.send(context.source, FeedbackType.INVALID_ROTATION)
            return 0
        }

        hologramManager.updateRotation(name, pitch, yaw, roll, context.source)
        return 1
    }

    private fun executeResetRotation(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        hologramManager.updateRotation(name, 0f, 0f, 0f, context.source)
        return 1
    }

    private fun executeCondition(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val condition = StringArgumentType.getString(context, "condition")

        if (!condition.matches(Regex("^%[\\w:]+% *(=|!=|>|<|>=|<=|contains|!contains|startsWith|endsWith) *\\S+$"))) {
            FeedbackManager.send(context.source, FeedbackType.INVALID_CONDITION)
            return 0
        }

        hologramManager.updateCondition(name, condition, context.source)
        return 1
    }

    private fun executeConditionRemove(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        hologramManager.updateCondition(name, null, context.source)
        return 1
    }
}