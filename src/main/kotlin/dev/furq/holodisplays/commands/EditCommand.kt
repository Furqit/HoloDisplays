package dev.furq.holodisplays.commands

import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.furq.holodisplays.utils.CommandUtils.requirePlayer
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Display.BillboardConstraints
import org.joml.Vector3f

abstract class EditCommand {
    abstract fun updateScale(name: String, scale: Vector3f, source: CommandSourceStack)
    abstract fun resetScale(name: String, source: CommandSourceStack)
    abstract fun updateBillboard(name: String, mode: String, source: CommandSourceStack)
    abstract fun resetBillboard(name: String, source: CommandSourceStack)
    abstract fun updateRotation(name: String, pitch: Float, yaw: Float, roll: Float, source: CommandSourceStack)
    abstract fun resetRotation(name: String, source: CommandSourceStack)
    abstract fun updateCondition(name: String, condition: String?, source: CommandSourceStack)
    abstract fun openEditGui(player: ServerPlayer, name: String)
    fun buildScaleCommands(): ArgumentBuilder<CommandSourceStack, *> {
        return Commands.literal("scale")
            .then(Commands.argument("x", FloatArgumentType.floatArg(0.1f))
                .then(Commands.argument("y", FloatArgumentType.floatArg(0.1f))
                    .then(Commands.argument("z", FloatArgumentType.floatArg(0.1f))
                        .executes { context -> executeScale(context) })))
            .then(Commands.literal("reset")
                .executes { context -> executeResetScale(context) })
    }

    fun buildBillboardCommands(): ArgumentBuilder<CommandSourceStack, *> {
        return Commands.literal("billboard")
            .then(Commands.argument("mode", StringArgumentType.word())
                .suggests { _, builder ->
                    BillboardConstraints.entries.forEach { builder.suggest(it.name.lowercase()) }
                    builder.buildFuture()
                }
                .executes { context -> executeBillboard(context) })
            .then(Commands.literal("reset")
                .executes { context -> executeResetBillboard(context) })
    }

    fun buildRotationCommands(): ArgumentBuilder<CommandSourceStack, *> {
        return Commands.literal("rotation")
            .then(Commands.argument("pitch", FloatArgumentType.floatArg(-180f, 180f))
                .then(Commands.argument("yaw", FloatArgumentType.floatArg(-180f, 180f))
                    .then(Commands.argument("roll", FloatArgumentType.floatArg(-180f, 180f))
                        .executes { context -> executeRotation(context) })))
            .then(Commands.literal("reset")
                .executes { context -> executeResetRotation(context) })
    }

    fun buildConditionCommands(): ArgumentBuilder<CommandSourceStack, *> {
        return Commands.literal("condition")
            .then(Commands.argument("condition", StringArgumentType.greedyString())
                .executes { context -> executeCondition(context) })
            .then(Commands.literal("remove")
                .executes { context -> executeConditionRemove(context) })
    }

    fun executeEdit(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        return requirePlayer(context)?.let { player ->
            openEditGui(player, name)
            1
        } ?: 0
    }

    private fun executeScale(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        val x = FloatArgumentType.getFloat(context, "x")
        val y = FloatArgumentType.getFloat(context, "y")
        val z = FloatArgumentType.getFloat(context, "z")
        updateScale(name, Vector3f(x, y, z), context.source)
        return 1
    }

    private fun executeResetScale(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        resetScale(name, context.source)
        return 1
    }

    private fun executeBillboard(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        val mode = StringArgumentType.getString(context, "mode")
        updateBillboard(name, mode, context.source)
        return 1
    }

    private fun executeResetBillboard(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        resetBillboard(name, context.source)
        return 1
    }

    private fun executeRotation(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        val pitch = FloatArgumentType.getFloat(context, "pitch")
        val yaw = FloatArgumentType.getFloat(context, "yaw")
        val roll = FloatArgumentType.getFloat(context, "roll")
        updateRotation(name, pitch, yaw, roll, context.source)
        return 1
    }

    private fun executeResetRotation(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        resetRotation(name, context.source)
        return 1
    }

    private fun executeCondition(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        val condition = StringArgumentType.getString(context, "condition")
        updateCondition(name, condition, context.source)
        return 1
    }

    private fun executeConditionRemove(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        updateCondition(name, null, context.source)
        return 1
    }
}