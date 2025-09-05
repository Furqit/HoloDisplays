package dev.furq.holodisplays.commands

import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.furq.holodisplays.utils.CommandUtils.requirePlayer
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import org.joml.Vector3f

abstract class EditCommand {
    abstract fun updateScale(name: String, scale: Vector3f, source: ServerCommandSource)
    abstract fun updateBillboard(name: String, mode: String, source: ServerCommandSource)
    abstract fun updateRotation(name: String, pitch: Float, yaw: Float, roll: Float, source: ServerCommandSource)
    abstract fun updateCondition(name: String, condition: String?, source: ServerCommandSource)
    abstract fun openEditGui(player: ServerPlayerEntity, name: String)
    fun buildScaleCommands(): ArgumentBuilder<ServerCommandSource, *> {
        return CommandManager.literal("scale")
            .then(CommandManager.argument("x", FloatArgumentType.floatArg(0.1f))
                .then(CommandManager.argument("y", FloatArgumentType.floatArg(0.1f))
                    .then(CommandManager.argument("z", FloatArgumentType.floatArg(0.1f))
                        .executes { context -> executeScale(context) })))
            .then(CommandManager.literal("reset")
                .executes { context -> executeResetScale(context) })
    }

    fun buildBillboardCommands(): ArgumentBuilder<ServerCommandSource, *> {
        return CommandManager.literal("billboard")
            .then(CommandManager.argument("mode", StringArgumentType.word())
                .suggests { _, builder ->
                    BillboardMode.entries.forEach { builder.suggest(it.name.lowercase()) }
                    builder.buildFuture()
                }
                .executes { context -> executeBillboard(context) })
            .then(CommandManager.literal("reset")
                .executes { context -> executeResetBillboard(context) })
    }

    fun buildRotationCommands(): ArgumentBuilder<ServerCommandSource, *> {
        return CommandManager.literal("rotation")
            .then(CommandManager.argument("pitch", FloatArgumentType.floatArg(-180f, 180f))
                .then(CommandManager.argument("yaw", FloatArgumentType.floatArg(-180f, 180f))
                    .then(CommandManager.argument("roll", FloatArgumentType.floatArg(-180f, 180f))
                        .executes { context -> executeRotation(context) })))
            .then(CommandManager.literal("reset")
                .executes { context -> executeResetRotation(context) })
    }

    fun buildConditionCommands(): ArgumentBuilder<ServerCommandSource, *> {
        return CommandManager.literal("condition")
            .then(CommandManager.argument("condition", StringArgumentType.greedyString())
                .executes { context -> executeCondition(context) })
            .then(CommandManager.literal("remove")
                .executes { context -> executeConditionRemove(context) })
    }

    fun executeEdit(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        return requirePlayer(context)?.let { player ->
            openEditGui(player, name)
            1
        } ?: 0
    }

    private fun executeScale(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val x = FloatArgumentType.getFloat(context, "x")
        val y = FloatArgumentType.getFloat(context, "y")
        val z = FloatArgumentType.getFloat(context, "z")
        updateScale(name, Vector3f(x, y, z), context.source)
        return 1
    }

    private fun executeResetScale(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        updateScale(name, Vector3f(1f), context.source)
        return 1
    }

    private fun executeBillboard(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val mode = StringArgumentType.getString(context, "mode")
        updateBillboard(name, mode, context.source)
        return 1
    }

    private fun executeResetBillboard(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        updateBillboard(name, BillboardMode.CENTER.name, context.source)
        return 1
    }

    private fun executeRotation(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val pitch = FloatArgumentType.getFloat(context, "pitch")
        val yaw = FloatArgumentType.getFloat(context, "yaw")
        val roll = FloatArgumentType.getFloat(context, "roll")
        updateRotation(name, pitch, yaw, roll, context.source)
        return 1
    }

    private fun executeResetRotation(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        updateRotation(name, 0f, 0f, 0f, context.source)
        return 1
    }

    private fun executeCondition(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val condition = StringArgumentType.getString(context, "condition")
        updateCondition(name, condition, context.source)
        return 1
    }

    private fun executeConditionRemove(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        updateCondition(name, null, context.source)
        return 1
    }
}